import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function RequireAuth() {
  const { user, loading } = useAuth();
  const location = useLocation();
  if (loading) {
    return <div className="flex min-h-screen items-center justify-center text-ink-400">Loading…</div>;
  }
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  return <Outlet />;
}

export function RequireRole({ role }: { role: string }) {
  const { user, loading, hasRole } = useAuth();
  if (loading) {
    return <div className="flex min-h-screen items-center justify-center text-ink-400">Loading…</div>;
  }
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  if (!hasRole(role)) {
    return <Navigate to="/" replace />;
  }
  return <Outlet />;
}
