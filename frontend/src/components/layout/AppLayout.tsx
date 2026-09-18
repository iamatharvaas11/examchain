import { Outlet, Link, useNavigate } from 'react-router-dom';
import { Shield, User, LogOut, RefreshCw, KeyRound } from 'lucide-react';
import { useAuth } from '../../features/auth/context/AuthContext';

export function AppLayout() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex flex-col bg-slate-50">
      <header className="bg-white border-b border-slate-200 sticky top-0 z-10">
        <div className="container mx-auto px-4 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 text-primary">
            <Shield className="h-8 w-8" />
            <span className="font-bold text-xl tracking-tight">EXAMCHAIN</span>
          </Link>
          
          <div className="flex items-center gap-3">
            {isAuthenticated && user ? (
              <div className="flex items-center gap-3">
                <div className="hidden sm:flex items-center gap-2 bg-slate-100/80 border border-slate-200 px-3 py-1.5 rounded-lg text-xs">
                  <User className="w-3.5 h-3.5 text-slate-500" />
                  <span className="font-semibold text-slate-800">{user.displayName}</span>
                  <span className="bg-primary text-white text-[10px] font-bold px-1.5 py-0.5 rounded">
                    {user.roles[0]}
                  </span>
                </div>
                
                <button
                  onClick={() => navigate('/login')}
                  title="Switch Mock Role"
                  className="flex items-center gap-1 text-xs font-medium text-slate-600 bg-white border border-slate-300 hover:bg-slate-50 px-2.5 py-1.5 rounded-lg transition"
                >
                  <RefreshCw className="w-3.5 h-3.5" />
                  <span className="hidden md:inline">Switch Role</span>
                </button>

                <button
                  onClick={() => {
                    logout();
                    navigate('/login');
                  }}
                  title="Sign Out"
                  className="flex items-center gap-1 text-xs font-medium text-red-600 bg-red-50 border border-red-200 hover:bg-red-100 px-2.5 py-1.5 rounded-lg transition"
                >
                  <LogOut className="w-3.5 h-3.5" />
                  <span className="hidden md:inline">Sign Out</span>
                </button>
              </div>
            ) : (
              <Link
                to="/login"
                className="flex items-center gap-1.5 text-xs font-semibold text-white bg-primary hover:bg-primary/90 px-3.5 py-1.5 rounded-lg transition shadow-sm"
              >
                <KeyRound className="w-3.5 h-3.5" />
                Sign In
              </Link>
            )}

            <span className="hidden lg:inline-block text-[11px] font-mono px-2 py-0.5 rounded bg-slate-100 text-slate-500 border border-slate-200">
              Phase 2 RBAC
            </span>
          </div>
        </div>
      </header>
      
      <main className="flex-1 container mx-auto px-4 py-8">
        <Outlet />
      </main>

      <footer className="bg-white border-t border-slate-200 py-6">
        <div className="container mx-auto px-4 text-center text-sm text-slate-500">
          &copy; {new Date().getFullYear()} EXAMCHAIN. Adaptive Zero-Trust Examination Infrastructure.
        </div>
      </footer>
    </div>
  );
}
