package com.examchain.exam;

import com.examchain.ExamchainApplication;
import com.examchain.exam.dto.ExamDtos.*;
import com.examchain.exam.model.ExamStatus;
import com.examchain.question.dto.QuestionDtos.*;
import com.examchain.question.model.CognitiveLevel;
import com.examchain.question.model.DifficultyLevel;
import com.examchain.question.model.PoolStatus;
import com.examchain.question.model.QuestionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ExamchainApplication.class)
@ActiveProfiles("test")
public class ExamAndQuestionPoolTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private static String json(Object obj) {
        if (obj instanceof ExamRequest r) {
            return String.format("{\"examCode\":\"%s\",\"title\":\"%s\",\"description\":\"%s\",\"academicSession\":\"%s\",\"status\":\"%s\"}",
                    r.examCode(), r.title(), r.description(), r.academicSession(), r.status());
        }
        if (obj instanceof SubjectRequest s) {
            return String.format("{\"subjectCode\":\"%s\",\"name\":\"%s\",\"totalMarks\":%d,\"passingMarks\":%d}",
                    s.subjectCode(), s.name(), s.totalMarks(), s.passingMarks());
        }
        if (obj instanceof AssignSetterRequest a) {
            return String.format("{\"setterId\":\"%s\"}", a.setterId());
        }
        if (obj instanceof QuestionPoolRequest p) {
            return String.format("{\"subjectId\":\"%s\",\"poolCode\":\"%s\",\"name\":\"%s\",\"description\":\"%s\"}",
                    p.subjectId(), p.poolCode(), p.name(), p.description());
        }
        if (obj instanceof PoolApprovalRequest a) {
            return String.format("{\"status\":\"%s\"}", a.status());
        }
        if (obj instanceof QuestionRequest q) {
            String optJson = q.content().options() != null
                    ? "[\"" + String.join("\",\"", q.content().options()) + "\"]"
                    : "[]";
            return String.format("{\"questionId\":\"%s\",\"unit\":%d,\"marks\":%d,\"difficulty\":\"%s\",\"questionType\":\"%s\",\"cognitiveLevel\":\"%s\",\"content\":{\"questionText\":\"%s\",\"options\":%s,\"correctOptionIndex\":\"%s\",\"rubricOrExplanation\":\"%s\"}}",
                    q.questionId(), q.unit(), q.marks(), q.difficulty(), q.questionType(), q.cognitiveLevel(),
                    q.content().questionText(), optJson, q.content().correctOptionIndex(), q.content().rubricOrExplanation());
        }
        return "{}";
    }

    @Test
    @DisplayName("Exam Lifecycle: Create exam, subject, and assign setter")
    void testExamAndSubjectCreationWorkflow() throws Exception {
        // 1. Create Exam as EXAM_AUTHORITY
        String examReq = json(new ExamRequest("EXAM-2026-CS", "Computer Science Tripos", "Annual final examinations", "2025-2026", ExamStatus.ACTIVE));
        MvcResult examResult = mockMvc.perform(post("/api/v1/authority/exams")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(examReq))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.examCode").value("EXAM-2026-CS"))
                .andReturn();

        String examId = extractField(examResult.getResponse().getContentAsString(), "id");

        // 2. Reject duplicate exam code
        mockMvc.perform(post("/api/v1/authority/exams")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(examReq))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));

        // 3. Create Subject for Exam
        String subReq = json(new SubjectRequest("CS-301", "Distributed Systems Security", 100, 40));
        MvcResult subResult = mockMvc.perform(post("/api/v1/authority/exams/" + examId + "/subjects")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(subReq))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.subjectCode").value("CS-301"))
                .andReturn();

        String subjectId = extractField(subResult.getResponse().getContentAsString(), "id");

        // 4. Reject invalid passing marks (passingMarks > totalMarks)
        String invalidSubReq = json(new SubjectRequest("CS-302", "Fault Tolerant Consensus", 50, 60));
        mockMvc.perform(post("/api/v1/authority/exams/" + examId + "/subjects")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidSubReq))
                .andExpect(status().isBadRequest());

        // 5. Assign Setter
        String assignReq = json(new AssignSetterRequest("prof_turing"));
        mockMvc.perform(post("/api/v1/authority/subjects/" + subjectId + "/setters")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CONTROLLER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignReq))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.setterId").value("prof_turing"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Question Pool Lifecycle & Question CRUD with SHA-256 Hashing")
    void testQuestionPoolAndQuestionWorkflow() throws Exception {
        // Setup: Create exam & subject
        String examReq = json(new ExamRequest("EXAM-2026-MATH", "Applied Cryptography", "Special term", "2026", ExamStatus.ACTIVE));
        MvcResult examResult = mockMvc.perform(post("/api/v1/authority/exams")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(examReq))
                .andExpect(status().isCreated())
                .andReturn();
        String examId = extractField(examResult.getResponse().getContentAsString(), "id");

        String subReq = json(new SubjectRequest("MATH-501", "Elliptic Curve Cryptography", 100, 50));
        MvcResult subResult = mockMvc.perform(post("/api/v1/authority/exams/" + examId + "/subjects")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(subReq))
                .andExpect(status().isCreated())
                .andReturn();
        String subjectId = extractField(subResult.getResponse().getContentAsString(), "id");

        // 1. Create Question Pool as PAPER_SETTER
        String poolReq = json(new QuestionPoolRequest(UUID.fromString(subjectId), "POOL-ECC-A", "ECC Unit 1 Questions", "Blind questions"));
        MvcResult poolResult = mockMvc.perform(post("/api/v1/setter/pools")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(poolReq))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();
        String poolId = extractField(poolResult.getResponse().getContentAsString(), "id");

        // 2. Cannot submit empty pool
        mockMvc.perform(post("/api/v1/setter/pools/" + poolId + "/submit")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER"))))
                .andExpect(status().isBadRequest());

        // 3. Create Question in Pool
        QuestionContent content1 = new QuestionContent(
                "What is the discrete logarithm problem over secp256k1?",
                List.of("Finding k given P and Q=kP", "Computing point doubling", "Calculating scalar multiplication", "Finding curve order"),
                "0",
                "Full marks for identifying the difficulty of inverting scalar multiplication."
        );
        QuestionRequest qReq1 = new QuestionRequest(
                "Q-MATH501-001",
                1,
                5,
                DifficultyLevel.HARD,
                QuestionType.MCQ,
                CognitiveLevel.ANALYZE,
                content1
        );

        MvcResult qResult = mockMvc.perform(post("/api/v1/setter/pools/" + poolId + "/questions")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(qReq1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.questionId").value("Q-MATH501-001"))
                .andExpect(jsonPath("$.data.version").value(1))
                .andExpect(jsonPath("$.data.hash").isString())
                .andReturn();

        String questionDbId = extractField(qResult.getResponse().getContentAsString(), "id");
        String initialHash = extractField(qResult.getResponse().getContentAsString(), "hash");
        assertThat(initialHash).hasSize(64);

        // 4. Reject duplicate question ID
        mockMvc.perform(post("/api/v1/setter/pools/" + poolId + "/questions")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(qReq1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already exists")));

        // 5. Reject invalid metadata (marks <= 0 or unit <= 0)
        QuestionRequest invalidMarksReq = new QuestionRequest(
                "Q-MATH501-002",
                1,
                -10, // invalid marks
                DifficultyLevel.EASY,
                QuestionType.SHORT_ANSWER,
                CognitiveLevel.UNDERSTAND,
                new QuestionContent("Explain ECDSA nonce reuse", List.of(), "", "Key leakage explanation")
        );
        mockMvc.perform(post("/api/v1/setter/pools/" + poolId + "/questions")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalidMarksReq)))
                .andExpect(status().isBadRequest());

        // 6. Update question: increments version to 2 and recalculates SHA-256 hash
        QuestionContent updatedContent = new QuestionContent(
                "What is the discrete logarithm problem over secp256k1? (Updated with clarification)",
                content1.options(),
                content1.correctOptionIndex(),
                content1.rubricOrExplanation()
        );
        QuestionRequest updateReq = new QuestionRequest(
                "Q-MATH501-001",
                1,
                5,
                DifficultyLevel.HARD,
                QuestionType.MCQ,
                CognitiveLevel.ANALYZE,
                updatedContent
        );

        MvcResult updateResult = mockMvc.perform(put("/api/v1/setter/questions/" + questionDbId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").value(2))
                .andReturn();

        String updatedHash = extractField(updateResult.getResponse().getContentAsString(), "hash");
        assertThat(updatedHash).isNotEqualTo(initialHash);

        // 7. Submit Pool for Review
        mockMvc.perform(post("/api/v1/setter/pools/" + poolId + "/submit")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        // 8. Authority approves pool
        mockMvc.perform(post("/api/v1/authority/pools/" + poolId + "/review")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PoolApprovalRequest(PoolStatus.APPROVED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // 9. Approved Pool Locking: verify questions cannot be added, edited, or deleted once APPROVED
        QuestionRequest lockedAddReq = new QuestionRequest(
                "Q-MATH501-003",
                2,
                10,
                DifficultyLevel.MEDIUM,
                QuestionType.LONG_ANSWER,
                CognitiveLevel.CREATE,
                new QuestionContent("Design an ECDH key exchange", List.of(), "", "Rubric")
        );
        mockMvc.perform(post("/api/v1/setter/pools/" + poolId + "/questions")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(lockedAddReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("locked pool")));

        // Attempt edit in locked pool
        mockMvc.perform(put("/api/v1/setter/questions/" + questionDbId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updateReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("locked pool")));

        // Attempt delete in locked pool
        mockMvc.perform(delete("/api/v1/setter/questions/" + questionDbId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("locked pool")));
    }

    @Test
    @DisplayName("Role RBAC: STUDENT and EXAM_OPERATOR cannot access exam or setter authoring")
    void testLeastPrivilegeRbac() throws Exception {
        // STUDENT attempting to create exam -> 403 Forbidden
        mockMvc.perform(post("/api/v1/authority/exams")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ExamRequest("HACK-EXAM", "Hack", "Desc", "2026", ExamStatus.ACTIVE))))
                .andExpect(status().isForbidden());

        // STUDENT attempting to create question pool -> 403 Forbidden
        mockMvc.perform(post("/api/v1/setter/pools")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        // PAPER_SETTER attempting to review/approve pool -> 403 Forbidden
        mockMvc.perform(post("/api/v1/authority/pools/" + UUID.randomUUID() + "/review")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PoolApprovalRequest(PoolStatus.APPROVED))))
                .andExpect(status().isForbidden());
    }

    private static String extractField(String json, String field) {
        String pattern = "\"" + field + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) return "";
        start += pattern.length();
        int end = json.indexOf("\"", start);
        return end != -1 ? json.substring(start, end) : "";
    }
}

