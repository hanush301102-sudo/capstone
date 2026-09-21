import { useState } from 'react';
import type { FormEvent } from 'react';
import axios from 'axios';
import { api } from '../api/client';

export function ReportPage() {
  const [form, setForm] = useState({ reportedUserId: '', jobId: '', reason: '', description: '' });
  const [message, setMessage] = useState('');
  const [ok, setOk] = useState(false);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setMessage('');
    setOk(false);
    try {
      await api.post('/reports', {
        reportedUserId: form.reportedUserId ? Number(form.reportedUserId) : undefined,
        jobId: form.jobId ? Number(form.jobId) : undefined,
        reason: form.reason,
        description: form.description || undefined,
      });
      setOk(true);
      setForm({ reportedUserId: '', jobId: '', reason: '', description: '' });
    } catch (err) {
      setMessage(
        (axios.isAxiosError(err) && (err.response?.data as { message?: string })?.message) ||
          'Could not file the report.',
      );
    }
  };

  const set = (key: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
    setForm((f) => ({ ...f, [key]: e.target.value }));

  const input =
    'mt-1 block w-full rounded-xl border border-slate-300 px-4 py-2.5 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100';

  return (
    <div className="mx-auto max-w-xl">
      <h1 className="text-3xl font-extrabold tracking-tight">File a report</h1>
      <p className="mt-1 text-ink-600">Flag a user or job for admin review. Target at least one.</p>
      <form onSubmit={submit} className="mt-6 space-y-4">
        {message && <p className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700">{message}</p>}
        {ok && <p className="rounded-xl bg-emerald-50 px-4 py-3 text-sm text-emerald-700">Report filed. Admins will review it.</p>}
        <div className="grid grid-cols-2 gap-4">
          <label className="block">
            <span className="text-sm font-medium">Reported user ID</span>
            <input type="number" value={form.reportedUserId} onChange={set('reportedUserId')} className={input} />
          </label>
          <label className="block">
            <span className="text-sm font-medium">Job ID</span>
            <input type="number" value={form.jobId} onChange={set('jobId')} className={input} />
          </label>
        </div>
        <label className="block">
          <span className="text-sm font-medium">Reason *</span>
          <input required value={form.reason} onChange={set('reason')} className={input} />
        </label>
        <label className="block">
          <span className="text-sm font-medium">Details</span>
          <textarea rows={3} value={form.description} onChange={set('description')} className={input} />
        </label>
        <button type="submit" className="w-full rounded-xl bg-brand-600 py-3 font-semibold text-white hover:bg-brand-700">
          Submit report
        </button>
      </form>
    </div>
  );
}
