import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../auth/AuthContext';

function roleHome(roles: string[]): string {
  if (roles.includes('ADMIN')) return '/admin';
  if (roles.includes('CLIENT')) return '/client';
  if (roles.includes('CREATOR')) return '/creator';
  return '/discover';
}

function errorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const message = (error.response?.data as { message?: string } | undefined)?.message;
    if (message) return message;
    if (error.response?.status === 401) return 'Invalid email or password.';
  }
  return 'Something went wrong. Please try again.';
}

export function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      const data = await login(email, password);
      const from = (location.state as { from?: string } | null)?.from;
      navigate(from ?? roleHome(data.roles), { replace: true });
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 403 && (err.response?.data as { message?: string } | undefined)?.message?.toLowerCase().includes('verif')) {
        navigate('/verify-otp', { replace: true, state: { email } });
        return;
      }
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="mx-auto max-w-md py-10">
      <h1 className="text-3xl font-extrabold tracking-tight">Welcome back</h1>
      <p className="mt-2 text-ink-600">
        New here?{' '}
        <Link to="/register" className="font-medium text-brand-600 hover:underline">
          Create an account
        </Link>
      </p>
      <form onSubmit={submit} className="mt-8 space-y-4">
        {error && <p className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>}
        <label className="block">
          <span className="text-sm font-medium">Email</span>
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="mt-1 w-full rounded-xl border border-slate-300 px-4 py-2.5 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
          />
        </label>
        <label className="block">
          <span className="text-sm font-medium">Password</span>
          <input
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="mt-1 w-full rounded-xl border border-slate-300 px-4 py-2.5 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
          />
        </label>
        <button
          type="submit"
          disabled={busy}
          className="w-full rounded-xl bg-brand-600 py-3 font-semibold text-white hover:bg-brand-700 disabled:opacity-60"
        >
          {busy ? 'Logging in…' : 'Login'}
        </button>
      </form>
    </div>
  );
}
