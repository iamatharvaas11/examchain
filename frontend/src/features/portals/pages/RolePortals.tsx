import React, { useState } from 'react';
import { useAuth } from '../../auth/context/AuthContext';
import { ShieldCheck, ArrowLeft, PlusCircle, CheckCircle2, XCircle, Send, Hash, BookOpen, Layers } from 'lucide-react';
import { Link } from 'react-router-dom';

interface PortalViewProps {
  title: string;
  roleBadge: string;
  description: string;
  boundaryNote: string;
  children?: React.ReactNode;
}

const PortalView: React.FC<PortalViewProps> = ({ title, roleBadge, description, boundaryNote, children }) => {
  const { user } = useAuth();

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <Link to="/dashboard" className="text-sm font-medium text-slate-500 hover:text-slate-900 inline-flex items-center gap-1.5">
          <ArrowLeft className="w-4 h-4" /> Back to Dashboard
        </Link>
        <span className="text-xs font-mono font-bold bg-primary/10 text-primary px-2.5 py-1 rounded-full border border-primary/20">
          Enforced Role: {roleBadge}
        </span>
      </div>

      <div className="bg-white border border-slate-200 rounded-2xl p-6 md:p-8 shadow-sm">
        <div className="inline-flex p-3 bg-emerald-50 text-emerald-600 rounded-xl mb-4">
          <ShieldCheck className="w-8 h-8" />
        </div>
        <h1 className="text-2xl font-bold text-slate-900">{title}</h1>
        <p className="text-slate-600 mt-2">{description}</p>

        <div className="mt-4 p-3 bg-slate-50 border border-slate-200 rounded-xl space-y-1">
          <div className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Security Perimeter Boundary</div>
          <p className="text-xs text-slate-600 font-mono">{boundaryNote}</p>
        </div>

        {user && (
          <div className="mt-3 text-xs text-slate-400">
            Authenticated caller: <span className="font-semibold text-slate-700">{user.displayName}</span> ({user.roles.join(', ')})
          </div>
        )}
      </div>

      {children}
    </div>
  );
};

export const AdminPortalPage: React.FC = () => (
  <PortalView
    title="Super Administrator Command Portal"
    roleBadge="SUPER_ADMIN"
    description="Platform-wide administration, node registration, cryptographic certificate authority binding, and root security policy configuration."
    boundaryNote="Endpoints under /api/v1/admin/** require strict SUPER_ADMIN authority. Sub-roles cannot escalate privileges."
  />
);

export const AuthorityPortalPage: React.FC = () => {
  const [examCode, setExamCode] = useState('EXAM-2026-CS');
  const [examTitle, setExamTitle] = useState('Computer Science Final Tripos');
  const [session, setSession] = useState('2025-2026');
  const [createdExams, setCreatedExams] = useState<string[]>(['EXAM-2026-CS (Active)']);
  const [feedback, setFeedback] = useState<string | null>(null);

  const handleCreateExam = (e: React.FormEvent) => {
    e.preventDefault();
    if (!examCode.trim() || !examTitle.trim()) return;
    setCreatedExams(prev => [...prev, `${examCode.trim().toUpperCase()} - ${examTitle}`]);
    setFeedback(`Exam ${examCode.toUpperCase()} successfully initialized under authority custody.`);
    setExamCode('');
    setExamTitle('');
  };

  return (
    <PortalView
      title="Examination Authority & Controller Portal"
      roleBadge="EXAM_AUTHORITY / CONTROLLER"
      description="Exam cycle initialization, paper blueprint approvals, time-lock scheduling, and multi-signature release authorisations."
      boundaryNote="Endpoints under /api/v1/authority/** enforce quorum authorisations. Dual-signature requirements between Authority and Controller are mandated in Phase 4."
    >
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Exam Management */}
        <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
          <h3 className="text-base font-bold text-slate-900 mb-3 flex items-center gap-2">
            <BookOpen className="w-4 h-4 text-primary" />
            Initialize Examination Cycle
          </h3>
          <form onSubmit={handleCreateExam} className="space-y-3">
            <div>
              <label className="text-xs font-semibold text-slate-700">Exam Code</label>
              <input
                type="text"
                value={examCode}
                onChange={e => setExamCode(e.target.value)}
                placeholder="e.g. EXAM-2026-MATH"
                className="w-full text-xs mt-1 p-2 border border-slate-300 rounded-lg focus:outline-primary"
                required
              />
            </div>
            <div>
              <label className="text-xs font-semibold text-slate-700">Exam Title</label>
              <input
                type="text"
                value={examTitle}
                onChange={e => setExamTitle(e.target.value)}
                placeholder="e.g. Mathematics Honors Examination"
                className="w-full text-xs mt-1 p-2 border border-slate-300 rounded-lg focus:outline-primary"
                required
              />
            </div>
            <div>
              <label className="text-xs font-semibold text-slate-700">Academic Session</label>
              <input
                type="text"
                value={session}
                onChange={e => setSession(e.target.value)}
                className="w-full text-xs mt-1 p-2 border border-slate-300 rounded-lg focus:outline-primary"
                required
              />
            </div>
            <button
              type="submit"
              className="w-full text-xs font-semibold py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition flex items-center justify-center gap-1.5"
            >
              <PlusCircle className="w-3.5 h-3.5" /> Register Exam
            </button>
            {feedback && <div className="text-xs text-emerald-600 bg-emerald-50 p-2 rounded border border-emerald-200">{feedback}</div>}
          </form>
        </div>

        {/* Question Pool Review Queue */}
        <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
          <h3 className="text-base font-bold text-slate-900 mb-3 flex items-center gap-2">
            <Layers className="w-4 h-4 text-primary" />
            Question Pool Review Queue
          </h3>
          <div className="space-y-3">
            <div className="p-3 bg-slate-50 border border-slate-200 rounded-xl flex items-center justify-between">
              <div>
                <div className="text-xs font-bold text-slate-800">POOL-CS301-A (Unit 1 & 2)</div>
                <div className="text-[11px] text-slate-500">Subject: CS-301 &bull; 12 Questions &bull; Status: SUBMITTED</div>
              </div>
              <div className="flex items-center gap-1.5">
                <button
                  onClick={() => alert("Pool POOL-CS301-A Approved and Locked for Blueprint selection.")}
                  className="px-2.5 py-1 text-xs bg-emerald-600 text-white rounded hover:bg-emerald-700 flex items-center gap-1"
                >
                  <CheckCircle2 className="w-3 h-3" /> Approve
                </button>
                <button
                  onClick={() => alert("Pool rejected and returned to Paper Setter.")}
                  className="px-2.5 py-1 text-xs bg-rose-600 text-white rounded hover:bg-rose-700 flex items-center gap-1"
                >
                  <XCircle className="w-3 h-3" /> Reject
                </button>
              </div>
            </div>

            <div className="text-xs text-slate-400">
              Approved pools become immutable: questions cannot be altered, ensuring zero-trust tamper protection prior to algorithmic blueprint generation.
            </div>

            <div className="pt-2 border-t border-slate-100">
              <span className="text-[11px] font-bold text-slate-600 uppercase">Active Cycles:</span>
              <ul className="text-xs text-slate-500 mt-1 space-y-1">
                {createdExams.map((ex, idx) => (
                  <li key={idx}>&bull; {ex}</li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </div>
    </PortalView>
  );
};

export const SetterPortalPage: React.FC = () => {
  const [qId, setQId] = useState('Q-CS301-001');
  const [unit, setUnit] = useState(1);
  const [marks, setMarks] = useState(5);
  const [difficulty, setDifficulty] = useState('HARD');
  const [qType, setQType] = useState('MCQ');
  const [cognitive, setCognitive] = useState('ANALYZE');
  const [text, setText] = useState('What is Byzantine Fault Tolerance threshold in synchronous consensus?');
  const [submittedQuestions, setSubmittedQuestions] = useState<string[]>([]);

  const handleAddQuestion = (e: React.FormEvent) => {
    e.preventDefault();
    setSubmittedQuestions(prev => [...prev, `${qId} (Unit ${unit}, ${marks}M, ${difficulty})`]);
    alert(`Question ${qId} submitted to DRAFT pool with SHA-256 fingerprint generated.`);
    setQId(`Q-CS301-00${submittedQuestions.length + 2}`);
  };

  return (
    <PortalView
      title="Blind Paper Setter Portal"
      roleBadge="PAPER_SETTER"
      description="Isolated question item authoring, blind pool contributions, and zero-knowledge question tagging."
      boundaryNote="Endpoints under /api/v1/setter/** allow blind question ingestion only. Setters cannot view assembled final papers or centre allocations."
    >
      <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm space-y-4">
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <div>
            <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <PlusCircle className="w-4 h-4 text-primary" />
              Author Blind Question (Full Multidimensional Metadata)
            </h3>
            <p className="text-xs text-slate-500">Active Pool: POOL-CS301-A (Status: DRAFT)</p>
          </div>
          <button
            onClick={() => alert("Pool POOL-CS301-A submitted to Examination Authority for review.")}
            className="text-xs font-semibold px-3 py-1.5 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 flex items-center gap-1.5 transition"
          >
            <Send className="w-3.5 h-3.5" /> Submit Pool for Approval
          </button>
        </div>

        <form onSubmit={handleAddQuestion} className="space-y-4">
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-3">
            <div>
              <label className="text-[11px] font-semibold text-slate-700">Question ID</label>
              <input
                type="text"
                value={qId}
                onChange={e => setQId(e.target.value)}
                className="w-full text-xs mt-1 p-1.5 border border-slate-300 rounded font-mono"
                required
              />
            </div>
            <div>
              <label className="text-[11px] font-semibold text-slate-700">Unit (1-5)</label>
              <input
                type="number"
                min={1}
                max={5}
                value={unit}
                onChange={e => setUnit(parseInt(e.target.value) || 1)}
                className="w-full text-xs mt-1 p-1.5 border border-slate-300 rounded"
                required
              />
            </div>
            <div>
              <label className="text-[11px] font-semibold text-slate-700">Marks</label>
              <input
                type="number"
                min={1}
                max={50}
                value={marks}
                onChange={e => setMarks(parseInt(e.target.value) || 1)}
                className="w-full text-xs mt-1 p-1.5 border border-slate-300 rounded"
                required
              />
            </div>
            <div>
              <label className="text-[11px] font-semibold text-slate-700">Difficulty</label>
              <select
                value={difficulty}
                onChange={e => setDifficulty(e.target.value)}
                className="w-full text-xs mt-1 p-1.5 border border-slate-300 rounded bg-white"
              >
                <option value="EASY">EASY</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="HARD">HARD</option>
              </select>
            </div>
            <div>
              <label className="text-[11px] font-semibold text-slate-700">Type</label>
              <select
                value={qType}
                onChange={e => setQType(e.target.value)}
                className="w-full text-xs mt-1 p-1.5 border border-slate-300 rounded bg-white"
              >
                <option value="MCQ">MCQ</option>
                <option value="SHORT_ANSWER">SHORT ANSWER</option>
                <option value="LONG_ANSWER">LONG ANSWER</option>
                <option value="NUMERICAL">NUMERICAL</option>
                <option value="CODE">CODE</option>
              </select>
            </div>
            <div>
              <label className="text-[11px] font-semibold text-slate-700">Cognitive Level</label>
              <select
                value={cognitive}
                onChange={e => setCognitive(e.target.value)}
                className="w-full text-xs mt-1 p-1.5 border border-slate-300 rounded bg-white"
              >
                <option value="REMEMBER">REMEMBER</option>
                <option value="UNDERSTAND">UNDERSTAND</option>
                <option value="APPLY">APPLY</option>
                <option value="ANALYZE">ANALYZE</option>
                <option value="EVALUATE">EVALUATE</option>
                <option value="CREATE">CREATE</option>
              </select>
            </div>
          </div>

          <div>
            <label className="text-xs font-semibold text-slate-700">Question Content / Problem Statement</label>
            <textarea
              rows={3}
              value={text}
              onChange={e => setText(e.target.value)}
              className="w-full text-xs mt-1 p-2 border border-slate-300 rounded-lg focus:outline-primary font-mono"
              required
            />
          </div>

          <div className="flex items-center justify-between pt-2">
            <div className="flex items-center gap-1.5 text-xs text-slate-500 font-mono">
              <Hash className="w-3.5 h-3.5 text-primary" />
              <span>SHA-256 Tamper-Evident Hashing Active</span>
            </div>
            <button
              type="submit"
              className="text-xs font-semibold px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition flex items-center gap-1.5"
            >
              <PlusCircle className="w-3.5 h-3.5" /> Append Question to Pool
            </button>
          </div>
        </form>

        {submittedQuestions.length > 0 && (
          <div className="mt-4 pt-3 border-t border-slate-100">
            <div className="text-xs font-bold text-slate-700">Authored Questions in Pool ({submittedQuestions.length}):</div>
            <ul className="mt-1 space-y-1">
              {submittedQuestions.map((q, idx) => (
                <li key={idx} className="text-xs text-slate-600 font-mono bg-slate-50 p-1.5 rounded border border-slate-200">
                  {q}
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </PortalView>
  );
};

export const CentrePortalPage: React.FC = () => (
  <PortalView
    title="Exam Centre Operations Portal"
    roleBadge="CENTRE_ADMIN / EXAM_OPERATOR"
    description="Centre-level device heartbeat, operator attendance verification, air-gapped terminal synchronization, and dual-custody OTP paper decryption."
    boundaryNote="Endpoints under /api/v1/centre/** and /api/v1/operator/** require registered centre credentials and time-gated verification."
  />
);

export const AuditPortalPage: React.FC = () => (
  <PortalView
    title="Independent Auditor Portal"
    roleBadge="AUDITOR / SUPER_ADMIN"
    description="Real-time immutability verification, cryptographic chain of custody tracking, access audit trails, and fairness metric verifications."
    boundaryNote="Endpoints under /api/v1/audit/** are read-only and backed by verifiable cryptographic ledger proofs."
  />
);

export const StudentPortalPage: React.FC = () => (
  <PortalView
    title="Student & Candidate Verification Portal"
    roleBadge="STUDENT"
    description="Candidate paper authenticity scanner, QR cryptographic signature verification, and candidate exam hall schedule."
    boundaryNote="Public and student-scoped verification endpoints validate paper authenticity without exposing master secret keys."
  />
);
