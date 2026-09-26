import { useEffect, useState } from 'react';
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
  }
  return 'Something went wrong. Please try again.';
}

export function VerifyOtp() {
  const { verifyOtp, resendOtp } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const initialEmail = (location.state as { email?: string } | null)?.email ?? '';
  const [email, setEmail] = useState(initialEmail);
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [info, setInfo] = useState(
    initialEmail ? `We sent a 6-digit code to ${initialEmail}. It expires in 10 minutes.` : '',
  );
  const [busy, setBusy] = useState(false);
  const [resending, setResending] = useState(false);
  const [cooldown, setCooldown] = useState(0);

  useEffect(() => {
    if (cooldown <= 0) return;
    const t = setTimeout(() => setCooldown((c) => c - 1), 1000);
    return () => clearTimeout(t);
  }, [cooldown]);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setInfo('');
    setBusy(true);
    try {
      const data = await verifyOtp(email, code);
      navigate(roleHome(data.roles), { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const resend = async () => {
    if (!email || cooldown > 0) return;
    setError('');
    setInfo('');
    setResending(true);
    try {
      await resendOtp(email);
      setInfo('A new code was sent. Previous codes no longer work.');
      setCooldown(30);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setResending(false);
    }
  };

  const input =
    'mt-1 w-full rounded-xl border border-slate-300 px-4 py-2.5 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100';

  return (
    <div className="mx-auto max-w-md py-10">
      <h1 className="text-3xl font-extrabold tracking-tight">Verify your email</h1>
      <p className="mt-2 text-ink-600">Enter the 6-digit code we emailed you.</p>
      <form onSubmit={submit} className="mt-8 space-y-4">
        {error && <p className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>}
        {info && <p className="rounded-xl bg-green-50 px-4 py-3 text-sm text-green-700">{info}</p>}
        <label className="block">
          <span className="text-sm font-medium">Email</span>
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className={input}
          />
        </label>
        <label className="block">
          <span className="text-sm font-medium">6-digit code</span>
          <input
            required
            inputMode="numeric"
            pattern="\d{6}"
            maxLength={6}
            value={code}
            onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
            className={`${input} text-center text-2xl tracking-[0.5em]`}
            placeholder="••••••"
          />
        </label>
        <button
          type="submit"
          disabled={busy}
          className="w-full rounded-xl bg-brand-600 py-3 font-semibold text-white hover:bg-brand-700 disabled:opacity-60"
        >
          {busy ? 'Verifying…' : 'Verify email'}
        </button>
        <button
          type="button"
          onClick={resend}
          disabled={resending || cooldown > 0 || !email}
          className="w-full rounded-xl border border-slate-300 py-3 font-semibold text-ink-700 hover:bg-slate-50 disabled:opacity-60"
        >
          {cooldown > 0
            ? `Resend code in ${cooldown}s`
            : resending
              ? 'Sending…'
              : 'Resend code'}
        </button>
        <p className="text-center text-sm text-ink-600">
          <Link to="/login" className="font-medium text-brand-600 hover:underline">
            Back to login
          </Link>
        </p>
      </form>
    </div>
  );
}
