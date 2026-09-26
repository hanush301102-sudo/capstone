import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { api, getToken, setToken } from '../api/client';
import type { AuthResponse, AuthUser } from '../api/types';

export interface OtpResponse {
  message: string;
  email: string;
}

interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<AuthResponse>;
  register: (input: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    role: string;
  }) => Promise<OtpResponse>;
  verifyOtp: (email: string, code: string) => Promise<AuthResponse>;
  resendOtp: (email: string) => Promise<OtpResponse>;
  logout: () => void;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!getToken()) {
      setLoading(false);
      return;
    }
    api.get<AuthUser>('/auth/me')
      .then((res) => setUser(res.data))
      .catch(() => setToken(null))
      .finally(() => setLoading(false));
  }, []);

  const applyResponse = useCallback((data: AuthResponse) => {
    setToken(data.token);
    setUser({ email: data.email, firstName: '', lastName: '', roles: data.roles });
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const res = await api.post<AuthResponse>('/auth/login', { email, password });
    applyResponse(res.data);
    return res.data;
  }, [applyResponse]);

  const register = useCallback(
    async (input: { email: string; password: string; firstName: string; lastName: string; role: string }) => {
      const res = await api.post<OtpResponse>('/auth/register', input);
      return res.data;
    },
    [],
  );

  const verifyOtp = useCallback(async (email: string, code: string) => {
    const res = await api.post<AuthResponse>('/auth/verify-otp', { email, code });
    applyResponse(res.data);
    return res.data;
  }, [applyResponse]);

  const resendOtp = useCallback(async (email: string) => {
    const res = await api.post<OtpResponse>('/auth/resend-otp', { email });
    return res.data;
  }, []);

  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
  }, []);

  const hasRole = useCallback((role: string) => user?.roles.includes(role) ?? false, [user]);

  const value = useMemo(
    () => ({ user, loading, login, register, verifyOtp, resendOtp, logout, hasRole }),
    [user, loading, login, register, verifyOtp, resendOtp, logout, hasRole],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}
