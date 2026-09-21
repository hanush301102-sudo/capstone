import { useEffect, useState } from 'react';
import axios from 'axios';
import { api } from '../../api/client';
import type { Project } from '../../api/types';
import { StatusBadge } from './shared';

const NEXT: Record<string, string[]> = {
  NOT_STARTED: ['IN_PROGRESS', 'CANCELLED'],
  IN_PROGRESS: ['COMPLETED', 'CANCELLED'],
  COMPLETED: [],
  CANCELLED: [],
};

export function ClientProjects() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [error, setError] = useState('');

  const reload = () => {
    api.get<Project[]>('/projects/mine').then((res) => setProjects(res.data)).catch(() => {});
  };
  useEffect(reload, []);

  const advance = async (id: number, status: string) => {
    setError('');
    try {
      await api.patch(`/projects/${id}/status`, { status });
      reload();
    } catch (err) {
      setError(
        (axios.isAxiosError(err) && (err.response?.data as { message?: string })?.message) ||
          'Could not update project.',
      );
    }
  };

  return (
    <div>
      <h1 className="text-3xl font-extrabold tracking-tight">Hired projects</h1>
      <p className="mt-1 text-ink-600">Track delivery. Completing on time feeds your creator's reliability record.</p>
      {error && <p className="mt-4 rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>}
      {projects.length ? (
        <div className="mt-6 space-y-4">
          {projects.map((p) => (
            <div key={p.id} className="rounded-2xl border border-slate-200 p-5">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h3 className="font-bold">{p.title}</h3>
                  <p className="text-sm text-ink-400">
                    {p.jobTitle}
                    {p.deadline ? ` · Due ${p.deadline}` : ''}
                    {p.completedOn ? ` · Completed ${p.completedOn}` : ''}
                  </p>
                </div>
                <StatusBadge status={p.status} />
              </div>
              {NEXT[p.status]?.length > 0 && (
                <div className="mt-3 flex gap-2">
                  {NEXT[p.status].map((next) => (
                    <button
                      key={next}
                      onClick={() => void advance(p.id, next)}
                      className="rounded-lg bg-ink-900 px-3 py-1.5 text-sm font-semibold text-white hover:bg-ink-600"
                    >
                      Mark {next.replace('_', ' ')}
                    </button>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      ) : (
        <p className="mt-6 rounded-2xl border border-dashed border-slate-300 p-10 text-center text-ink-400">
          No hired projects yet. Hire an applicant from a job to create one.
        </p>
      )}
    </div>
  );
}
