import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../auth/AuthContext';

function roleHome(roles: string[]): string {
  if (roles.includes('CLIENT')) return '/client';
  if (roles.includes('CREATOR')) return '/creator';
  return '/discover';
}

function errorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const message = (error.response?.data as { message?: string } | undefined)?.message;
    if (message) return message;
    if (error.response?.status === 409) return 'That email is already registered.';
  }
  return 'Something went wrong. Please try again.';
}

export function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    role: 'CLIENT',
  });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const set = (key: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
    setForm((f) => ({ ...f, [key]: e.target.value }));

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      const data = await register(form);
      navigate(roleHome(data.roles), { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const input =
    'mt-1 w-full rounded-xl border border-slate-300 px-4 py-2.5 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100';

  return (
    <div className="mx-auto max-w-md py-10">
      <h1 className="text-3xl font-extrabold tracking-tight">Create your account</h1>
      <p className="mt-2 text-ink-600">
        Already registered?{' '}
        <Link to="/login" className="font-medium text-brand-600 hover:underline">
          Login
        </Link>
      </p>
      <form onSubmit={submit} className="mt-8 space-y-4">
        {error && <p className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>}
        <div className="grid grid-cols-2 gap-4">
          <label className="block">
            <span className="text-sm font-medium">First name</span>
            <input required value={form.firstName} onChange={set('firstName')} className={input} />
          </label>
          <label className="block">
            <span className="text-sm font-medium">Last name</span>
            <input required value={form.lastName} onChange={set('lastName')} className={input} />
          </label>
        </div>
        <label className="block">
          <span className="text-sm font-medium">Email</span>
          <input type="email" required value={form.email} onChange={set('email')} className={input} />
        </label>
        <label className="block">
          <span className="text-sm font-medium">Password (min 8 characters)</span>
          <input
            type="password"
            required
            minLength={8}
            value={form.password}
            onChange={set('password')}
            className={input}
          />
        </label>
        <label className="block">
          <span className="text-sm font-medium">I want to…</span>
          <select value={form.role} onChange={set('role')} className={input}>
            <option value="CLIENT">Hire creators (Client)</option>
            <option value="CREATOR">Get hired (Video / Design / Scripts)</option>
          </select>
        </label>
        <button
          type="submit"
          disabled={busy}
          className="w-full rounded-xl bg-brand-600 py-3 font-semibold text-white hover:bg-brand-700 disabled:opacity-60"
        >
          {busy ? 'Creating account…' : 'Create account'}
        </button>
      </form>
    </div>
  );
}
