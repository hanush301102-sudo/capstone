import { Link } from 'react-router-dom';
import { JobLink, useMyJobs } from './shared';

export function ClientDashboard() {
  const { jobs } = useMyJobs();

  return (
    <div>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-extrabold tracking-tight">Client dashboard</h1>
          <p className="mt-1 text-ink-600">Post briefed jobs, review scored applications, track projects.</p>
        </div>
        <div className="flex gap-2">
          <Link
            to="/client/projects"
            className="rounded-xl border border-slate-300 px-4 py-2 font-semibold hover:bg-slate-50"
          >
            Projects
          </Link>
          <Link
            to="/client/jobs/new"
            className="rounded-xl bg-brand-600 px-4 py-2 font-semibold text-white hover:bg-brand-700"
          >
            + Post a job
          </Link>
        </div>
      </div>

      <h2 className="mt-8 text-xl font-bold">My jobs ({jobs.length})</h2>
      {jobs.length ? (
        <div className="mt-4 grid gap-4 md:grid-cols-2">
          {jobs.map((job) => (
            <JobLink key={job.id} job={job} />
          ))}
        </div>
      ) : (
        <div className="mt-4 rounded-2xl border border-dashed border-slate-300 p-10 text-center text-ink-400">
          No jobs yet. Post your first briefed job — it takes under 3 minutes.
        </div>
      )}
    </div>
  );
}
