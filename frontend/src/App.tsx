import { Routes, Route } from 'react-router-dom';
import { AuthProvider } from './features/auth/context/AuthContext';
import { AppLayout } from './components/layout/AppLayout';
import { HomePage } from './pages/HomePage';
import { NotFoundPage } from './pages/NotFoundPage';
import { LoginPage } from './features/auth/pages/LoginPage';
import { UnauthorizedPage } from './features/auth/pages/UnauthorizedPage';
import { DashboardPage } from './features/dashboard/pages/DashboardPage';
import { ProtectedRoute } from './features/auth/components/ProtectedRoute';
import {
  AdminPortalPage,
  AuthorityPortalPage,
  SetterPortalPage,
  CentrePortalPage,
  AuditPortalPage,
  StudentPortalPage,
} from './features/portals/pages/RolePortals';

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<AppLayout />}>
          {/* Public Routes */}
          <Route index element={<HomePage />} />
          <Route path="login" element={<LoginPage />} />
          <Route path="unauthorized" element={<UnauthorizedPage />} />

          {/* Authenticated Dashboard */}
          <Route
            path="dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />

          {/* Role-Gated Domain Portals */}
          <Route
            path="admin"
            element={
              <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
                <AdminPortalPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="authority"
            element={
              <ProtectedRoute allowedRoles={['EXAM_AUTHORITY', 'CONTROLLER']}>
                <AuthorityPortalPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="setter"
            element={
              <ProtectedRoute allowedRoles={['PAPER_SETTER']}>
                <SetterPortalPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="centre"
            element={
              <ProtectedRoute allowedRoles={['CENTRE_ADMIN', 'EXAM_OPERATOR']}>
                <CentrePortalPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="audit"
            element={
              <ProtectedRoute allowedRoles={['AUDITOR', 'SUPER_ADMIN']}>
                <AuditPortalPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="student"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <StudentPortalPage />
              </ProtectedRoute>
            }
          />

          {/* Fallback */}
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </AuthProvider>
  );
}

export default App;
