import { Link } from 'react-router-dom';
import type { CreatorCard as CreatorCardType } from '../api/types';

function availabilityStyle(availability: string): string {
  if (availability === 'AVAILABLE') return 'bg-emerald-100 text-emerald-700';
  if (availability === 'PARTIAL') return 'bg-amber-100 text-amber-700';
  return 'bg-slate-200 text-slate-600';
}

export function CreatorCard({ creator }: { creator: CreatorCardType }) {
  const initial = (creator.headline ?? '?').charAt(0).toUpperCase();
  return (
    <div className="flex flex-col rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-1 hover:shadow-lg">
      <div className="flex items-start gap-4">
        <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-500 to-fuchsia-500 text-xl font-extrabold text-white">
          {initial}
        </div>
        <div className="min-w-0">
          <h3 className="truncate font-bold">{creator.headline ?? 'Creative professional'}</h3>
          <span
            className={`mt-1 inline-block rounded-full px-2 py-0.5 text-xs font-semibold ${availabilityStyle(creator.availability)}`}
          >
            {creator.availability}
          </span>
        </div>
      </div>
      {creator.bio && <p className="mt-3 line-clamp-2 text-sm text-ink-600">{creator.bio}</p>}
      <div className="mt-3 flex flex-wrap gap-1.5">
        {creator.skills.slice(0, 4).map((skill) => (
          <span key={skill} className="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-medium text-brand-700">
            {skill}
          </span>
        ))}
      </div>
      <div className="mt-4 grid grid-cols-3 gap-2 border-t border-slate-100 pt-3 text-center text-xs">
        <div>
          <p className="font-extrabold text-ink-900">{creator.onTimeDeliveryRate}%</p>
          <p className="text-ink-400">On-time</p>
        </div>
        <div>
          <p className="font-extrabold text-ink-900">~{creator.avgResponseTimeHours}h</p>
          <p className="text-ink-400">Responds</p>
        </div>
        <div>
          <p className="font-extrabold text-ink-900">{creator.completedProjects}</p>
          <p className="text-ink-400">Projects</p>
        </div>
      </div>
      <div className="mt-3 flex items-center justify-between text-sm">
        <span className="font-semibold">
          {creator.hourlyRate != null ? `$${creator.hourlyRate}/hr` : 'Rate on request'}
        </span>
        {creator.experienceYears != null && (
          <span className="text-ink-400">{creator.experienceYears} yrs exp</span>
        )}
      </div>
      <Link
        to={`/creator/${creator.id}`}
        className="mt-4 rounded-xl border border-slate-300 py-2 text-center text-sm font-semibold hover:bg-slate-50"
      >
        View profile
      </Link>
    </div>
  );
}
