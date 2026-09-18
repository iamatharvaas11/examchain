import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../auth/context/AuthContext';
import { CANONICAL_ROLES, UserRole } from '../../../types/auth';
import { Shield, CheckCircle, Lock, ExternalLink, Terminal } from 'lucide-react';

interface ProbeResult {
  endpoint: string;
  status: number;
  message: string;
  success: boolean;
}

export const DashboardPage: React.FC = () => {
  const { user, hasRole, token } = useAuth();
  const [probeResults, setProbeResults] = useState<Record<string, ProbeResult>>({});
  const [isProbing, setIsProbing] = useState<string | null>(null);

  if (!user) return null;

  const testBackendProbe = async (endpoint: string) => {
    setIsProbing(endpoint);
    try {
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
      };
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      const res = await fetch(endpoint, { headers });
      const data = await res.json().catch(() => ({ message: res.statusText }));

      setProbeResults(prev => ({
        ...prev,
        [endpoint]: {
          endpoint,
          status: res.status,
          message: data?.data || data?.message || `HTTP ${res.status}`,
          success: res.ok,
        },
      }));
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : 'Connection failed';
      setProbeResults(prev => ({
        ...prev,
        [endpoint]: {
          endpoint,
          status: 0,
          message: `Network Error: ${errorMsg}`,
          success: false,
        },
      }));
    } finally {
      setIsProbing(null);
    }
  };

  const PROBES: { name: string; path: string; roles: UserRole[] }[] = [
    { name: 'Admin Probe', path: '/api/v1/admin/ping', roles: ['SUPER_ADMIN'] },
    { name: 'Authority Probe', path: '/api/v1/authority/ping', roles: ['EXAM_AUTHORITY', 'CONTROLLER'] },
    { name: 'Paper Setter Probe', path: '/api/v1/setter/ping', roles: ['PAPER_SETTER'] },
    { name: 'Centre Probe', path: '/api/v1/centre/ping', roles: ['CENTRE_ADMIN', 'EXAM_OPERATOR'] },
    { name: 'Auditor Probe', path: '/api/v1/audit/ping', roles: ['AUDITOR', 'SUPER_ADMIN'] },
    { name: 'Student Probe', path: '/api/v1/student/ping', roles: ['STUDENT'] },
  ];

  return (
    <div className="max-w-6xl mx-auto space-y-8">
      {/* Identity Banner */}
      <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <div className="w-14 h-14 bg-primary/10 text-primary rounded-xl flex items-center justify-center font-bold text-xl">
            {user.displayName.charAt(0)}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-2xl font-bold text-slate-900">{user.displayName}</h2>
              <span className="text-xs bg-emerald-50 text-emerald-700 border border-emerald-200 font-semibold px-2 py-0.5 rounded-full flex items-center gap-1">
                <CheckCircle className="w-3 h-3" /> Authenticated
              </span>
            </div>
            <p className="text-sm text-slate-500 font-mono mt-0.5">
              {user.username} &bull; {user.email} &bull; ID: {user.userId}
            </p>
          </div>
        </div>
        <div className="flex flex-wrap gap-2">
          {user.roles.map(r => (
            <span key={r} className="text-xs font-mono font-bold bg-slate-900 text-white px-3 py-1 rounded-lg">
              {r}
            </span>
          ))}
        </div>
      </div>

      {/* Role Navigation Matrix */}
      <div>
        <h3 className="text-lg font-bold text-slate-900 mb-4 flex items-center gap-2">
          <Shield className="w-5 h-5 text-primary" />
          Role-Gated Portals (Least-Privilege Routing)
        </h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {CANONICAL_ROLES.map(rc => {
            const authorized = hasRole(rc.role) || (rc.role === 'AUDITOR' && hasRole('SUPER_ADMIN'));
            return (
              <div
                key={rc.role}
                className={`border rounded-xl p-4 flex flex-col justify-between transition ${
                  authorized ? 'bg-white border-slate-200 hover:border-primary/60 shadow-sm' : 'bg-slate-50 border-slate-200 opacity-60'
                }`}
              >
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded ${rc.color}`}>
                      {rc.role}
                    </span>
                    {authorized ? (
                      <span className="text-[10px] text-emerald-600 font-bold bg-emerald-50 px-1.5 py-0.5 rounded">
                        Authorized
                      </span>
                    ) : (
                      <span className="text-[10px] text-slate-400 font-bold flex items-center gap-1">
                        <Lock className="w-3 h-3" /> Locked
                      </span>
                    )}
                  </div>
                  <h4 className="font-semibold text-slate-800 text-sm">{rc.title}</h4>
                  <p className="text-xs text-slate-500 mt-1 line-clamp-2">{rc.description}</p>
                </div>

                <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
                  <Link
                    to={rc.homePath}
                    className={`text-xs font-medium inline-flex items-center gap-1 ${
                      authorized ? 'text-primary hover:underline' : 'text-slate-400 hover:text-red-500'
                    }`}
                  >
                    <span>{authorized ? 'Enter Portal' : 'Test Route Guard'}</span>
                    <ExternalLink className="w-3 h-3" />
                  </Link>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Live Backend RBAC Probe Verification */}
      <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2">
              <Terminal className="w-5 h-5 text-primary" />
              Live Backend RBAC Probes
            </h3>
            <p className="text-xs text-slate-500 mt-0.5">
              Verify Spring Security authorization filter chains by sending requests to backend probe endpoints with your active credentials.
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
          {PROBES.map(probe => {
            const authorized = probe.roles.some(r => user.roles.includes(r));
            const result = probeResults[probe.path];
            return (
              <div key={probe.path} className="border border-slate-200 rounded-xl p-3 bg-slate-50/50">
                <div className="flex items-center justify-between mb-1.5">
                  <span className="text-xs font-semibold text-slate-800">{probe.name}</span>
                  <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${authorized ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700'}`}>
                    {authorized ? 'Permitted' : 'Denied'}
                  </span>
                </div>
                <div className="text-[11px] font-mono text-slate-500 truncate mb-2">{probe.path}</div>
                <button
                  onClick={() => testBackendProbe(probe.path)}
                  disabled={isProbing === probe.path}
                  className="w-full text-xs font-medium py-1 px-2.5 rounded bg-white border border-slate-300 hover:bg-slate-100 transition flex items-center justify-center gap-1.5"
                >
                  {isProbing === probe.path ? 'Querying...' : 'Dispatch Probe'}
                </button>
                {result && (
                  <div className={`mt-2 p-2 rounded text-[11px] font-mono ${result.success ? 'bg-emerald-50 text-emerald-800 border border-emerald-200' : 'bg-red-50 text-red-800 border border-red-200'}`}>
                    HTTP {result.status}: {result.message}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
