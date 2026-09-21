import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { api } from '../../api/client';

export interface AdminUser {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  status: string;
  roles: string[];
}

export interface AdminReport {
  id: number;
  reporterEmail: string;
  reportedUserId?: number;
  jobId?: number;
  reason: string;
  description?: string;
  status: string;
  createdAt: string;
}

export interface PendingPortfolio {
  id: number;
  creatorProfileId: number;
  title: string;
  description?: string;
  workType?: string;
  collaborationRole?: string;
  outcomeStats?: string;
  verificationStatus: string;
  skills: string[];
}

function apiError(error: unknown): string {
  if (axios.isAxiosError(error)) {
    return (error.response?.data as { message?: string })?.message ?? 'Action failed.';
  }
  return 'Action failed.';
}

function UsersTab() {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [message, setMessage] = useState('');
  const reload = () => {
    api.get<AdminUser[]>('/admin/users').then((res) => setUsers(res.data)).catch(() => {});
  };
  useEffect(reload, []);

  const setStatus = async (id: number, status: string) => {
    setMessage('');
    try {
      await api.patch(`/admin/users/${id}/status`, { status });
      reload();
    } catch (err) {
      setMessage(apiError(err));
    }
  };

  return (
    <div>
      {message && <p className="mb-4 rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700">{message}</p>}
      <div className="space-y-3">
        {users.map((u) => (
          <div key={u.id} className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-slate-200 p-4">
            <div>
              <p className="font-bold">{u.email}</p>
              <p className="text-sm text-ink-400">
                {u.firstName} {u.lastName} · {u.roles.join(', ')} · {u.status}
              </p>
            </div>
            <div className="flex gap-2">
              {u.status !== 'ACTIVE' && (
                <button onClick={() => void setStatus(u.id, 'ACTIVE')} className="rounded-lg bg-emerald-600 px-3 py-1.5 text-xs font-semibold text-white">
                  Activate
                </button>
              )}
              {u.status !== 'SUSPENDED' && (
                <button onClick={() => void setStatus(u.id, 'SUSPENDED')} className="rounded-lg bg-amber-500 px-3 py-1.5 text-xs font-semibold text-white">
                  Suspend
                </button>
              )}
              {u.status !== 'DEACTIVATED' && (
                <button onClick={() => void setStatus(u.id, 'DEACTIVATED')} className="rounded-lg border border-red-200 px-3 py-1.5 text-xs font-semibold text-red-600">
                  Deactivate
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function ReportsTab() {
  const [reports, setReports] = useState<AdminReport[]>([]);
  const [filter, setFilter] = useState('PENDING');
  const reload = () => {
    api.get<AdminReport[]>('/admin/reports', { params: filter === 'ALL' ? {} : { status: filter } })
      .then((res) => setReports(res.data))
      .catch(() => {});
  };
  useEffect(reload, [filter]);

  const review = async (id: number, status: string) => {
    await api.patch(`/admin/reports/${id}`, { status }).catch(() => {});
    reload();
  };

  return (
    <div>
      <div className="mb-4 flex gap-2">
        {['PENDING', 'REVIEWED', 'DISMISSED', 'ALL'].map((s) => (
          <button
            key={s}
            onClick={() => setFilter(s)}
            className={`rounded-lg px-3 py-1.5 text-sm font-semibold ${filter === s ? 'bg-ink-900 text-white' : 'bg-slate-100 hover:bg-slate-200'}`}
          >
            {s}
          </button>
        ))}
      </div>
      {reports.length ? (
        <div className="space-y-3">
          {reports.map((r) => (
            <div key={r.id} className="rounded-2xl border border-slate-200 p-4">
              <p className="font-bold">{r.reason} <span className="ml-2 text-xs font-semibold text-ink-400">{r.status}</span></p>
              <p className="mt-1 text-sm text-ink-600">{r.description}</p>
              <p className="mt-1 text-xs text-ink-400">
                By {r.reporterEmail}
                {r.reportedUserId ? ` · User #${r.reportedUserId}` : ''}
                {r.jobId ? ` · Job #${r.jobId}` : ''}
              </p>
              {r.status === 'PENDING' && (
                <div className="mt-3 flex gap-2">
                  <button onClick={() => void review(r.id, 'REVIEWED')} className="rounded-lg bg-brand-600 px-3 py-1.5 text-xs font-semibold text-white">
                    Mark reviewed
                  </button>
                  <button onClick={() => void review(r.id, 'DISMISSED')} className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-semibold">
                    Dismiss
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      ) : (
        <p className="rounded-2xl border border-dashed border-slate-300 p-8 text-center text-ink-400">No reports here.</p>
      )}
    </div>
  );
}

function PortfoliosTab() {
  const [items, setItems] = useState<PendingPortfolio[]>([]);
  const reload = () => {
    api.get<PendingPortfolio[]>('/admin/portfolios/pending').then((res) => setItems(res.data)).catch(() => {});
  };
  useEffect(reload, []);

  const decide = async (id: number, approve: boolean) => {
    await api.patch(`/admin/portfolios/${id}/${approve ? 'verify' : 'reject-verification'}`).catch(() => {});
    reload();
  };

  return (
    <div>
      {items.length ? (
        <div className="space-y-3">
          {items.map((item) => (
            <div key={item.id} className="rounded-2xl border border-slate-200 p-4">
              <p className="font-bold">{item.title}</p>
              <p className="mt-1 text-sm text-ink-600">{item.description}</p>
              <p className="mt-1 text-xs text-ink-400">
                Creator #{item.creatorProfileId} · {item.workType} · {item.collaborationRole} · {item.outcomeStats} · {item.skills.join(', ')}
              </p>
              <div className="mt-3 flex gap-2">
                <button onClick={() => void decide(item.id, true)} className="rounded-lg bg-emerald-600 px-3 py-1.5 text-xs font-semibold text-white">
                  Verify
                </button>
                <button onClick={() => void decide(item.id, false)} className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-semibold">
                  Reject
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p className="rounded-2xl border border-dashed border-slate-300 p-8 text-center text-ink-400">
          No portfolios awaiting verification.
        </p>
      )}
    </div>
  );
}

const TABS = [
  { id: 'users', title: 'Users' },
  { id: 'reports', title: 'Reports' },
  { id: 'portfolios', title: 'Portfolio verification' },
];

export function AdminDashboard() {
  const [tab, setTab] = useState('users');
  return (
    <div>
      <h1 className="text-3xl font-extrabold tracking-tight">Admin</h1>
      <p className="mt-1 text-ink-600">Moderate users, reports, and portfolio verification.</p>
      <div className="mt-6 flex gap-2">
        {TABS.map((t) => (
          <button
            key={t.id}
            onClick={() => setTab(t.id)}
            className={`rounded-xl px-5 py-2 font-semibold ${tab === t.id ? 'bg-ink-900 text-white' : 'bg-slate-100 text-ink-600 hover:bg-slate-200'}`}
          >
            {t.title}
          </button>
        ))}
      </div>
      <div className="mt-6">
        {tab === 'users' && <UsersTab />}
        {tab === 'reports' && <ReportsTab />}
        {tab === 'portfolios' && <PortfoliosTab />}
      </div>
      <p className="mt-6 text-sm text-ink-400">
        Filing a report? <Link to="/report" className="text-brand-600 hover:underline">Report a user or job</Link>
      </p>
    </div>
  );
}
