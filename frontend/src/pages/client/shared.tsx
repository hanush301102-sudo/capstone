import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client';
import type { Job, Skill } from '../../api/types';

export function useSkills(): Skill[] {
  const [skills, setSkills] = useState<Skill[]>([]);
  useEffect(() => {
    api.get<Skill[]>('/skills').then((res) => setSkills(res.data)).catch(() => {});
  }, []);
  return skills;
}

export function SkillPicker({
  selected,
  onChange,
}: {
  selected: number[];
  onChange: (ids: number[]) => void;
}) {
  const skills = useSkills();
  const toggle = (id: number) =>
    onChange(selected.includes(id) ? selected.filter((s) => s !== id) : [...selected, id]);
  return (
    <div className="flex flex-wrap gap-2">
      {skills.map((skill) => (
        <button
          type="button"
          key={skill.id}
          onClick={() => toggle(skill.id)}
          className={`rounded-full px-3 py-1 text-sm font-medium ${
            selected.includes(skill.id)
              ? 'bg-brand-600 text-white'
              : 'bg-slate-100 text-ink-600 hover:bg-slate-200'
          }`}
        >
          {skill.name}
        </button>
      ))}
    </div>
  );
}

export function skillIdsByName(skills: Skill[], names: string[]): number[] {
  const map = new Map(skills.map((s) => [s.name, s.id]));
  return names.map((n) => map.get(n)).filter((id): id is number => id !== undefined);
}

export function useMyJobs(): { jobs: Job[]; reload: () => void } {
  const [jobs, setJobs] = useState<Job[]>([]);
  const reload = () => {
    api.get<Job[]>('/jobs/mine').then((res) => setJobs(res.data)).catch(() => {});
  };
  useEffect(reload, []);
  return { jobs, reload };
}

export function StatusBadge({ status }: { status: string }) {
  const style =
    status === 'OPEN'
      ? 'bg-emerald-100 text-emerald-700'
      : status === 'ACCEPTED' || status === 'COMPLETED' || status === 'VERIFIED'
        ? 'bg-brand-100 text-brand-700'
        : status === 'REJECTED' || status === 'CANCELLED'
          ? 'bg-red-100 text-red-700'
          : 'bg-amber-100 text-amber-700';
  return (
    <span className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-semibold ${style}`}>
      {status}
    </span>
  );
}

export function JobLink({ job }: { job: Job }) {
  return (
    <Link
      to={`/client/jobs/${job.id}`}
      className="block rounded-2xl border border-slate-200 p-5 transition hover:shadow-md"
    >
      <div className="flex items-start justify-between gap-3">
        <h3 className="font-bold">{job.title}</h3>
        <StatusBadge status={job.status} />
      </div>
      <p className="mt-1 line-clamp-2 text-sm text-ink-600">{job.creativeBrief}</p>
      <p className="mt-2 text-sm text-ink-400">
        {job.applicationCount} application{job.applicationCount === 1 ? '' : 's'}
        {job.budgetMin != null && job.budgetMax != null && (
          <> · ${job.budgetMin}–${job.budgetMax}</>
        )}
      </p>
    </Link>
  );
}
