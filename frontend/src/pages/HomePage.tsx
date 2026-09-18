import { useEffect, useState } from 'react';
import { fetchHealth } from '@/services/api/health';
import type { HealthStatus } from '@/types/api';
import { Activity, ShieldCheck, ServerCrash } from 'lucide-react';

export function HomePage() {
  const [health, setHealth] = useState<HealthStatus | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkHealth = async () => {
      try {
        const data = await fetchHealth();
        setHealth(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to connect to backend');
      } finally {
        setLoading(false);
      }
    };

    checkHealth();
  }, []);

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <section className="text-center space-y-4 py-12">
        <h1 className="text-4xl md:text-5xl font-extrabold text-slate-900 tracking-tight">
          Welcome to <span className="text-primary">EXAMCHAIN</span>
        </h1>
        <p className="text-xl text-slate-600 max-w-2xl mx-auto">
          The next-generation secure examination platform built on blockchain technology for unparalleled transparency and trust.
        </p>
        <div className="pt-2">
          <a
            href="/login"
            className="inline-flex items-center gap-2 px-5 py-2.5 bg-primary text-white rounded-xl font-semibold shadow-sm hover:bg-primary/90 transition text-sm"
          >
            Access Identity & Role Portals &rarr;
          </a>
        </div>
      </section>

      <section className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
        <div className="flex items-center gap-2 mb-6">
          <Activity className="h-6 w-6 text-slate-500" />
          <h2 className="text-xl font-semibold text-slate-800">System Status</h2>
        </div>

        {loading ? (
          <div className="animate-pulse flex space-x-4">
            <div className="flex-1 space-y-4 py-1">
              <div className="h-4 bg-slate-200 rounded w-3/4"></div>
              <div className="space-y-2">
                <div className="h-4 bg-slate-200 rounded"></div>
                <div className="h-4 bg-slate-200 rounded w-5/6"></div>
              </div>
            </div>
          </div>
        ) : error ? (
          <div className="flex items-start gap-4 p-4 bg-danger/10 text-danger rounded-lg border border-danger/20">
            <ServerCrash className="h-6 w-6 flex-shrink-0 mt-0.5" />
            <div>
              <h3 className="font-medium">System Disconnected</h3>
              <p className="text-sm mt-1 opacity-90">{error}</p>
            </div>
          </div>
        ) : (
          <div className="flex items-start gap-4 p-4 bg-accent/10 text-accent rounded-lg border border-accent/20">
            <ShieldCheck className="h-6 w-6 flex-shrink-0 mt-0.5" />
            <div className="w-full">
              <h3 className="font-medium">System Online</h3>
              <div className="grid grid-cols-2 gap-4 mt-3 text-sm opacity-90">
                <div>
                  <span className="font-semibold">Application:</span> {health?.application}
                </div>
                <div>
                  <span className="font-semibold">Version:</span> {health?.version}
                </div>
                <div>
                  <span className="font-semibold">Database:</span> {health?.database}
                </div>
                <div>
                  <span className="font-semibold">Last Checked:</span>{' '}
                  {health?.timestamp ? new Date(health.timestamp).toLocaleTimeString() : 'N/A'}
                </div>
              </div>
            </div>
          </div>
        )}
      </section>
    </div>
  );
}
