import type { ReactNode } from 'react'
import { AlertTriangle, Ban, CheckCircle2, Clock, Loader2, XCircle, type LucideIcon } from 'lucide-react'
import type { RunStatus } from './types'

export const SEVERITY_COLORS = {
  critical: '#b91c1c',
  high: '#ea580c',
  medium: '#ca8a04',
  low: '#3b82f6',
} as const

const STATUS_STYLES: Record<RunStatus, { label: string; cls: string; Icon: LucideIcon; spin?: boolean }> = {
  SUCCESS: { label: 'Success', cls: 'bg-emerald-50 text-emerald-700 ring-emerald-200', Icon: CheckCircle2 },
  FAILED: { label: 'Failed', cls: 'bg-rose-50 text-rose-700 ring-rose-200', Icon: XCircle },
  RUNNING: { label: 'Running', cls: 'bg-amber-50 text-amber-700 ring-amber-200', Icon: Loader2, spin: true },
  QUEUED: { label: 'Queued', cls: 'bg-slate-100 text-slate-600 ring-slate-200', Icon: Clock },
  CANCELLED: { label: 'Cancelled', cls: 'bg-slate-100 text-slate-500 ring-slate-200', Icon: Ban },
}

export function StatusBadge({ status }: { status: RunStatus }) {
  const s = STATUS_STYLES[status]
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset ${s.cls}`}
    >
      <s.Icon size={13} className={s.spin ? 'animate-spin' : ''} />
      {s.label}
    </span>
  )
}

export function PageHeader({ title, subtitle, actions }: { title: string; subtitle?: string; actions?: ReactNode }) {
  return (
    <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">{title}</h1>
        {subtitle && <p className="mt-1 text-sm text-slate-500">{subtitle}</p>}
      </div>
      {actions && <div className="flex items-center gap-2">{actions}</div>}
    </div>
  )
}

interface StatCardProps {
  label: string
  value: ReactNode
  hint?: string
  Icon: LucideIcon
  tone?: 'indigo' | 'emerald' | 'rose' | 'amber' | 'sky' | 'slate'
}

const TONES = {
  indigo: 'bg-indigo-50 text-indigo-600',
  emerald: 'bg-emerald-50 text-emerald-600',
  rose: 'bg-rose-50 text-rose-600',
  amber: 'bg-amber-50 text-amber-600',
  sky: 'bg-sky-50 text-sky-600',
  slate: 'bg-slate-100 text-slate-600',
}

export function StatCard({ label, value, hint, Icon, tone = 'indigo' }: StatCardProps) {
  return (
    <div className="card p-5">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-slate-500">{label}</p>
          <p className="mt-2 text-3xl font-bold tracking-tight text-slate-900">{value}</p>
        </div>
        <span className={`rounded-xl p-2.5 ${TONES[tone]}`}>
          <Icon size={20} />
        </span>
      </div>
      {hint && <p className="mt-3 text-xs text-slate-500">{hint}</p>}
    </div>
  )
}

export function Panel({ title, subtitle, children, action }: { title: string; subtitle?: string; children: ReactNode; action?: ReactNode }) {
  return (
    <section className="card">
      <header className="flex items-start justify-between gap-3 border-b border-slate-100 px-5 py-4">
        <div>
          <h2 className="text-base font-semibold text-slate-900">{title}</h2>
          {subtitle && <p className="mt-0.5 text-xs text-slate-500">{subtitle}</p>}
        </div>
        {action}
      </header>
      {children}
    </section>
  )
}

export function Loading({ label = 'Loading' }: { label?: string }) {
  return (
    <div className="flex items-center justify-center gap-2 py-16 text-sm text-slate-500">
      <Loader2 size={18} className="animate-spin" /> {label}...
    </div>
  )
}

export function ErrorBox({ message }: { message: string }) {
  return (
    <div className="flex items-center gap-2 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
      <AlertTriangle size={16} /> {message}
    </div>
  )
}

export function Empty({ title, text }: { title: string; text: string }) {
  return (
    <div className="px-6 py-14 text-center">
      <p className="text-sm font-semibold text-slate-700">{title}</p>
      <p className="mx-auto mt-1 max-w-md text-sm text-slate-500">{text}</p>
    </div>
  )
}

export function Logo({ light = false }: { light?: boolean }) {
  return (
    <div className="flex items-center gap-2.5">
      <svg width="34" height="34" viewBox="0 0 32 32" aria-hidden="true">
        <rect width="32" height="32" rx="8" fill="#4f46e5" />
        <path d="M9 10h8a6 6 0 0 1 0 12H9z" fill="none" stroke="#fff" strokeWidth="3" strokeLinejoin="round" />
        <circle cx="23" cy="10" r="2.5" fill="#34d399" />
      </svg>
      <span className={`text-xl font-bold tracking-tight ${light ? 'text-white' : 'text-slate-900'}`}>DevFlow</span>
    </div>
  )
}
