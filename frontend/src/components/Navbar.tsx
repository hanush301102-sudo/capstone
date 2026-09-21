import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { NotificationBell } from './Notifications';

export function Navbar() {
  const { user, logout, hasRole } = useAuth();
  const navigate = useNavigate();

  return (
    <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4">
        <Link to="/" className="text-xl font-bold tracking-tight">
          Creator<span className="text-brand-600">Hire</span>
        </Link>
        <nav className="flex items-center gap-4 text-sm">
          {user ? (
            <>
              {hasRole('CLIENT') && <Link to="/client" className="hover:text-brand-600">Dashboard</Link>}
              {hasRole('CREATOR') && <Link to="/creator" className="hover:text-brand-600">Dashboard</Link>}
              {hasRole('ADMIN') && <Link to="/admin" className="hover:text-brand-600">Admin</Link>}
              <Link to="/discover" className="hover:text-brand-600">Discover</Link>
              <NotificationBell />
              <span className="text-ink-400">{user.email}</span>
              <button
                onClick={() => {
                  logout();
                  navigate('/');
                }}
                className="rounded-lg border border-slate-300 px-3 py-1.5 hover:bg-slate-50"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="hover:text-brand-600">Login</Link>
              <Link
                to="/register"
                className="rounded-lg bg-brand-600 px-4 py-2 font-medium text-white hover:bg-brand-700"
              >
                Get started
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
