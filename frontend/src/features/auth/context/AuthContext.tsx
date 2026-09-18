import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { UserRole, UserProfile, AuthContextType } from '../../../types/auth';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const STORAGE_KEY = 'examchain_mock_user';

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const parsed = JSON.parse(stored) as UserProfile;
        setUser(parsed);
      }
    } catch {
      localStorage.removeItem(STORAGE_KEY);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const login = useCallback((role: UserRole) => {
    const formattedName = role
      .toLowerCase()
      .split('_')
      .map(part => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');

    const mockProfile: UserProfile = {
      userId: `usr-${role.toLowerCase()}-001`,
      username: `${role.toLowerCase()}_user`,
      email: `${role.toLowerCase()}@examchain.org`,
      displayName: `${formattedName} Officer`,
      roles: [role],
    };

    localStorage.setItem(STORAGE_KEY, JSON.stringify(mockProfile));
    setUser(mockProfile);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY);
    setUser(null);
  }, []);

  const hasRole = useCallback(
    (requiredRoles: UserRole | UserRole[]): boolean => {
      if (!user || !user.roles) return false;
      const required = Array.isArray(requiredRoles) ? requiredRoles : [requiredRoles];
      return required.some(role => user.roles.includes(role));
    },
    [user]
  );

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    token: user ? `mock-jwt-token-for-${user.username}` : null,
    login,
    logout,
    hasRole,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
