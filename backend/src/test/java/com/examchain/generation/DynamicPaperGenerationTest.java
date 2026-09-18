package com.examchain.generation;

import com.examchain.ExamchainApplication;
import com.examchain.exam.dto.ExamDtos.*;
import com.examchain.exam.model.ExamStatus;
import com.examchain.generation.dto.BlueprintDtos.*;
import com.examchain.generation.model.PolicyMode;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ExamchainApplication.class)
@ActiveProfiles("test")
public class DynamicPaperGenerationTest {

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
        if (obj instanceof QuestionPoolRequest p) {
            return String.format("{\"subjectId\":\"%s\",\"poolCode\":\"%s\",\"name\":\"%s\",\"description\":\"%s\"}",
                    p.subjectId(), p.poolCode(), p.name(), p.description());
        }
        if (obj instanceof PoolApprovalRequest a) {
            return String.format("{\"status\":\"%s\"}", a.status());
        }
        if (obj instanceof QuestionRequest q) {
            return String.format("{\"questionId\":\"%s\",\"unit\":%d,\"marks\":%d,\"difficulty\":\"%s\",\"questionType\":\"%s\",\"cognitiveLevel\":\"%s\",\"content\":{\"questionText\":\"%s\",\"options\":[],\"correctOptionIndex\":\"\",\"rubricOrExplanation\":\"\"}}",
                    q.questionId(), q.unit(), q.marks(), q.difficulty(), q.questionType(), q.cognitiveLevel(), q.content().questionText());
        }
        if (obj instanceof BlueprintRequest b) {
            StringBuilder rulesJson = new StringBuilder("[");
            for (int i = 0; i < b.rules().size(); i++) {
                if (i > 0) rulesJson.append(",");
                BlueprintRuleDto r = b.rules().get(i);
                rulesJson.append(String.format("{\"unit\":%d,\"marksPerQuestion\":%d,\"difficulty\":\"%s\",\"questionType\":\"%s\",\"count\":%d}",
                        r.unit(), r.marksPerQuestion(), r.difficulty().name(), r.questionType().name(), r.count()));
            }
            rulesJson.append("]");
            return String.format("{\"blueprintCode\":\"%s\",\"name\":\"%s\",\"totalMarks\":%d,\"durationMinutes\":%d,\"policyMode\":\"%s\",\"rules\":%s}",
                    b.blueprintCode(), b.name(), b.totalMarks(), b.durationMinutes(), b.policyMode().name(), rulesJson);
        }
        if (obj instanceof GeneratePaperRequest g) {
            return String.format("{\"setCode\":\"%s\"}", g.setCode());
        }
        return "{}";
    }

    @Test
    @DisplayName("Algorithmic Paper Generation: Full workflow with constraint satisfaction, duplicate prevention, and cryptographic hashing")
    void testDynamicPaperGenerationWorkflow() throws Exception {
        // 1. Setup Exam & Subject
        String examReq = json(new ExamRequest("EXAM-GEN-01", "Software Engineering Exam", "Annual 2026", "2026", ExamStatus.ACTIVE));
        MvcResult examResult = mockMvc.perform(post("/api/v1/authority/exams")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(examReq))
                .andExpect(status().isCreated())
                .andReturn();
        String examId = extractField(examResult.getResponse().getContentAsString(), "id");

        String subReq = json(new SubjectRequest("SE-401", "Secure Architecture", 20, 8));
        MvcResult subResult = mockMvc.perform(post("/api/v1/authority/exams/" + examId + "/subjects")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(subReq))
                .andExpect(status().isCreated())
                .andReturn();
        String subjectId = extractField(subResult.getResponse().getContentAsString(), "id");

        // 2. Setup Question Pool
        String poolReq = json(new QuestionPoolRequest(UUID.fromString(subjectId), "POOL-SE-01", "Architecture Pool", "Unit 1 and 2"));
        MvcResult poolResult = mockMvc.perform(post("/api/v1/setter/pools")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(poolReq))
                .andExpect(status().isCreated())
                .andReturn();
        String poolId = extractField(poolResult.getResponse().getContentAsString(), "id");

        // 3. Populate questions:
        // Rule requires: 2 x Unit 1, 5M, EASY, MCQ = 10 marks
        // Rule requires: 1 x Unit 2, 10M, HARD, LONG_ANSWER = 10 marks
        // Total marks = 20 marks

        // Add 3 candidate questions for Unit 1 (so random selection occurs)
        for (int i = 1; i <= 3; i++) {
            QuestionRequest q = new QuestionRequest(
                    "Q-SE-U1-00" + i,
                    1,
                    5,
                    DifficultyLevel.EASY,
                    QuestionType.MCQ,
                    CognitiveLevel.UNDERSTAND,
                    new QuestionContent("Explain security concept " + i, List.of("A", "B"), "0", "Rubric")
            );
            mockMvc.perform(post("/api/v1/setter/pools/" + poolId + "/questions")
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(q)))
                    .andExpect(status().isCreated());
        }

        // Add 2 candidate questions for Unit 2
        for (int i = 1; i <= 2; i++) {
            QuestionRequest q = new QuestionRequest(
                    "Q-SE-U2-00" + i,
                    2,
                    10,
                    DifficultyLevel.HARD,
                    QuestionType.LONG_ANSWER,
                    CognitiveLevel.CREATE,
                    new QuestionContent("Design zero-trust architecture " + i, List.of(), "", "Rubric")
            );
            mockMvc.perform(post("/api/v1/setter/pools/" + poolId + "/questions")
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(q)))
                    .andExpect(status().isCreated());
        }

        // Submit and approve pool
        mockMvc.perform(post("/api/v1/setter/pools/" + poolId + "/submit")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/authority/pools/" + poolId + "/review")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new PoolApprovalRequest(PoolStatus.APPROVED))))
                .andExpect(status().isOk());

        // 4. Create Blueprint
        List<BlueprintRuleDto> rules = List.of(
                new BlueprintRuleDto(1, 5, DifficultyLevel.EASY, QuestionType.MCQ, 2),
                new BlueprintRuleDto(2, 10, DifficultyLevel.HARD, QuestionType.LONG_ANSWER, 1)
        );
        BlueprintRequest bpReq = new BlueprintRequest("BP-SE401-FINAL", "SE-401 Blueprint", 20, 120, PolicyMode.DYNAMIC_MULTI_POOL, rules);

        MvcResult bpResult = mockMvc.perform(post("/api/v1/authority/subjects/" + subjectId + "/blueprints")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(bpReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.blueprintCode").value("BP-SE401-FINAL"))
                .andReturn();
        String blueprintId = extractField(bpResult.getResponse().getContentAsString(), "id");

        // 5. Generate Paper SET_A
        MvcResult genResult = mockMvc.perform(post("/api/v1/authority/blueprints/" + blueprintId + "/generate")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new GeneratePaperRequest("SET_A"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.paperId").value("PAP-BP-SE401-FINAL-SET_A"))
                .andExpect(jsonPath("$.data.totalMarks").value(20))
                .andExpect(jsonPath("$.data.questionCount").value(3))
                .andExpect(jsonPath("$.data.paperHash").isString())
                .andReturn();

        String responseJson = genResult.getResponse().getContentAsString();
        String paperHash = extractField(responseJson, "paperHash");
        assertThat(paperHash).hasSize(64);

        // 6. Duplicate Prevention Verification
        // Retrieve paper details and verify questions are distinct
        MvcResult detailsResult = mockMvc.perform(get("/api/v1/authority/papers/PAP-BP-SE401-FINAL-SET_A")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY"))))
                .andExpect(status().isOk())
                .andReturn();

        String detailsJson = detailsResult.getResponse().getContentAsString();
        Set<String> extractedQuestionIds = extractAllQuestionIds(detailsJson);
        assertThat(extractedQuestionIds).hasSize(3); // Exactly 3 distinct questions!
    }

    @Test
    @DisplayName("Impossible Blueprint: Rejects blueprint if rule marks do not match total marks")
    void testImpossibleBlueprintMarksMismatch() throws Exception {
        // Setup Subject
        String examReq = json(new ExamRequest("EXAM-MISMATCH", "Mismatch Exam", "Desc", "2026", ExamStatus.ACTIVE));
        MvcResult examResult = mockMvc.perform(post("/api/v1/authority/exams")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(examReq))
                .andExpect(status().isCreated())
                .andReturn();
        String examId = extractField(examResult.getResponse().getContentAsString(), "id");

        String subReq = json(new SubjectRequest("SUB-MISMATCH", "Mismatch Subject", 100, 40));
        MvcResult subResult = mockMvc.perform(post("/api/v1/authority/exams/" + examId + "/subjects")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(subReq))
                .andExpect(status().isCreated())
                .andReturn();
        String subjectId = extractField(subResult.getResponse().getContentAsString(), "id");

        // Rule specifies 2 * 5 = 10 marks, but totalMarks is requested as 50 marks -> MISMATCH!
        List<BlueprintRuleDto> rules = List.of(
                new BlueprintRuleDto(1, 5, DifficultyLevel.EASY, QuestionType.MCQ, 2)
        );
        BlueprintRequest bpReq = new BlueprintRequest("BP-MISMATCH", "Mismatch Blueprint", 50, 60, PolicyMode.SINGLE_SETTER, rules);

        mockMvc.perform(post("/api/v1/authority/subjects/" + subjectId + "/blueprints")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(bpReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("does not match blueprint total marks")));
    }

    @Test
    @DisplayName("Insufficient Pool: Rejects paper generation when pool has fewer questions than requested")
    void testInsufficientPoolRejection() throws Exception {
        // Setup Subject
        String examReq = json(new ExamRequest("EXAM-INSUFF", "Insuff Exam", "Desc", "2026", ExamStatus.ACTIVE));
        MvcResult examResult = mockMvc.perform(post("/api/v1/authority/exams")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(examReq))
                .andExpect(status().isCreated())
                .andReturn();
        String examId = extractField(examResult.getResponse().getContentAsString(), "id");

        String subReq = json(new SubjectRequest("SUB-INSUFF", "Insuff Subject", 25, 10));
        MvcResult subResult = mockMvc.perform(post("/api/v1/authority/exams/" + examId + "/subjects")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(subReq))
                .andExpect(status().isCreated())
                .andReturn();
        String subjectId = extractField(subResult.getResponse().getContentAsString(), "id");

        // Create blueprint requesting 5 questions of 5 marks
        List<BlueprintRuleDto> rules = List.of(
                new BlueprintRuleDto(1, 5, DifficultyLevel.MEDIUM, QuestionType.SHORT_ANSWER, 5)
        );
        BlueprintRequest bpReq = new BlueprintRequest("BP-INSUFF", "Insuff BP", 25, 90, PolicyMode.DYNAMIC_MULTI_POOL, rules);

        MvcResult bpResult = mockMvc.perform(post("/api/v1/authority/subjects/" + subjectId + "/blueprints")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(bpReq)))
                .andExpect(status().isCreated())
                .andReturn();
        String blueprintId = extractField(bpResult.getResponse().getContentAsString(), "id");

        // Attempt paper generation with empty pool -> 400 Bad Request
        mockMvc.perform(post("/api/v1/authority/blueprints/" + blueprintId + "/generate")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EXAM_AUTHORITY")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new GeneratePaperRequest("SET_X"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Insufficient pool")));
    }

    @Test
    @DisplayName("RBAC: STUDENT and PAPER_SETTER cannot trigger paper generation")
    void testGenerationRbac() throws Exception {
        UUID dummyBpId = UUID.randomUUID();

        // STUDENT receives 403
        mockMvc.perform(post("/api/v1/authority/blueprints/" + dummyBpId + "/generate")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new GeneratePaperRequest("SET_A"))))
                .andExpect(status().isForbidden());

        // PAPER_SETTER receives 403
        mockMvc.perform(post("/api/v1/authority/blueprints/" + dummyBpId + "/generate")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAPER_SETTER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new GeneratePaperRequest("SET_A"))))
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

    private static Set<String> extractAllQuestionIds(String json) {
        Set<String> ids = new HashSet<>();
        String pattern = "\"questionId\":\"";
        int pos = 0;
        while ((pos = json.indexOf(pattern, pos)) != -1) {
            pos += pattern.length();
            int end = json.indexOf("\"", pos);
            if (end != -1) {
                ids.add(json.substring(pos, end));
                pos = end;
            }
        }
        return ids;
    }
}
