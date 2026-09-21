export function Placeholder({ title }: { title: string }) {
  return (
    <div className="rounded-2xl border border-dashed border-slate-300 p-12 text-center">
      <h1 className="text-2xl font-bold">{title}</h1>
      <p className="mt-2 text-ink-400">This screen ships in the next frontend task.</p>
    </div>
  );
}
