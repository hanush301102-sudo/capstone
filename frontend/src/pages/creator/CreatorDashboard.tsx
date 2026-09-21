import axios from 'axios';
import { api } from '../../api/client';
import { DashboardLinks, ReliabilityBanner, StatusBadge, useCreatorProfile, useMyApplications, useMyProjects } from './shared';

const NEXT: Record<string, string[]> = {
  NOT_STARTED: ['IN_PROGRESS', 'CANCELLED'],
  IN_PROGRESS: ['COMPLETED', 'CANCELLED'],
  COMPLETED: [],
  CANCELLED: [],
};

export function CreatorDashboard() {
  const { profile } = useCreatorProfile();
  const { applications } = useMyApplications();
  const { projects, reload } = useMyProjects();

  const advance = async (id: number, status: string) => {
    try {
      await api.patch(`/projects/${id}/status`, { status });
      reload();
    } catch (err) {
      alert(
        (axios.isAxiosError(err) && (err.response?.data as { message?: string })?.message) ||
          'Could not update project.',
      );
    }
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-extrabold tracking-tight">Creator dashboard</h1>
        <p className="mt-1 text-ink-600">
          {profile?.headline ?? 'Complete your profile to stand out on Radar.'}
        </p>
      </div>

      <ReliabilityBanner profile={profile} />
      <DashboardLinks />

      <section>
        <h2 className="text-xl font-bold">My applications ({applications.length})</h2>
        {applications.length ? (
          <div className="mt-4 space-y-3">
            {applications.map((a) => (
              <div key={a.id} className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-slate-200 p-4">
                <div>
                  <p className="font-bold">{a.jobTitle}</p>
                  <p className="text-sm text-ink-400">
                    Match {a.matchScore} · ${a.proposedRate} · {a.estimatedDays} days
                  </p>
                </div>
                <StatusBadge status={a.status} />
              </div>
            ))}
          </div>
        ) : (
          <p className="mt-4 rounded-2xl border border-dashed border-slate-300 p-8 text-center text-ink-400">
            No applications yet. Discover open briefs and answer them with samples.
          </p>
        )}
      </section>

      <section>
        <h2 className="text-xl font-bold">My projects ({projects.length})</h2>
        {projects.length ? (
          <div className="mt-4 space-y-3">
            {projects.map((p) => (
              <div key={p.id} className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-slate-200 p-4">
                <div>
                  <p className="font-bold">{p.title}</p>
                  <p className="text-sm text-ink-400">
                    {p.jobTitle}{p.deadline ? ` · Due ${p.deadline}` : ''}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <StatusBadge status={p.status} />
                  {NEXT[p.status]?.map((next) => (
                    <button
                      key={next}
                      onClick={() => void advance(p.id, next)}
                      className="rounded-lg bg-ink-900 px-3 py-1.5 text-xs font-semibold text-white hover:bg-ink-600"
                    >
                      {next.replace('_', ' ')}
                    </button>
                  ))}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="mt-4 rounded-2xl border border-dashed border-slate-300 p-8 text-center text-ink-400">
            No hired projects yet.
          </p>
        )}
      </section>
    </div>
  );
}
