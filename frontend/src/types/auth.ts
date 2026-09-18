/**
 * Canonical 8-role RBAC taxonomy for EXAMCHAIN Zero-Trust Architecture.
 */
export type UserRole =
  | 'SUPER_ADMIN'
  | 'EXAM_AUTHORITY'
  | 'CONTROLLER'
  | 'PAPER_SETTER'
  | 'CENTRE_ADMIN'
  | 'EXAM_OPERATOR'
  | 'AUDITOR'
  | 'STUDENT';

export interface UserProfile {
  userId: string;
  username: string;
  email: string;
  displayName: string;
  roles: UserRole[];
}

export interface AuthState {
  user: UserProfile | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  token: string | null;
}

export interface AuthContextType extends AuthState {
  login: (role: UserRole) => void;
  logout: () => void;
  hasRole: (requiredRoles: UserRole | UserRole[]) => boolean;
}

export interface RoleConfig {
  role: UserRole;
  title: string;
  description: string;
  color: string;
  homePath: string;
}

export const CANONICAL_ROLES: RoleConfig[] = [
  {
    role: 'SUPER_ADMIN',
    title: 'Super Administrator',
    description: 'Platform infrastructure, node registry, and global policies',
    color: 'bg-red-50 text-red-700 border-red-200',
    homePath: '/admin',
  },
  {
    role: 'EXAM_AUTHORITY',
    title: 'Exam Authority',
    description: 'Exam creation, blueprint approval, and release authorization',
    color: 'bg-purple-50 text-purple-700 border-purple-200',
    homePath: '/authority',
  },
  {
    role: 'CONTROLLER',
    title: 'Controller of Examinations',
    description: 'Paper release approval, incident response, and time-lock management',
    color: 'bg-indigo-50 text-indigo-700 border-indigo-200',
    homePath: '/authority',
  },
  {
    role: 'PAPER_SETTER',
    title: 'Paper Setter',
    description: 'Blind question authoring and encrypted submission',
    color: 'bg-blue-50 text-blue-700 border-blue-200',
    homePath: '/setter',
  },
  {
    role: 'CENTRE_ADMIN',
    title: 'Centre Administrator',
    description: 'Exam centre operations, operator authorization, and custody oversight',
    color: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    homePath: '/centre',
  },
  {
    role: 'EXAM_OPERATOR',
    title: 'Exam Operator',
    description: 'Two-man OTP decryption and local print/display coordination',
    color: 'bg-teal-50 text-teal-700 border-teal-200',
    homePath: '/centre',
  },
  {
    role: 'AUDITOR',
    title: 'Independent Auditor',
    description: 'Chain of custody verification, fairness proofs, and immutable audit trails',
    color: 'bg-amber-50 text-amber-700 border-amber-200',
    homePath: '/audit',
  },
  {
    role: 'STUDENT',
    title: 'Student / Candidate',
    description: 'QR paper authenticity verification and exam candidate view',
    color: 'bg-sky-50 text-sky-700 border-sky-200',
    homePath: '/student',
  },
];
