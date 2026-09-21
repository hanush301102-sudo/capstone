import { useCallback, useEffect, useState } from 'react';
import { api } from '../api/client';
import type { CreatorCard as CreatorCardType, Job, Skill } from '../api/types';
import { CreatorCard } from '../components/CreatorCard';
import { JobCard } from '../components/JobCard';

type Tab = 'creators' | 'jobs';

export function Discover() {
  const [tab, setTab] = useState<Tab>('creators');
  const [skills, setSkills] = useState<Skill[]>([]);
  const [selectedSkills, setSelectedSkills] = useState<number[]>([]);
  const [availability, setAvailability] = useState('');
  const [keyword, setKeyword] = useState('');
  const [creators, setCreators] = useState<CreatorCardType[]>([]);
  const [jobs, setJobs] = useState<Job[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get<Skill[]>('/skills').then((res) => setSkills(res.data)).catch(() => {});
  }, []);

  const toggleSkill = (id: number) =>
    setSelectedSkills((prev) => (prev.includes(id) ? prev.filter((s) => s !== id) : [...prev, id]));

  const searchCreators = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get<CreatorCardType[]>('/discovery/creators', {
        params: {
          ...(selectedSkills.length ? { skillIds: selectedSkills.join(',') } : {}),
          ...(availability ? { availability } : {}),
        },
      });
      setCreators(res.data);
    } catch {
      setError('Could not load creators.');
    } finally {
      setLoading(false);
    }
  }, [selectedSkills, availability]);

  const searchJobs = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get<Job[]>('/jobs', {
        params: {
          ...(keyword ? { keyword } : {}),
          ...(selectedSkills.length ? { skillIds: selectedSkills.join(',') } : {}),
        },
      });
      setJobs(res.data);
    } catch {
      setError('Could not load jobs.');
    } finally {
      setLoading(false);
    }
  }, [keyword, selectedSkills]);

  useEffect(() => {
    if (tab === 'creators') void searchCreators();
    else void searchJobs();
  }, [tab, searchCreators, searchJobs]);

  const skillFilter = (
    <div className="flex flex-wrap gap-2">
      {skills.map((skill) => (
        <button
          key={skill.id}
          onClick={() => toggleSkill(skill.id)}
          className={`rounded-full px-3 py-1 text-sm font-medium ${
            selectedSkills.includes(skill.id)
              ? 'bg-brand-600 text-white'
              : 'bg-slate-100 text-ink-600 hover:bg-slate-200'
          }`}
        >
          {skill.name}
        </button>
      ))}
    </div>
  );

  return (
    <div>
      <h1 className="text-3xl font-extrabold tracking-tight">Discover</h1>
      <p className="mt-1 text-ink-600">Creator Radar and open briefs — filter by skill, availability, keyword.</p>

      <div className="mt-6 flex gap-2">
        {(['creators', 'jobs'] as Tab[]).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`rounded-xl px-5 py-2 font-semibold capitalize ${
              tab === t ? 'bg-ink-900 text-white' : 'bg-slate-100 text-ink-600 hover:bg-slate-200'
            }`}
          >
            {t}
          </button>
        ))}
      </div>

      <div className="mt-6 rounded-2xl border border-slate-200 p-5">
        {tab === 'creators' ? (
          <div className="flex flex-wrap items-end gap-4">
            <label className="block">
              <span className="text-sm font-medium">Availability</span>
              <select
                value={availability}
                onChange={(e) => setAvailability(e.target.value)}
                className="mt-1 block rounded-xl border border-slate-300 px-4 py-2"
              >
                <option value="">Any</option>
                <option value="AVAILABLE">Available</option>
                <option value="PARTIAL">Partial</option>
                <option value="UNAVAILABLE">Unavailable</option>
              </select>
            </label>
            <button
              onClick={() => void searchCreators()}
              className="rounded-xl bg-brand-600 px-5 py-2 font-semibold text-white hover:bg-brand-700"
            >
              Search
            </button>
          </div>
        ) : (
          <div className="flex flex-wrap items-end gap-4">
            <label className="block flex-1 min-w-52">
              <span className="text-sm font-medium">Keyword</span>
              <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && void searchJobs()}
                placeholder="teaser, thumbnail, script…"
                className="mt-1 block w-full rounded-xl border border-slate-300 px-4 py-2"
              />
            </label>
            <button
              onClick={() => void searchJobs()}
              className="rounded-xl bg-brand-600 px-5 py-2 font-semibold text-white hover:bg-brand-700"
            >
              Search
            </button>
          </div>
        )}
        <div className="mt-4">
          <p className="mb-2 text-sm font-medium">Skills</p>
          {skillFilter}
        </div>
      </div>

      {error && <p className="mt-4 rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>}

      <div className="mt-6">
        {loading ? (
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {[0, 1, 2].map((i) => (
              <div key={i} className="h-64 animate-pulse rounded-2xl bg-slate-100" />
            ))}
          </div>
        ) : tab === 'creators' ? (
          creators.length ? (
            <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
              {creators.map((c) => (
                <CreatorCard key={c.id} creator={c} />
              ))}
            </div>
          ) : (
            <EmptyState text="No creators match these filters yet." />
          )
        ) : jobs.length ? (
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {jobs.map((j) => (
              <JobCard key={j.id} job={j} />
            ))}
          </div>
        ) : (
          <EmptyState text="No open jobs match. Try clearing a filter." />
        )}
      </div>
    </div>
  );
}

function EmptyState({ text }: { text: string }) {
  return (
    <div className="rounded-2xl border border-dashed border-slate-300 p-12 text-center text-ink-400">
      {text}
    </div>
  );
}
