import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import axios from 'axios';
import { api } from '../../api/client';
import type { PortfolioItem, Skill } from '../../api/types';

const EMPTY = { title: '', description: '', mediaUrl: '', workType: 'VIDEO', collaborationRole: '', outcomeStats: '' };

export function PortfolioManager() {
  const [items, setItems] = useState<PortfolioItem[]>([]);
  const [skills, setSkills] = useState<Skill[]>([]);
  const [form, setForm] = useState(EMPTY);
  const [skillIds, setSkillIds] = useState<number[]>([]);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [message, setMessage] = useState('');

  const reload = () => {
    api.get<PortfolioItem[]>('/portfolios/mine').then((res) => setItems(res.data)).catch(() => {});
  };

  useEffect(() => {
    reload();
    api.get<Skill[]>('/skills').then((res) => setSkills(res.data)).catch(() => {});
  }, []);

  const startEdit = (item: PortfolioItem) => {
    setEditingId(item.id);
    setForm({
      title: item.title,
      description: item.description ?? '',
      mediaUrl: item.mediaUrl ?? '',
      workType: item.workType ?? 'VIDEO',
      collaborationRole: item.collaborationRole ?? '',
      outcomeStats: item.outcomeStats ?? '',
    });
    const map = new Map(skills.map((s) => [s.name, s.id]));
    setSkillIds(item.skills.map((n) => map.get(n)).filter((id): id is number => id !== undefined));
  };

  const reset = () => {
    setEditingId(null);
    setForm(EMPTY);
    setSkillIds([]);
  };

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setMessage('');
    const body = { ...form, skillIds };
    try {
      if (editingId) await api.put(`/portfolios/${editingId}`, body);
      else await api.post('/portfolios', body);
      reset();
      reload();
    } catch (err) {
      setMessage(
        (axios.isAxiosError(err) && (err.response?.data as { message?: string })?.message) ||
          'Could not save portfolio item.',
      );
    }
  };

  const remove = async (id: number) => {
    if (!confirm('Delete this portfolio item?')) return;
    await api.delete(`/portfolios/${id}`).catch(() => {});
    reload();
  };

  const requestVerification = async (id: number) => {
    setMessage('');
    try {
      await api.patch(`/portfolios/${id}/request-verification`);
      reload();
    } catch (err) {
      setMessage(
        (axios.isAxiosError(err) && (err.response?.data as { message?: string })?.message) ||
          'Could not request verification.',
      );
    }
  };

  const set = (key: keyof typeof EMPTY) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
    setForm((f) => ({ ...f, [key]: e.target.value }));

  const input =
    'mt-1 block w-full rounded-xl border border-slate-300 px-4 py-2.5 outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100';

  return (
    <div>
      <h1 className="text-3xl font-extrabold tracking-tight">Portfolio</h1>
      <p className="mt-1 text-ink-600">Tag work with skills, then request verification to earn the badge.</p>
      {message && <p className="mt-4 rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700">{message}</p>}

      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <form onSubmit={submit} className="space-y-3 rounded-2xl border border-slate-200 p-5">
          <h2 className="font-bold">{editingId ? 'Edit item' : 'Add work'}</h2>
          <label className="block">
            <span className="text-sm font-medium">Title *</span>
            <input required value={form.title} onChange={set('title')} className={input} />
          </label>
          <label className="block">
            <span className="text-sm font-medium">Description</span>
            <textarea rows={2} value={form.description} onChange={set('description')} className={input} />
          </label>
          <div className="grid grid-cols-2 gap-3">
            <label className="block">
              <span className="text-sm font-medium">Media URL</span>
              <input value={form.mediaUrl} onChange={set('mediaUrl')} placeholder="https://…" className={input} />
            </label>
            <label className="block">
              <span className="text-sm font-medium">Work type</span>
              <select value={form.workType} onChange={set('workType')} className={input}>
                <option value="VIDEO">Video</option>
                <option value="DESIGN">Design</option>
                <option value="SCRIPT">Script</option>
              </select>
            </label>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <label className="block">
              <span className="text-sm font-medium">Your role</span>
              <input value={form.collaborationRole} onChange={set('collaborationRole')} placeholder="Lead Editor" className={input} />
            </label>
            <label className="block">
              <span className="text-sm font-medium">Outcome</span>
              <input value={form.outcomeStats} onChange={set('outcomeStats')} placeholder="2M views" className={input} />
            </label>
          </div>
          <div>
            <p className="mb-2 text-sm font-medium">Skill tags</p>
            <div className="flex flex-wrap gap-2">
              {skills.map((s) => (
                <button
                  type="button"
                  key={s.id}
                  onClick={() =>
                    setSkillIds((prev) => (prev.includes(s.id) ? prev.filter((x) => x !== s.id) : [...prev, s.id]))
                  }
                  className={`rounded-full px-3 py-1 text-sm ${skillIds.includes(s.id) ? 'bg-brand-600 text-white' : 'bg-slate-100 hover:bg-slate-200'}`}
                >
                  {s.name}
                </button>
              ))}
            </div>
          </div>
          <div className="flex gap-2">
            <button type="submit" className="flex-1 rounded-xl bg-brand-600 py-2.5 font-semibold text-white hover:bg-brand-700">
              {editingId ? 'Save' : 'Add'}
            </button>
            {editingId && (
              <button type="button" onClick={reset} className="rounded-xl border border-slate-300 px-4 py-2.5 hover:bg-slate-50">
                Cancel
              </button>
            )}
          </div>
        </form>

        <div className="space-y-3">
          {items.length ? items.map((item) => (
            <div key={item.id} className="rounded-2xl border border-slate-200 p-4">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <p className="font-bold">{item.title}</p>
                  <p className="text-xs text-ink-400">
                    {item.workType} · {item.collaborationRole} · {item.outcomeStats} · {item.verificationStatus}
                  </p>
                </div>
              </div>
              <div className="mt-3 flex flex-wrap gap-2">
                <button onClick={() => startEdit(item)} className="rounded-lg border border-slate-300 px-3 py-1 text-xs font-semibold hover:bg-slate-50">
                  Edit
                </button>
                <button onClick={() => void remove(item.id)} className="rounded-lg border border-red-200 px-3 py-1 text-xs font-semibold text-red-600 hover:bg-red-50">
                  Delete
                </button>
                {item.verificationStatus === 'UNVERIFIED' && (
                  <button onClick={() => void requestVerification(item.id)} className="rounded-lg bg-ink-900 px-3 py-1 text-xs font-semibold text-white">
                    Request verification
                  </button>
                )}
              </div>
            </div>
          )) : (
            <p className="rounded-2xl border border-dashed border-slate-300 p-8 text-center text-ink-400">
              No work showcased yet. Add your best pieces.
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
