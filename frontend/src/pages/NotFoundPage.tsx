import { Link } from 'react-router-dom';
import { FileQuestion } from 'lucide-react';

export function NotFoundPage() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] text-center space-y-6">
      <div className="p-4 bg-slate-100 rounded-full">
        <FileQuestion className="h-12 w-12 text-slate-400" />
      </div>
      <div className="space-y-2">
        <h1 className="text-3xl font-bold text-slate-900">Page Not Found</h1>
        <p className="text-slate-500">The page you're looking for doesn't exist or has been moved.</p>
      </div>
      <Link 
        to="/" 
        className="inline-flex items-center justify-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-primary hover:bg-primary/90 transition-colors"
      >
        Return to Home
      </Link>
    </div>
  );
}
