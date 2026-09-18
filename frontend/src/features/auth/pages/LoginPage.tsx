import React from 'react';
import { useNavigate, useLocation, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { CANONICAL_ROLES, UserRole } from '../../../types/auth';
import { Shield, KeyRound, ArrowRight, UserCheck } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const { user, isAuthenticated, login, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname;

  const handleRoleSelect = (role: UserRole, homePath: string) => {
    login(role);
    const destination = from || homePath;
    navigate(destination, { replace: true });
  };

  if (isAuthenticated && user && !from) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <div className="max-w-4xl mx-auto py-8">
      <div className="text-center mb-8">
        <div className="inline-flex items-center justify-center p-3 bg-primary/10 rounded-2xl text-primary mb-4">
          <Shield className="w-12 h-12" />
        </div>
        <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">EXAMCHAIN Identity Portal</h1>
        <p className="mt-2 text-slate-600 max-w-lg mx-auto">
          Adaptive Zero-Trust Role-Based Access Control. Select a canonical examination authority role below to simulate authenticated session tokens.
        </p>
      </div>

      {isAuthenticated && user && (
        <div className="mb-8 p-4 bg-amber-50 border border-amber-200 rounded-xl flex items-center justify-between">
          <div className="flex items-center gap-3">
            <UserCheck className="w-5 h-5 text-amber-600" />
            <span className="text-sm font-medium text-amber-800">
              Currently authenticated as <strong>{user.displayName}</strong> ({user.roles.join(', ')})
            </span>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => navigate('/dashboard')}
              className="text-xs bg-amber-600 text-white px-3 py-1.5 rounded-lg hover:bg-amber-700 transition"
            >
              Go to Dashboard
            </button>
            <button
              onClick={logout}
              className="text-xs bg-white text-slate-700 border border-slate-300 px-3 py-1.5 rounded-lg hover:bg-slate-50 transition"
            >
              Sign Out
            </button>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {CANONICAL_ROLES.map(roleConfig => (
          <div
            key={roleConfig.role}
            className="border border-slate-200 bg-white rounded-xl p-5 hover:border-primary/50 hover:shadow-md transition cursor-pointer flex flex-col justify-between"
            onClick={() => handleRoleSelect(roleConfig.role, roleConfig.homePath)}
          >
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className={`text-xs font-semibold px-2.5 py-0.5 rounded-full border ${roleConfig.color}`}>
                  {roleConfig.role}
                </span>
                <KeyRound className="w-4 h-4 text-slate-400" />
              </div>
              <h3 className="font-semibold text-slate-900 text-lg">{roleConfig.title}</h3>
              <p className="text-sm text-slate-500 mt-1">{roleConfig.description}</p>
            </div>
            <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-primary font-medium">
              <span>Sign in as {roleConfig.role}</span>
              <ArrowRight className="w-4 h-4" />
            </div>
          </div>
        ))}
      </div>

      <div className="mt-8 p-4 bg-slate-100 rounded-xl text-xs text-slate-500 text-center">
        <p><strong>Note for Phase 2 Evaluation:</strong> In local development, the Identity Switcher injects valid mock OIDC JWT credentials mapping to the Keycloak Realm roles defined in the EXAMCHAIN RBAC matrix.</p>
      </div>
    </div>
  );
};
