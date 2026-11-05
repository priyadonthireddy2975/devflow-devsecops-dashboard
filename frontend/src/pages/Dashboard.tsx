import { Link } from 'react-router-dom'
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { Activity, Box, CheckCircle2, Clock, GitBranch, RefreshCw, ShieldAlert, Timer, XCircle } from 'lucide-react'
import { api } from '../api'
import { formatDuration, shortDate, shortSha, timeAgo } from '../format'
import { useAsync } from '../hooks'
import type { DashboardSummary, Page, Run, TrendPoint } from '../types'
import { Empty, ErrorBox, Loading, PageHeader, Panel, SEVERITY_COLORS, StatCard, StatusBadge } from '../ui'

const SEVERITIES = [
  { key: 'critical', label: 'Critical' },
  { key: 'high', label: 'High' },
  { key: 'medium', label: 'Medium' },
  { key: 'low', label: 'Low' },
] as const

export default function Dashboard() {
  const summary = useAsync(() => api<DashboardSummary>('/api/dashboard'), [], 30000)
  const trend = useAsync(() => api<TrendPoint[]>('/api/dashboard/trend?days=14'), [], 30000)
  const recent = useAsync(() => api<Page<Run>>('/api/runs?size=6'), [], 30000)

  if (summary.loading && !summary.data) return <Loading label="Loading dashboard" />
  if (summary.error && !summary.data) return <ErrorBox message={summary.error} />
  const s = summary.data
  if (!s) return null

  const severityMax = Math.max(1, s.security.critical, s.security.high, s.security.medium, s.security.low)
  const chartData = (trend.data ?? []).map((p) => ({ ...p, label: shortDate(p.date) }))
  const hasTrend = chartData.some((p) => p.success + p.failed > 0)

  return (
    <>
      <PageHeader
        title="Overview"
        subtitle="Pipeline health and security posture across all tracked repositories. Refreshes every 30 seconds."
        actions={
          <button className="btn btn-ghost" onClick={() => void Promise.all([summary.reload(), trend.reload(), recent.reload()])}>
            <RefreshCw size={15} /> Refresh
          </button>
        }
      />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Pipeline runs" value={s.totalRuns} hint="All recorded runs" Icon={Activity} tone="indigo" />
        <StatCard
          label="Success rate"
          value={`${s.successRate}%`}
          hint={`${s.successfulRuns} passed of ${s.successfulRuns + s.failedRuns} finished`}
          Icon={CheckCircle2}
          tone="emerald"
        />
        <StatCard label="Failed runs" value={s.failedRuns} hint="Need attention" Icon={XCircle} tone="rose" />
        <StatCard label="Active now" value={s.activeRuns} hint="Running or queued" Icon={Clock} tone="amber" />
        <StatCard label="Avg. duration" value={formatDuration(s.averageDurationSeconds)} hint="Finished runs" Icon={Timer} tone="sky" />
        <StatCard label="Repositories" value={s.repositories} hint="Tracked on GitHub" Icon={GitBranch} tone="slate" />
        <StatCard
          label="Docker containers"
          value={s.dockerAvailable ? s.containersRunning : '-'}
          hint={s.dockerAvailable ? 'Running on this host' : 'Docker socket not connected'}
          Icon={Box}
          tone="sky"
        />
        <StatCard
          label="Critical vulnerabilities"
          value={s.security.critical}
          hint="From the latest scan of each tool"
          Icon={ShieldAlert}
          tone={s.security.critical > 0 ? 'rose' : 'emerald'}
        />
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <Panel title="Pipeline runs, last 14 days" subtitle="Finished runs per day">
            <div className="h-72 px-3 pb-3 pt-5">
              {hasTrend ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={chartData} margin={{ top: 4, right: 12, left: -16, bottom: 0 }}>
                    <CartesianGrid vertical={false} stroke="#eef1f6" />
                    <XAxis dataKey="label" tickLine={false} axisLine={false} tick={{ fontSize: 12, fill: '#64748b' }} interval="preserveStartEnd" />
                    <YAxis allowDecimals={false} tickLine={false} axisLine={false} tick={{ fontSize: 12, fill: '#64748b' }} />
                    <Tooltip cursor={{ fill: 'rgba(99,102,241,0.06)' }} contentStyle={{ borderRadius: 10, border: '1px solid #e6eaf2', fontSize: 13 }} />
                    <Legend iconType="circle" wrapperStyle={{ fontSize: 13, paddingTop: 8 }} />
                    <Bar dataKey="success" name="Success" stackId="runs" fill="#059669" maxBarSize={28} />
                    <Bar dataKey="failed" name="Failed" stackId="runs" fill="#e11d48" maxBarSize={28} radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <Empty title="No finished runs yet" text="Runs appear here once GitHub sends a workflow_run webhook or your CI reports a result." />
              )}
            </div>
          </Panel>
        </div>

        <Panel
          title="Open vulnerabilities"
          subtitle="Latest scan of each tool per repository"
          action={
            <Link to="/security" className="text-xs font-semibold text-indigo-600 hover:text-indigo-700">
              Details
            </Link>
          }
        >
          <ul className="space-y-4 px-5 py-5">
            {SEVERITIES.map(({ key, label }) => (
              <li key={key}>
                <div className="mb-1.5 flex items-center justify-between text-sm">
                  <span className="font-medium text-slate-700">{label}</span>
                  <span className="font-semibold text-slate-900">{s.security[key]}</span>
                </div>
                <div className="h-2 overflow-hidden rounded-full bg-slate-100">
                  <div
                    className="h-full rounded-full"
                    style={{ width: `${(s.security[key] / severityMax) * 100}%`, background: SEVERITY_COLORS[key] }}
                  />
                </div>
              </li>
            ))}
          </ul>
        </Panel>
      </div>

      <div className="mt-6">
        <Panel
          title="Recent pipeline runs"
          action={
            <Link to="/pipelines" className="text-xs font-semibold text-indigo-600 hover:text-indigo-700">
              View all
            </Link>
          }
        >
          {recent.data && recent.data.content.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[640px]">
                <thead>
                  <tr>
                    <th className="th">Repository</th>
                    <th className="th">Branch</th>
                    <th className="th">Commit</th>
                    <th className="th">Status</th>
                    <th className="th">Duration</th>
                    <th className="th">Started</th>
                  </tr>
                </thead>
                <tbody>
                  {recent.data.content.map((r) => (
                    <tr key={r.id}>
                      <td className="td font-medium text-slate-900">{r.repository}</td>
                      <td className="td">{r.branch ?? '-'}</td>
                      <td className="td font-mono text-xs">{shortSha(r.commitSha)}</td>
                      <td className="td">
                        <StatusBadge status={r.status} />
                      </td>
                      <td className="td">{formatDuration(r.durationSeconds)}</td>
                      <td className="td">{timeAgo(r.startedAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <Empty title="No pipeline runs yet" text="Connect the GitHub webhook in Settings, or report runs from your CI pipeline." />
          )}
        </Panel>
      </div>
    </>
  )
}
