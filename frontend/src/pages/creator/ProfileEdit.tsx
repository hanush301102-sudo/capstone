import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { api } from '../../api/client';
import type { Skill } from '../../api/types';

const LEVELS = ['BEGINNER', 'INTERMEDIATE', 'EXPERT'];

export function ProfileEdit() {
  const navigate = useNavigate();
  const [skills, setSkills] = useState<Skill[]>([]);
  const [form, setForm] = useState({
    headline: '',
    bio: '',
    experienceYears: '',
    availability: 'AVAILABLE',
    hourlyRate: '',
  });
  const [picked, setPicked] = useState<Record<number, string>>({});
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api.get<Skill[]>('/skills').then((res) => setSkills(res.data)).catch(() => {});
    api.get('/profiles/creator/me').then((res) => {
      const p = res.data;
      setForm({
        headline: p.headline ?? '',
        bio: p.bio ?? '',
        experienceYears: p.experienceYears?.toString() ?? '',
        availability: p.availability ?? 'AVAILABLE',
        hourlyRate: p.hourlyRate?.toString() ?? '',
      });
      const map: Record<number, string> = {};
      for (const s of p.skills ?? []) map[s.skillId] = s.level;
      setPicked(map);
    }).catch(() => {});
  }, []);

  const toggle = (id: number) =>
    setPicked((prev) => {
      const next = { ...prev };
      if (next[id]) delete next[id];
      else next[id] = 'INTERMEDIATE';
      return next;
    });

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setMessage('');
    setBusy(true);
    try {
      await api.put('/profiles/creator/me', {
        headline: form.headline || undefined,
        bio: form.bio || undefined,
        experienceYears: form.experienceYears ? Number(form.experienceYears) : undefined,
        availability: form.availability,
        hourlyRate: form.hourlyRate ? Number(form.hourlyRate) : undefined,
        skills: Object.entries(picked).map(([skillId, level]) => ({ skillId: Number(skillId), level })),
      });
      navigate('/creator');
    } catch (err) {
      setMessage(
        (axios.isAxiosError(err) && (err.response?.data as { message?: string })?.message) ||
          'Could not save profile.',
      );
    } finally {
      setBusy(false);
    }
  };

  const set = (key: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
    setForm((f) => ({ ...f, [key]: e.target.value }));

  const input =
    'mt-1 block w-full rounded-xl border border-slate-300 px-4 py-2.5 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100';

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="text-3xl font-extrabold tracking-tight">Edit profile</h1>
      <p className="mt-1 text-ink-600">This is what clients see on Radar. Skills power your match scores.</p>
      <form onSubmit={submit} className="mt-6 space-y-4">
        {message && <p className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700">{message}</p>}
        <label className="block">
          <span className="text-sm font-medium">Headline</span>
          <input value={form.headline} onChange={set('headline')} placeholder="Teaser specialist" className={input} />
        </label>
        <label className="block">
          <span className="text-sm font-medium">Bio</span>
          <textarea rows={3} value={form.bio} onChange={set('bio')} className={input} />
        </label>
        <div className="grid grid-cols-3 gap-4">
          <label className="block">
            <span className="text-sm font-medium">Experience (yrs)</span>
            <input type="number" min={0} value={form.experienceYears} onChange={set('experienceYears')} className={input} />
          </label>
          <label className="block">
            <span className="text-sm font-medium">Availability</span>
            <select value={form.availability} onChange={set('availability')} className={input}>
              <option value="AVAILABLE">Available</option>
              <option value="PARTIAL">Partial</option>
              <option value="UNAVAILABLE">Unavailable</option>
            </select>
          </label>
          <label className="block">
            <span className="text-sm font-medium">Hourly rate ($)</span>
            <input type="number" min={0} value={form.hourlyRate} onChange={set('hourlyRate')} className={input} />
          </label>
        </div>
        <div>
          <p className="mb-2 text-sm font-medium">Skills + level</p>
          <div className="space-y-2">
            {skills.map((skill) => (
              <div key={skill.id} className="flex items-center gap-3 rounded-xl border border-slate-200 px-3 py-2">
                <input type="checkbox" checked={skill.id in picked} onChange={() => toggle(skill.id)} className="h-4 w-4" />
                <span className="flex-1 text-sm font-medium">{skill.name}</span>
                {skill.id in picked && (
                  <select
                    value={picked[skill.id]}
                    onChange={(e) => setPicked((p) => ({ ...p, [skill.id]: e.target.value }))}
                    className="rounded-lg border border-slate-300 px-2 py-1 text-sm"
                  >
                    {LEVELS.map((l) => (
                      <option key={l} value={l}>{l}</option>
                    ))}
                  </select>
                )}
              </div>
            ))}
          </div>
        </div>
        <button type="submit" disabled={busy} className="w-full rounded-xl bg-brand-600 py-3 font-semibold text-white hover:bg-brand-700 disabled:opacity-60">
          {busy ? 'Saving…' : 'Save profile'}
        </button>
      </form>
    </div>
  );
}
