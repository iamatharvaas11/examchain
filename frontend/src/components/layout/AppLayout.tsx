import { Outlet, Link } from 'react-router-dom';
import { Shield } from 'lucide-react';

export function AppLayout() {
  return (
    <div className="min-h-screen flex flex-col bg-slate-50">
      <header className="bg-white border-b border-slate-200 sticky top-0 z-10">
        <div className="container mx-auto px-4 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 text-primary">
            <Shield className="h-8 w-8" />
            <span className="font-bold text-xl tracking-tight">EXAMCHAIN</span>
          </Link>
          <div className="flex items-center gap-4">
            <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-slate-100 text-slate-600 border border-slate-200">
              v1.0.0
            </span>
          </div>
        </div>
      </header>
      
      <main className="flex-1 container mx-auto px-4 py-8">
        <Outlet />
      </main>

      <footer className="bg-white border-t border-slate-200 py-6">
        <div className="container mx-auto px-4 text-center text-sm text-slate-500">
          &copy; {new Date().getFullYear()} EXAMCHAIN. All rights reserved.
        </div>
      </footer>
    </div>
  );
}
