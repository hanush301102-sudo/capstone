import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../../api/client';
import type { CreatorCard, PortfolioItem } from '../../api/types';
import { StatusBadge } from './shared';

export function PublicProfile() {
  const { id } = useParams();
  const [card, setCard] = useState<CreatorCard | null>(null);
  const [portfolio, setPortfolio] = useState<PortfolioItem[]>([]);

  useEffect(() => {
    if (!id) return;
    api.get<CreatorCard[]>('/discovery/creators').then((res) => {
      setCard(res.data.find((c) => c.id === Number(id)) ?? null);
    }).catch(() => {});
    api.get<PortfolioItem[]>(`/portfolios/creator/${id}`).then((res) => setPortfolio(res.data)).catch(() => {});
  }, [id]);

  if (!card) return <p className="text-ink-400">Loading profile…</p>;

  return (
    <div>
      <Link to="/discover" className="text-sm text-brand-600 hover:underline">← Discover</Link>
      <div className="mt-2 rounded-2xl border border-slate-200 p-6">
        <h1 className="text-3xl font-extrabold tracking-tight">{card.headline ?? 'Creative professional'}</h1>
        <p className="mt-2 text-ink-600">{card.bio}</p>
        <div className="mt-3 flex flex-wrap gap-1.5">
          {card.skills.map((s) => (
            <span key={s} className="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-medium text-brand-700">{s}</span>
          ))}
        </div>
        <div className="mt-4 flex flex-wrap gap-6 text-sm">
          <span><strong>{card.onTimeDeliveryRate}%</strong> on-time</span>
          <span><strong>~{card.avgResponseTimeHours}h</strong> response</span>
          <span><strong>{card.completedProjects}</strong> projects</span>
          <span><strong>{card.hourlyRate != null ? `$${card.hourlyRate}/hr` : 'Rate on request'}</strong></span>
          <span>{card.availability}</span>
        </div>
      </div>

      <h2 className="mt-8 text-xl font-bold">Portfolio ({portfolio.length})</h2>
      {portfolio.length ? (
        <div className="mt-4 grid gap-4 md:grid-cols-2">
          {portfolio.map((item) => (
            <div key={item.id} className="rounded-2xl border border-slate-200 p-5">
              <div className="flex items-start justify-between gap-2">
                <p className="font-bold">{item.title}</p>
                <StatusBadge status={item.verificationStatus} />
              </div>
              {item.description && <p className="mt-2 text-sm text-ink-600">{item.description}</p>}
              <p className="mt-2 text-xs text-ink-400">
                {item.workType} · {item.collaborationRole} · {item.outcomeStats}
              </p>
              {item.mediaUrl && (
                <a href={item.mediaUrl} target="_blank" rel="noreferrer" className="mt-2 inline-block text-sm text-brand-600 hover:underline">
                  View work →
                </a>
              )}
            </div>
          ))}
        </div>
      ) : (
        <p className="mt-4 rounded-2xl border border-dashed border-slate-300 p-8 text-center text-ink-400">
          No public work yet.
        </p>
      )}
    </div>
  );
}
