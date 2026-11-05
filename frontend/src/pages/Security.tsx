import { useState } from 'react'
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { ExternalLink, ShieldAlert } from 'lucide-react'
import { api } from '../api'
import { shortSha, timeAgo } from '../format'
import { useAsync } from '../hooks'
import type { Page, Scan, SecuritySummary } from '../types'
import { Empty, ErrorBox, Loading, PageHeader, Panel, SEVERITY_COLORS } from '../ui'

const SEVERITIES = [
  { key: 'critical', label: 'Critical' },
  { key: 'high', label: 'High' },
  { key: 'medium', label: 'Medium' },
  { key: 'low', label: 'Low' },
] as const

function SeverityCount({ value, color }: { value: number; color: string }) {
  return (
    <span
      className="inline-flex min-w-8 justify-center rounded-md px-2 py-0.5 text-xs font-bold"
      style={value > 0 ? { background: `${color}1a`, color } : { background: '#f1f5f9', color: '#94a3b8' }}
    >
      {value}
    </span>
  )
}

export default function Security() {
  const [tool, setTool] = useState('')
  const [page, setPage] = useState(0)

  const summary = useAsync(() => api<SecuritySummary>('/api/scans/summary'), [], 30000)
  const scans = useAsync(() => {
    const params = new URLSearchParams({ page: String(page), size: '10' })
    if (tool) params.set('tool', tool)
    return api<Page<Scan>>(`/api/scans?${params.toString()}`)
  }, [tool, page])

  if (summary.loading && !summary.data) return <Loading />
  if (summary.error && !summary.data) return <ErrorBox message={summary.error} />
  const s = summary.data
  if (!s) return null

  const chartData = s.tools.map((t) => ({ ...t, name: t.tool }))

  return (
    <>
      <PageHeader title="Security" subtitle="Findings from the latest scan of each tool for every repository." />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {SEVERITIES.map(({ key, label }) => (
          <div key={key} className="card flex items-center gap-4 p-5">
            <span className="rounded-xl p-3" style={{ background: `${SEVERITY_COLORS[key]}1a`, color: SEVERITY_COLORS[key] }}>
              <ShieldAlert size={22} />
            </span>
            <div>
              <p className="text-sm font-medium text-slate-500">{label}</p>
              <p className="text-3xl font-bold tracking-tight text-slate-900">{s[key]}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="mt-6">
        <Panel title="Findings by scanner" subtitle="Stacked by severity">
          <div className="px-3 pb-3 pt-5" style={{ height: Math.max(220, chartData.length * 64 + 70) }}>
            {chartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData} layout="vertical" margin={{ top: 0, right: 16, left: 24, bottom: 0 }}>
                  <CartesianGrid horizontal={false} stroke="#eef1f6" />
                  <XAxis type="number" allowDecimals={false} tickLine={false} axisLine={false} tick={{ fontSize: 12, fill: '#64748b' }} />
                  <YAxis type="category" dataKey="name" width={150} tickLine={false} axisLine={false} tick={{ fontSize: 13, fill: '#334155' }} />
                  <Tooltip cursor={{ fill: 'rgba(99,102,241,0.06)' }} contentStyle={{ borderRadius: 10, border: '1px solid #e6eaf2', fontSize: 13 }} />
                  <Legend iconType="circle" wrapperStyle={{ fontSize: 13 }} />
                  <Bar dataKey="critical" name="Critical" stackId="s" fill={SEVERITY_COLORS.critical} barSize={22} />
                  <Bar dataKey="high" name="High" stackId="s" fill={SEVERITY_COLORS.high} />
                  <Bar dataKey="medium" name="Medium" stackId="s" fill={SEVERITY_COLORS.medium} />
                  <Bar dataKey="low" name="Low" stackId="s" fill={SEVERITY_COLORS.low} radius={[0, 4, 4, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <Empty title="No scan results yet" text="Your CI pipeline posts scanner summaries to /api/scans. See Settings for the setup snippet." />
            )}
          </div>
        </Panel>
      </div>

      <div className="mt-6">
        <Panel
          title="Scan history"
          action={
            <select
              className="input !w-auto"
              value={tool}
              onChange={(e) => {
                setTool(e.target.value)
                setPage(0)
              }}
              aria-label="Filter by scanner"
            >
              <option value="">All scanners</option>
              {s.tools.map((t) => (
                <option key={t.tool} value={t.tool}>
                  {t.tool}
                </option>
              ))}
            </select>
          }
        >
          {scans.data && scans.data.content.length > 0 ? (
            <>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[720px]">
                  <thead>
                    <tr>
                      <th className="th">Repository</th>
                      <th className="th">Scanner</th>
                      <th className="th">Commit</th>
                      <th className="th">Critical</th>
                      <th className="th">High</th>
                      <th className="th">Medium</th>
                      <th className="th">Low</th>
                      <th className="th">Scanned</th>
                      <th className="th" />
                    </tr>
                  </thead>
                  <tbody>
                    {scans.data.content.map((x) => (
                      <tr key={x.id}>
                        <td className="td font-medium text-slate-900">{x.repository}</td>
                        <td className="td">{x.tool}</td>
                        <td className="td font-mono text-xs">{shortSha(x.commitSha)}</td>
                        <td className="td">
                          <SeverityCount value={x.critical} color={SEVERITY_COLORS.critical} />
                        </td>
                        <td className="td">
                          <SeverityCount value={x.high} color={SEVERITY_COLORS.high} />
                        </td>
                        <td className="td">
                          <SeverityCount value={x.medium} color={SEVERITY_COLORS.medium} />
                        </td>
                        <td className="td">
                          <SeverityCount value={x.low} color={SEVERITY_COLORS.low} />
                        </td>
                        <td className="td whitespace-nowrap">{timeAgo(x.scannedAt)}</td>
                        <td className="td">
                          {x.reportUrl && /^https:\/\//.test(x.reportUrl) && (
                            <a href={x.reportUrl} target="_blank" rel="noopener noreferrer" className="text-slate-400 hover:text-indigo-600" aria-label="Open report">
                              <ExternalLink size={15} />
                            </a>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="flex items-center justify-between px-5 py-3 text-sm text-slate-500">
                <span>
                  Page {scans.data.number + 1} of {Math.max(1, scans.data.totalPages)}
                </span>
                <div className="flex gap-2">
                  <button className="btn btn-ghost" disabled={page === 0} onClick={() => setPage(page - 1)}>
                    Prev
                  </button>
                  <button className="btn btn-ghost" disabled={page + 1 >= scans.data.totalPages} onClick={() => setPage(page + 1)}>
                    Next
                  </button>
                </div>
              </div>
            </>
          ) : (
            <Empty title="No scans recorded" text="Scan results will list here after your pipeline reports them." />
          )}
        </Panel>
      </div>
    </>
  )
}
