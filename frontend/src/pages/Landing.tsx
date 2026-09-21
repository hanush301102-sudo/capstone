import { Link } from 'react-router-dom';
import { Reveal, useScrollY } from '../components/Parallax';

const DIFFERENTIATORS = [
  {
    title: 'Creative Briefs',
    text: 'Every job ships with style keywords and reference links — no more vague "make it cool" posts.',
  },
  {
    title: 'Match Score',
    text: 'Every application is scored on skills, budget fit, availability, and reliability. Screen in minutes.',
  },
  {
    title: 'Verified Portfolio',
    text: 'Work tagged with skills, role, and outcomes — verified by platform admins, not just claimed.',
  },
  {
    title: 'Reliability Record',
    text: 'On-time delivery rate and response time earned from real completed projects.',
  },
  {
    title: 'Creator Radar',
    text: 'Discover creators by skill and availability before you even post a job.',
  },
];

const STEPS = [
  { n: '01', title: 'Post with a brief', text: 'Skills, budget, deadline — plus the style keywords that prevent rework.' },
  { n: '02', title: 'Get scored applications', text: 'Creators answer your brief with tailored samples. Ranked by match score.' },
  { n: '03', title: 'Compare, hire, track', text: 'Side-by-side shortlists, one-click hire, project tracking built in.' },
];

export function Landing() {
  const y = useScrollY();

  return (
    <div className="-mx-4 -my-8">
      {/* HERO with parallax layers */}
      <section className="relative overflow-hidden bg-ink-900 text-white">
        <div
          className="pointer-events-none absolute -left-32 top-10 h-96 w-96 rounded-full bg-brand-600/40 blur-3xl"
          style={{ transform: `translateY(${y * 0.15}px)` }}
        />
        <div
          className="pointer-events-none absolute -right-24 top-64 h-80 w-80 rounded-full bg-fuchsia-500/30 blur-3xl"
          style={{ transform: `translateY(${y * 0.3}px)` }}
        />
        <div className="relative mx-auto grid max-w-7xl gap-12 px-4 pb-28 pt-24 lg:grid-cols-2">
          <div style={{ transform: `translateY(${y * 0.08}px)` }}>
            <p className="mb-4 inline-block rounded-full bg-white/10 px-4 py-1 text-sm text-brand-100">
              Style-first · Verified · Match-scored
            </p>
            <h1 className="text-5xl font-extrabold leading-tight tracking-tight lg:text-6xl">
              Stop fishing through generic applications.
            </h1>
            <p className="mt-6 max-w-lg text-lg text-slate-300">
              Hire video editors, designers, and scriptwriters whose style, skills, and reliability are
              proven — not just promised.
            </p>
            <div className="mt-8 flex flex-wrap gap-4">
              <Link
                to="/register"
                className="rounded-xl bg-brand-600 px-6 py-3 font-semibold hover:bg-brand-500"
              >
                Post a job — free
              </Link>
              <Link
                to="/discover"
                className="rounded-xl border border-white/25 px-6 py-3 font-semibold hover:bg-white/10"
              >
                Find creators
              </Link>
            </div>
          </div>
          {/* Floating creator cards at different parallax speeds */}
          <div className="relative hidden h-96 lg:block">
            <div
              className="absolute left-4 top-6 w-64 rounded-2xl bg-white p-4 text-ink-900 shadow-2xl"
              style={{ transform: `translateY(${y * -0.12}px) rotate(-4deg)` }}
            >
              <div className="h-28 rounded-xl bg-gradient-to-br from-brand-500 to-fuchsia-500" />
              <p className="mt-3 font-bold">Maya R. · Video Editor</p>
              <p className="text-sm text-ink-400">98% on-time · 42 projects</p>
              <p className="mt-2 inline-block rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-bold text-emerald-700">
                Match 94
              </p>
            </div>
            <div
              className="absolute right-4 top-32 w-64 rounded-2xl bg-white p-4 text-ink-900 shadow-2xl"
              style={{ transform: `translateY(${y * -0.22}px) rotate(3deg)` }}
            >
              <div className="h-28 rounded-xl bg-gradient-to-br from-amber-400 to-rose-500" />
              <p className="mt-3 font-bold">Dev K. · Thumbnail Designer</p>
              <p className="text-sm text-ink-400">Verified portfolio · 3.1M views</p>
              <p className="mt-2 inline-block rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-bold text-emerald-700">
                Match 91
              </p>
            </div>
            <div
              className="absolute bottom-0 left-24 w-64 rounded-2xl bg-white p-4 text-ink-900 shadow-2xl"
              style={{ transform: `translateY(${y * -0.05}px) rotate(-2deg)` }}
            >
              <div className="h-28 rounded-xl bg-gradient-to-br from-emerald-400 to-brand-600" />
              <p className="mt-3 font-bold">Sana P. · Scriptwriter</p>
              <p className="text-sm text-ink-400">Responds in ~2h · Short-form expert</p>
              <p className="mt-2 inline-block rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-bold text-emerald-700">
                Match 89
              </p>
            </div>
          </div>
        </div>
        <div
          className="relative border-t border-white/10 bg-white/5 backdrop-blur"
          style={{ transform: `translateY(${Math.max(0, y * 0.05 - 8)}px)` }}
        >
          <div className="mx-auto flex max-w-7xl flex-wrap gap-8 px-4 py-5 text-sm text-slate-300">
            <span><strong className="text-white">15</strong> launch skills catalogued</span>
            <span><strong className="text-white">5-point</strong> transparent match score</span>
            <span><strong className="text-white">&lt;3 min</strong> to post a briefed job</span>
          </div>
        </div>
      </section>

      {/* DIFFERENTIATORS */}
      <section className="mx-auto max-w-7xl px-4 py-20">
        <Reveal>
          <h2 className="text-3xl font-extrabold tracking-tight">Why CreatorHire is different</h2>
          <p className="mt-2 max-w-2xl text-ink-600">
            Generic marketplaces leave you screening spam. CreatorHire backs every hire with evidence.
          </p>
        </Reveal>
        <div className="mt-10 grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {DIFFERENTIATORS.map((d, i) => (
            <Reveal key={d.title} delay={(i % 3) * 100}>
              <div className="h-full rounded-2xl border border-slate-200 p-6 shadow-sm transition hover:-translate-y-1 hover:shadow-lg">
                <h3 className="font-bold">{d.title}</h3>
                <p className="mt-2 text-sm text-ink-600">{d.text}</p>
              </div>
            </Reveal>
          ))}
          <Reveal delay={200}>
            <Link
              to="/register"
              className="flex h-full flex-col justify-center rounded-2xl bg-ink-900 p-6 text-white transition hover:-translate-y-1"
            >
              <h3 className="font-bold">Try it on your next project →</h3>
              <p className="mt-2 text-sm text-slate-300">Post your first briefed job in under 3 minutes.</p>
            </Link>
          </Reveal>
        </div>
      </section>

      {/* HOW IT WORKS */}
      <section className="bg-slate-50 py-20">
        <div className="mx-auto max-w-7xl px-4">
          <Reveal>
            <h2 className="text-3xl font-extrabold tracking-tight">From brief to hired in three steps</h2>
          </Reveal>
          <div className="mt-10 grid gap-6 md:grid-cols-3">
            {STEPS.map((s, i) => (
              <Reveal key={s.n} delay={i * 120}>
                <div className="rounded-2xl bg-white p-6 shadow-sm">
                  <p className="text-4xl font-extrabold text-brand-100">{s.n}</p>
                  <h3 className="mt-2 font-bold">{s.title}</h3>
                  <p className="mt-2 text-sm text-ink-600">{s.text}</p>
                </div>
              </Reveal>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="mx-auto max-w-7xl px-4 py-20 text-center">
        <Reveal>
          <h2 className="text-3xl font-extrabold tracking-tight">Hire proven. Ship faster.</h2>
          <p className="mx-auto mt-2 max-w-xl text-ink-600">
            Join CreatorHire as a client or a creative professional — your first match-scored hire is
            minutes away.
          </p>
          <div className="mt-8 flex justify-center gap-4">
            <Link to="/register" className="rounded-xl bg-brand-600 px-6 py-3 font-semibold text-white hover:bg-brand-700">
              Create free account
            </Link>
            <Link to="/login" className="rounded-xl border border-slate-300 px-6 py-3 font-semibold hover:bg-slate-50">
              Login
            </Link>
          </div>
        </Reveal>
      </section>
    </div>
  );
}
