import { Link } from 'react-router-dom';
import type { Job } from '../api/types';

export function JobCard({ job }: { job: Job }) {
  return (
    <div className="flex flex-col rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-1 hover:shadow-lg">
      <div className="flex items-start justify-between gap-3">
        <h3 className="font-bold">{job.title}</h3>
        <span className="shrink-0 rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-semibold text-emerald-700">
          {job.status}
        </span>
      </div>
      {job.companyName && <p className="mt-1 text-sm text-ink-400">{job.companyName}</p>}
      <p className="mt-3 line-clamp-2 text-sm text-ink-600">{job.creativeBrief}</p>
      <div className="mt-3 flex flex-wrap gap-1.5">
        {job.skills.slice(0, 4).map((skill) => (
          <span key={skill} className="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-medium text-brand-700">
            {skill}
          </span>
        ))}
      </div>
      <div className="mt-4 flex items-center justify-between border-t border-slate-100 pt-3 text-sm">
        <span className="font-semibold">
          {job.budgetMin != null && job.budgetMax != null
            ? `$${job.budgetMin}–$${job.budgetMax}`
            : 'Budget open'}
        </span>
        <span className="text-ink-400">
          {job.deadline ? `Due ${job.deadline}` : 'No deadline'} · {job.applicationCount} applied
        </span>
      </div>
      <Link
        to={`/jobs/${job.id}`}
        className="mt-4 rounded-xl border border-slate-300 py-2 text-center text-sm font-semibold hover:bg-slate-50"
      >
        View & apply
      </Link>
    </div>
  );
}
