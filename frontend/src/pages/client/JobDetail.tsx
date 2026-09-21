import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import { api } from '../../api/client';
import type { Application, Job } from '../../api/types';
import { StatusBadge } from './shared';

function actionError(error: unknown): string {
  if (axios.isAxiosError(error)) {
    return (error.response?.data as { message?: string })?.message ?? 'Action failed.';
  }
  return 'Action failed.';
}

export function JobDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [job, setJob] = useState<Job | null>(null);
  const [applications, setApplications] = useState<Application[]>([]);
  const [compare, setCompare] = useState<number[]>([]);
  const [error, setError] = useState('');

  const reload = () => {
    if (!id) return;
    api.get<Job>(`/jobs/${id}`).then((res) => setJob(res.data)).catch(() => {});
    api.get<Application[]>('/applications', { params: { jobId: id } })
      .then((res) => setApplications(res.data))
      .catch(() => {});
  };
  useEffect(reload, [id]);

  const mutate = async (fn: () => Promise<unknown>) => {
    setError('');
    try {
      await fn();
      reload();
    } catch (err) {
      setError(actionError(err));
    }
  };

  const setStatus = (appId: number, status: string) =>
    mutate(() => api.patch(`/applications/${appId}/status`, { status }));

  const hire = (appId: number) =>
    mutate(() => api.post(`/projects/from-application/${appId}`).then(() => navigate('/client/projects')));

  const remove = () =>
    mutate(() => api.delete(`/jobs/${id}`).then(() => navigate('/client')));

  const toggleCompare = (appId: number) =>
    setCompare((prev) => (prev.includes(appId) ? prev.filter((a) => a !== appId) : [...prev, appId].slice(-3)));

  if (!job) return <p className="text-ink-400">Loading job…</p>;
  const compared = applications.filter((a) => compare.includes(a.id));

  return (
    <div>
      <Link to="/client" className="text-sm text-brand-600 hover:underline">← My jobs</Link>
      <div className="mt-2 flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold tracking-tight">{job.title}</h1>
          <div className="mt-2 flex gap-2">
            <StatusBadge status={job.status} />
            <span className="text-sm text-ink-400">{applications.length} applications</span>
          </div>
        </div>
        <div className="flex gap-2">
          <Link to={`/client/jobs/${job.id}/edit`} className="rounded-xl border border-slate-300 px-4 py-2 text-sm font-semibold hover:bg-slate-50">
            Edit
          </Link>
          <button onClick={() => void remove()} className="rounded-xl border border-red-200 px-4 py-2 text-sm font-semibold text-red-600 hover:bg-red-50">
            Delete
          </button>
        </div>
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <div className="rounded-2xl border border-slate-200 p-5">
          <h2 className="font-bold">Creative brief</h2>
          <p className="mt-2 text-sm text-ink-600">{job.creativeBrief}</p>
          {job.description && <p className="mt-3 text-sm text-ink-600">{job.description}</p>}
          <div className="mt-3 flex flex-wrap gap-1.5">
            {job.skills.map((s) => (
              <span key={s} className="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-medium text-brand-700">{s}</span>
            ))}
          </div>
          <p className="mt-3 text-sm text-ink-400">
            {job.budgetMin != null && job.budgetMax != null ? `$${job.budgetMin}–$${job.budgetMax}` : 'Budget open'}
            {job.deadline ? ` · Due ${job.deadline}` : ''} · {job.styleKeywords} · {job.referenceLinks}
          </p>
        </div>
        <div className="rounded-2xl bg-slate-50 p-5">
          <h2 className="font-bold">How to hire here</h2>
          <ol className="mt-2 list-decimal space-y-1 pl-5 text-sm text-ink-600">
            <li>Review applications ranked by match score.</li>
            <li>Tick up to 3 to compare side-by-side.</li>
            <li>Shortlist, then hire — a project is created automatically.</li>
          </ol>
        </div>
      </div>

      {error && <p className="mt-4 rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>}

      {compared.length >= 2 && <ComparisonTable applications={compared} />}

      <h2 className="mt-8 text-xl font-bold">Applications (ranked by match score)</h2>
      {applications.length ? (
        <div className="mt-4 space-y-4">
          {applications.map((app) => (
            <div key={app.id} className="rounded-2xl border border-slate-200 p-5">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div className="flex items-center gap-3">
                  <input
                    type="checkbox"
                    checked={compare.includes(app.id)}
                    onChange={() => toggleCompare(app.id)}
                    title="Add to comparison"
                    className="h-4 w-4"
                  />
                  <div>
                    <p className="font-bold">{app.creatorHeadline ?? `Creator #${app.creatorProfileId}`}</p>
                    <p className="text-sm text-ink-400">
                      ${app.proposedRate} · {app.estimatedDays} days · responded in {app.responseTimeHours}h
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <span className="rounded-full bg-emerald-100 px-3 py-1 text-sm font-extrabold text-emerald-700">
                    {app.matchScore}
                  </span>
                  <StatusBadge status={app.status} />
                </div>
              </div>
              {app.briefResponse && (
                <p className="mt-3 rounded-xl bg-slate-50 p-3 text-sm text-ink-600">
                  <strong>On your brief:</strong> {app.briefResponse}
                </p>
              )}
              {app.coverLetter && <p className="mt-2 text-sm text-ink-600">{app.coverLetter}</p>}
              <div className="mt-3 flex flex-wrap gap-2">
                {app.status === 'PENDING' && (
                  <button onClick={() => void setStatus(app.id, 'SHORTLISTED')} className="rounded-lg bg-amber-500 px-3 py-1.5 text-sm font-semibold text-white hover:bg-amber-600">
                    Shortlist
                  </button>
                )}
                {(app.status === 'PENDING' || app.status === 'SHORTLISTED') && (
                  <>
                    <button onClick={() => void hire(app.id)} className="rounded-lg bg-brand-600 px-3 py-1.5 text-sm font-semibold text-white hover:bg-brand-700">
                      Hire
                    </button>
                    <button onClick={() => void setStatus(app.id, 'REJECTED')} className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-semibold hover:bg-slate-50">
                      Reject
                    </button>
                  </>
                )}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p className="mt-4 rounded-2xl border border-dashed border-slate-300 p-8 text-center text-ink-400">
          No applications yet. Share the job or find creators via Discover.
        </p>
      )}
    </div>
  );
}

function ComparisonTable({ applications }: { applications: Application[] }) {
  const rows: [string, (a: Application) => string][] = [
    ['Match score', (a) => a.matchScore.toFixed(2)],
    ['Proposed rate', (a) => (a.proposedRate != null ? `$${a.proposedRate}` : '—')],
    ['Timeline', (a) => (a.estimatedDays != null ? `${a.estimatedDays} days` : '—')],
    ['Responded in', (a) => `${a.responseTimeHours}h`],
    ['Status', (a) => a.status],
    ['Samples', (a) => (a.samplePortfolioIds.length ? `${a.samplePortfolioIds.length} attached` : '—')],
  ];
  return (
    <div className="mt-6 overflow-x-auto rounded-2xl border border-brand-200">
      <table className="w-full min-w-xl text-sm">
        <thead>
          <tr className="bg-brand-50">
            <th className="p-3 text-left">Compare ({applications.length})</th>
            {applications.map((a) => (
              <th key={a.id} className="p-3 text-left">{a.creatorHeadline ?? `#${a.creatorProfileId}`}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map(([label, fn]) => (
            <tr key={label} className="border-t border-slate-100">
              <td className="p-3 font-medium text-ink-400">{label}</td>
              {applications.map((a) => (
                <td key={a.id} className="p-3 font-semibold">{fn(a)}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
