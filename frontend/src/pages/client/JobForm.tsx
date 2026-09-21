import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import { api } from '../../api/client';
import type { Job } from '../../api/types';
import { SkillPicker, skillIdsByName, useSkills } from './shared';

export function JobForm() {
  const { id } = useParams();
  const editing = Boolean(id);
  const navigate = useNavigate();
  const skills = useSkills();
  const [form, setForm] = useState({
    title: '',
    description: '',
    creativeBrief: '',
    styleKeywords: '',
    referenceLinks: '',
    budgetMin: '',
    budgetMax: '',
    deadline: '',
  });
  const [skillIds, setSkillIds] = useState<number[]>([]);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!id) return;
    api.get<Job>(`/jobs/${id}`).then((res) => {
      const job = res.data;
      setForm({
        title: job.title,
        description: job.description ?? '',
        creativeBrief: job.creativeBrief,
        styleKeywords: job.styleKeywords ?? '',
        referenceLinks: job.referenceLinks ?? '',
        budgetMin: job.budgetMin?.toString() ?? '',
        budgetMax: job.budgetMax?.toString() ?? '',
        deadline: job.deadline ?? '',
      });
      setSkillIds(skillIdsByName(skills, job.skills));
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const set = (key: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
    setForm((f) => ({ ...f, [key]: e.target.value }));

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    const body = {
      title: form.title,
      description: form.description || undefined,
      creativeBrief: form.creativeBrief,
      styleKeywords: form.styleKeywords || undefined,
      referenceLinks: form.referenceLinks || undefined,
      budgetMin: form.budgetMin ? Number(form.budgetMin) : undefined,
      budgetMax: form.budgetMax ? Number(form.budgetMax) : undefined,
      deadline: form.deadline || undefined,
      skillIds,
    };
    try {
      const res = editing
        ? await api.put<Job>(`/jobs/${id}`, body)
        : await api.post<Job>('/jobs', body);
      navigate(`/client/jobs/${res.data.id}`);
    } catch (err) {
      const message =
        (axios.isAxiosError(err) && (err.response?.data as { message?: string })?.message) ||
        'Could not save the job. Check the brief, skills, and budget.';
      setError(message);
    } finally {
      setBusy(false);
    }
  };

  const input =
    'mt-1 block w-full rounded-xl border border-slate-300 px-4 py-2.5 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100';

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="text-3xl font-extrabold tracking-tight">{editing ? 'Edit job' : 'Post a job'}</h1>
      <p className="mt-1 text-ink-600">A sharp brief is what kills style mismatch. Be specific.</p>
      <form onSubmit={submit} className="mt-6 space-y-4">
        {error && <p className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>}
        <label className="block">
          <span className="text-sm font-medium">Title *</span>
          <input required value={form.title} onChange={set('title')} className={input} />
        </label>
        <label className="block">
          <span className="text-sm font-medium">Description</span>
          <textarea rows={3} value={form.description} onChange={set('description')} className={input} />
        </label>
        <label className="block">
          <span className="text-sm font-medium">Creative brief * — style, pacing, must-haves</span>
          <textarea required rows={4} value={form.creativeBrief} onChange={set('creativeBrief')} className={input} />
        </label>
        <div className="grid grid-cols-2 gap-4">
          <label className="block">
            <span className="text-sm font-medium">Style keywords</span>
            <input value={form.styleKeywords} onChange={set('styleKeywords')} placeholder="cinematic, punchy" className={input} />
          </label>
          <label className="block">
            <span className="text-sm font-medium">Reference links</span>
            <input value={form.referenceLinks} onChange={set('referenceLinks')} placeholder="https://…" className={input} />
          </label>
        </div>
        <div className="grid grid-cols-3 gap-4">
          <label className="block">
            <span className="text-sm font-medium">Budget min ($)</span>
            <input type="number" min={0} value={form.budgetMin} onChange={set('budgetMin')} className={input} />
          </label>
          <label className="block">
            <span className="text-sm font-medium">Budget max ($)</span>
            <input type="number" min={0} value={form.budgetMax} onChange={set('budgetMax')} className={input} />
          </label>
          <label className="block">
            <span className="text-sm font-medium">Deadline</span>
            <input type="date" value={form.deadline} onChange={set('deadline')} className={input} />
          </label>
        </div>
        <div>
          <p className="mb-2 text-sm font-medium">Required skills *</p>
          <SkillPicker selected={skillIds} onChange={setSkillIds} />
        </div>
        <button
          type="submit"
          disabled={busy}
          className="w-full rounded-xl bg-brand-600 py-3 font-semibold text-white hover:bg-brand-700 disabled:opacity-60"
        >
          {busy ? 'Saving…' : editing ? 'Save changes' : 'Publish job'}
        </button>
      </form>
    </div>
  );
}
