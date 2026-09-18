import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ShieldAlert, ArrowLeft, RefreshCw } from 'lucide-react';

export const UnauthorizedPage: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="max-w-xl mx-auto py-16 text-center">
      <div className="inline-flex items-center justify-center p-4 bg-red-100 rounded-full text-red-600 mb-6">
        <ShieldAlert className="w-16 h-16" />
      </div>
      <h1 className="text-4xl font-extrabold text-slate-900 tracking-tight">403 Forbidden</h1>
      <p className="mt-2 text-lg font-semibold text-red-700">Least-Privilege Authorization Enforcement</p>
      <p className="mt-4 text-slate-600">
        Your current role does not have authorization to view or execute operations on this secure endpoint.
      </p>

      {user && (
        <div className="mt-6 p-4 bg-white border border-slate-200 rounded-xl text-left shadow-sm">
          <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Active Session Identity</div>
          <div className="text-sm font-medium text-slate-900">User ID: <span className="font-mono text-slate-600">{user.userId}</span></div>
          <div className="text-sm font-medium text-slate-900">Username: <span className="font-mono text-slate-600">{user.username}</span></div>
          <div className="text-sm font-medium text-slate-900 mt-1">
            Current Roles:{' '}
            {user.roles.map(r => (
              <span key={r} className="inline-block bg-red-50 text-red-700 border border-red-200 text-xs px-2 py-0.5 rounded font-mono ml-1">
                {r}
              </span>
            ))}
          </div>
        </div>
      )}

      <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-3">
        <Link
          to="/dashboard"
          className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-5 py-2.5 bg-primary text-white rounded-lg font-medium hover:bg-primary/90 transition"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Dashboard
        </Link>
        <button
          onClick={() => {
            logout();
            navigate('/login');
          }}
          className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-5 py-2.5 bg-white border border-slate-300 text-slate-700 rounded-lg font-medium hover:bg-slate-50 transition"
        >
          <RefreshCw className="w-4 h-4" />
          Switch Role / Re-authenticate
        </button>
      </div>
    </div>
  );
};
