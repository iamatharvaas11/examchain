import React from 'react';
import { useAuth } from '../../auth/context/AuthContext';
import { ShieldCheck, ArrowLeft } from 'lucide-react';
import { Link } from 'react-router-dom';

interface PortalViewProps {
  title: string;
  roleBadge: string;
  description: string;
  boundaryNote: string;
}

const PortalView: React.FC<PortalViewProps> = ({ title, roleBadge, description, boundaryNote }) => {
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

      <div className="bg-white border border-slate-200 rounded-2xl p-8 shadow-sm">
        <div className="inline-flex p-3 bg-emerald-50 text-emerald-600 rounded-xl mb-4">
          <ShieldCheck className="w-8 h-8" />
        </div>
        <h1 className="text-2xl font-bold text-slate-900">{title}</h1>
        <p className="text-slate-600 mt-2">{description}</p>

        <div className="mt-6 p-4 bg-slate-50 border border-slate-200 rounded-xl space-y-2">
          <div className="text-xs font-bold uppercase tracking-wider text-slate-400">Security Perimeter Boundary</div>
          <p className="text-xs text-slate-600 font-mono">{boundaryNote}</p>
        </div>

        {user && (
          <div className="mt-4 text-xs text-slate-400">
            Authenticated caller: <span className="font-semibold text-slate-700">{user.displayName}</span> ({user.roles.join(', ')})
          </div>
        )}
      </div>
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

export const AuthorityPortalPage: React.FC = () => (
  <PortalView
    title="Examination Authority & Controller Portal"
    roleBadge="EXAM_AUTHORITY / CONTROLLER"
    description="Exam cycle initialization, paper blueprint approvals, time-lock scheduling, and multi-signature release authorisations."
    boundaryNote="Endpoints under /api/v1/authority/** enforce quorum authorisations. Dual-signature requirements between Authority and Controller are mandated in Phase 4."
  />
);

export const SetterPortalPage: React.FC = () => (
  <PortalView
    title="Blind Paper Setter Portal"
    roleBadge="PAPER_SETTER"
    description="Isolated question item authoring, blind pool contributions, and zero-knowledge question tagging."
    boundaryNote="Endpoints under /api/v1/setter/** allow blind question ingestion only. Setters cannot view assembled final papers or centre allocations."
  />
);

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
