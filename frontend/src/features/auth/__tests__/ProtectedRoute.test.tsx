import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { describe, it, expect, beforeEach } from 'vitest';
import { AuthProvider, useAuth } from '../context/AuthContext';
import { ProtectedRoute } from '../components/ProtectedRoute';
import { UserRole } from '../../../types/auth';
import React, { useEffect } from 'react';

// Helper component that logs in with a specific role on mount for testing
const AuthInitializer: React.FC<{ role?: UserRole; children: React.ReactNode }> = ({ role, children }) => {
  const { login, logout } = useAuth();
  useEffect(() => {
    if (role) {
      login(role);
    } else {
      logout();
    }
  }, [role, login, logout]);

  return <>{children}</>;
};

describe('ProtectedRoute RBAC Enforcement', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('redirects unauthenticated users to /login', async () => {
    render(
      <MemoryRouter initialEntries={['/secret']}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<div>Login Portal Page</div>} />
            <Route
              path="/secret"
              element={
                <ProtectedRoute>
                  <div>Confidential Examination Content</div>
                </ProtectedRoute>
              }
            />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    expect(await screen.findByText('Login Portal Page')).toBeInTheDocument();
    expect(screen.queryByText('Confidential Examination Content')).not.toBeInTheDocument();
  });

  it('allows authenticated user with required role to access route', async () => {
    render(
      <MemoryRouter initialEntries={['/admin-panel']}>
        <AuthProvider>
          <AuthInitializer role="SUPER_ADMIN">
            <Routes>
              <Route path="/unauthorized" element={<div>Access Denied 403</div>} />
              <Route
                path="/admin-panel"
                element={
                  <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
                    <div>Super Admin Secure Workspace</div>
                  </ProtectedRoute>
                }
              />
            </Routes>
          </AuthInitializer>
        </AuthProvider>
      </MemoryRouter>
    );

    expect(await screen.findByText('Super Admin Secure Workspace')).toBeInTheDocument();
    expect(screen.queryByText('Access Denied 403')).not.toBeInTheDocument();
  });

  it('redirects authenticated user lacking required role to /unauthorized (403)', async () => {
    render(
      <MemoryRouter initialEntries={['/admin-panel']}>
        <AuthProvider>
          <AuthInitializer role="STUDENT">
            <Routes>
              <Route path="/unauthorized" element={<div>403 Forbidden Access Denied</div>} />
              <Route
                path="/admin-panel"
                element={
                  <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
                    <div>Super Admin Secure Workspace</div>
                  </ProtectedRoute>
                }
              />
            </Routes>
          </AuthInitializer>
        </AuthProvider>
      </MemoryRouter>
    );

    expect(await screen.findByText('403 Forbidden Access Denied')).toBeInTheDocument();
    expect(screen.queryByText('Super Admin Secure Workspace')).not.toBeInTheDocument();
  });

  it('allows access when user matches one of multiple permitted roles', async () => {
    render(
      <MemoryRouter initialEntries={['/authority-panel']}>
        <AuthProvider>
          <AuthInitializer role="CONTROLLER">
            <Routes>
              <Route path="/unauthorized" element={<div>Access Denied</div>} />
              <Route
                path="/authority-panel"
                element={
                  <ProtectedRoute allowedRoles={['EXAM_AUTHORITY', 'CONTROLLER']}>
                    <div>Examination Authority / Controller Workspace</div>
                  </ProtectedRoute>
                }
              />
            </Routes>
          </AuthInitializer>
        </AuthProvider>
      </MemoryRouter>
    );

    expect(await screen.findByText('Examination Authority / Controller Workspace')).toBeInTheDocument();
  });
});
