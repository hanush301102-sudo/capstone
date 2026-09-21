import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import type { NotificationItem } from '../api/types';

export function useNotifications() {
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [unread, setUnread] = useState(0);
  const reload = () => {
    api.get<NotificationItem[]>('/notifications/mine').then((res) => setItems(res.data)).catch(() => {});
    api.get<number>('/notifications/unread-count').then((res) => setUnread(res.data)).catch(() => {});
  };
  useEffect(reload, []);
  const markRead = async (id: number) => {
    await api.patch(`/notifications/${id}/read`).catch(() => {});
    reload();
  };
  const markAllRead = async () => {
    await Promise.all(items.filter((n) => !n.read).map((n) => api.patch(`/notifications/${n.id}/read`).catch(() => {})));
    reload();
  };
  return { items, unread, reload, markRead, markAllRead };
}

export function NotificationBell() {
  const { unread } = useNotifications();
  return (
    <Link to="/notifications" className="relative rounded-lg border border-slate-300 px-3 py-1.5 hover:bg-slate-50" title="Notifications">
      🔔
      {unread > 0 && (
        <span className="absolute -right-2 -top-2 flex h-5 min-w-5 items-center justify-center rounded-full bg-red-600 px-1 text-[11px] font-bold text-white">
          {unread}
        </span>
      )}
    </Link>
  );
}

export function NotificationsPage() {
  const { items, markRead, markAllRead } = useNotifications();
  return (
    <div className="mx-auto max-w-2xl">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-extrabold tracking-tight">Notifications</h1>
        <button onClick={() => void markAllRead()} className="rounded-xl border border-slate-300 px-4 py-2 text-sm font-semibold hover:bg-slate-50">
          Mark all read
        </button>
      </div>
      {items.length ? (
        <div className="mt-6 space-y-3">
          {items.map((n) => (
            <button
              key={n.id}
              onClick={() => void markRead(n.id)}
              className={`block w-full rounded-2xl border p-4 text-left ${n.read ? 'border-slate-200' : 'border-brand-200 bg-brand-50'}`}
            >
              <p className="text-sm font-medium">{n.message}</p>
              <p className="mt-1 text-xs text-ink-400">{n.type} · {new Date(n.createdAt).toLocaleString()}</p>
            </button>
          ))}
        </div>
      ) : (
        <p className="mt-6 rounded-2xl border border-dashed border-slate-300 p-10 text-center text-ink-400">
          Nothing yet. Applications, shortlists, hires, and project updates land here.
        </p>
      )}
    </div>
  );
}
