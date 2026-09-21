import { Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { Application, Project } from '../../api/types';

export interface CreatorSkillView {
  skillId: number;
  skillName: string;
  level: string;
}

export interface CreatorProfile extends CreatorProfileBase {
  skills: CreatorSkillView[];
}

interface CreatorProfileBase {
  id: number;
  headline?: string;
  bio?: string;
  experienceYears?: number;
  availability: string;
  hourlyRate?: number;
  onTimeDeliveryRate: number;
  avgResponseTimeHours: number;
  completedProjects: number;
}

export function useCreatorProfile() {
  const [profile, setProfile] = useState<CreatorProfile | null>(null);
  const reload = () => {
    api.get<CreatorProfile>('/profiles/creator/me').then((res) => setProfile(res.data)).catch(() => {});
  };
  useEffect(reload, []);
  return { profile, reload };
}

export function ReliabilityBanner({ profile }: { profile: CreatorProfile | null }) {
  if (!profile) return null;
  return (
    <div className="grid grid-cols-3 gap-4 rounded-2xl bg-ink-900 p-5 text-white">
      <div className="text-center">
        <p className="text-2xl font-extrabold">{profile.onTimeDeliveryRate}%</p>
        <p className="text-xs text-slate-300">On-time delivery</p>
      </div>
      <div className="text-center">
        <p className="text-2xl font-extrabold">~{profile.avgResponseTimeHours}h</p>
        <p className="text-xs text-slate-300">Avg response</p>
      </div>
      <div className="text-center">
        <p className="text-2xl font-extrabold">{profile.completedProjects}</p>
        <p className="text-xs text-slate-300">Completed</p>
      </div>
    </div>
  );
}

export function useMyApplications() {
  const [applications, setApplications] = useState<Application[]>([]);
  const reload = () => {
    api.get<Application[]>('/applications/mine').then((res) => setApplications(res.data)).catch(() => {});
  };
  useEffect(reload, []);
  return { applications, reload };
}

export function useMyProjects() {
  const [projects, setProjects] = useState<Project[]>([]);
  const reload = () => {
    api.get<Project[]>('/projects/mine').then((res) => setProjects(res.data)).catch(() => {});
  };
  useEffect(reload, []);
  return { projects, reload };
}

export function StatusBadge({ status }: { status: string }) {
  const style =
    status === 'ACCEPTED' || status === 'COMPLETED' || status === 'VERIFIED' || status === 'OPEN'
      ? 'bg-emerald-100 text-emerald-700'
      : status === 'REJECTED' || status === 'CANCELLED'
        ? 'bg-red-100 text-red-700'
        : 'bg-amber-100 text-amber-700';
  return (
    <span className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-semibold ${style}`}>
      {status}
    </span>
  );
}

export function DashboardLinks() {
  const links = [
    { to: '/creator/profile', title: 'Edit profile', text: 'Headline, availability, rate, skills' },
    { to: '/creator/portfolio', title: 'Portfolio', text: 'Showcase work, request verification' },
    { to: '/discover', title: 'Find jobs', text: 'Browse briefs and apply' },
  ];
  return (
    <div className="grid gap-4 md:grid-cols-3">
      {links.map((l) => (
        <Link key={l.to} to={l.to} className="rounded-2xl border border-slate-200 p-5 transition hover:shadow-md">
          <h3 className="font-bold">{l.title}</h3>
          <p className="mt-1 text-sm text-ink-400">{l.text}</p>
        </Link>
      ))}
    </div>
  );
}
