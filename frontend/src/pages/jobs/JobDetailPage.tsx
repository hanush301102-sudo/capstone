import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useParams } from 'react-router-dom';
import axios from 'axios';
import { api } from '../../api/client';
import type { Job, PortfolioItem } from '../../api/types';
import { useAuth } from '../../auth/AuthContext';

export function JobDetailPage() {
  const { id } = useParams();
  const { hasRole } = useAuth();
  const [job, setJob] = useState<Job | null>(null);
  const [portfolio, setPortfolio] = useState<PortfolioItem[]>([]);
  const [form, setForm] = useState({ coverLetter: '', briefResponse: '', proposedRate: '', estimatedDays: '' });
  const [samples, setSamples] = useState<number[]>([]);
  const [message, setMessage] = useState('');
  const [done, setDone] = useState(false);

  useEffect(() => {
    if (!id) return;
    api.get<Job>(`/jobs/${id}`).then((res) => setJob(res.data)).catch(() => {});
    if (hasRole('CREATOR')) {
      api.get<PortfolioItem[]>('/portfolios/mine').then((res) => setPortfolio(res.data)).catch(() => {});
    }
  }, [id, hasRole]);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setMessage('');
    try {
      await api.post('/applications', {
        jobId: Number(id),
        coverLetter: form.coverLetter || undefined,
        briefResponse: form.briefResponse || undefined,
        proposedRate: form.proposedRate ? Number(form.proposedRate) : undefined,
        estimatedDays: form.estimatedDays ? Number(form.estimatedDays) : undefined,
        portfolioSampleIds: samples,
      });
      setDone(true);
    } catch (err) {
      setMessage(
        (axios.isAxiosError(err) && (err.response?.data as { message?: string })?.message) ||
          'Could not submit application.',
      );
    }
  };

  const set = (key: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
    setForm((f) => ({ ...f, [key]: e.target.value }));

  if (!job) return <p className="text-ink-400">Loading job…</p>;

  const input =
    'mt-1 block w-full rounded-xl border border-slate-300 px-4 py-2.5 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100';

  return (
    <div>
      <Link to="/discover" className="text-sm text-brand-600 hover:underline">← Discover</Link>
      <h1 className="mt-2 text-3xl font-extrabold tracking-tight">{job.title}</h1>
      <p className="mt-1 text-sm text-ink-400">
        {job.companyName} · {job.budgetMin != null && job.budgetMax != null ? `$${job.budgetMin}–$${job.budgetMax}` : 'Budget open'}
        {job.deadline ? ` · Due ${job.deadline}` : ''} · {job.applicationCount} applied
      </p>

      <div className="mt-6 rounded-2xl border border-slate-200 p-5">
        <h2 className="font-bold">Creative brief</h2>
        <p className="mt-2 text-sm text-ink-600">{job.creativeBrief}</p>
        {job.description && <p className="mt-3 text-sm text-ink-600">{job.description}</p>}
        <div className="mt-3 flex flex-wrap gap-1.5">
          {job.skills.map((s) => (
            <span key={s} className="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-medium text-brand-700">{s}</span>
          ))}
        </div>
        {(job.styleKeywords || job.referenceLinks) && (
          <p className="mt-3 text-sm text-ink-400">Style: {job.styleKeywords} · Refs: {job.referenceLinks}</p>
        )}
      </div>

      {hasRole('CREATOR') && (
        <div className="mt-6 rounded-2xl border border-brand-200 p-5">
          <h2 className="font-bold">Apply — answer the brief, don't paste a template</h2>
          {done ? (
            <p className="mt-3 rounded-xl bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
              Application submitted with a match score. Track it on your dashboard.
            </p>
          ) : (
            <form onSubmit={submit} className="mt-4 space-y-3">
              {message && <p className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700">{message}</p>}
              <label className="block">
                <span className="text-sm font-medium">Brief response * — how you'll nail this style</span>
                <textarea required rows={3} value={form.briefResponse} onChange={set('briefResponse')} className={input} />
              </label>
              <label className="block">
                <span className="text-sm font-medium">Cover letter</span>
                <textarea rows={2} value={form.coverLetter} onChange={set('coverLetter')} className={input} />
              </label>
              <div className="grid grid-cols-2 gap-3">
                <label className="block">
                  <span className="text-sm font-medium">Proposed rate ($)</span>
                  <input type="number" min={0} value={form.proposedRate} onChange={set('proposedRate')} className={input} />
                </label>
                <label className="block">
                  <span className="text-sm font-medium">Estimated days</span>
                  <input type="number" min={1} value={form.estimatedDays} onChange={set('estimatedDays')} className={input} />
                </label>
              </div>
              <div>
                <p className="mb-2 text-sm font-medium">Attach portfolio samples</p>
                {portfolio.length ? (
                  <div className="space-y-2">
                    {portfolio.map((item) => (
                      <label key={item.id} className="flex items-center gap-3 rounded-xl border border-slate-200 px-3 py-2 text-sm">
                        <input
                          type="checkbox"
                          checked={samples.includes(item.id)}
                          onChange={() =>
                            setSamples((prev) => (prev.includes(item.id) ? prev.filter((s) => s !== item.id) : [...prev, item.id]))
                          }
                          className="h-4 w-4"
                        />
                        <span className="font-medium">{item.title}</span>
                        <span className="text-ink-400">· {item.verificationStatus}</span>
                      </label>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-ink-400">
                    No portfolio items yet. <Link to="/creator/portfolio" className="text-brand-600 hover:underline">Add work first</Link> — samples win hires.
                  </p>
                )}
              </div>
              <button type="submit" className="w-full rounded-xl bg-brand-600 py-3 font-semibold text-white hover:bg-brand-700">
                Submit application
              </button>
            </form>
          )}
        </div>
      )}
    </div>
  );
}
