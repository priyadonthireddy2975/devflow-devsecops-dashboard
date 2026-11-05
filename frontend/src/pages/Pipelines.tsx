import { useState } from 'react'
import { ChevronLeft, ChevronRight, ExternalLink } from 'lucide-react'
import { api } from '../api'
import { formatDuration, shortSha, timeAgo } from '../format'
import { useAsync } from '../hooks'
import type { Page, Repo, Run, RunStatus } from '../types'
import { Empty, ErrorBox, Loading, PageHeader, Panel, StatusBadge } from '../ui'

const STATUSES: RunStatus[] = ['SUCCESS', 'FAILED', 'RUNNING', 'QUEUED', 'CANCELLED']

export default function Pipelines() {
  const [status, setStatus] = useState('')
  const [repository, setRepository] = useState('')
  const [branch, setBranch] = useState('')
  const [page, setPage] = useState(0)

  const repos = useAsync(() => api<Repo[]>('/api/repositories'), [])
  const runs = useAsync(
    () => {
      const params = new URLSearchParams({ page: String(page), size: '12' })
      if (status) params.set('status', status)
      if (repository) params.set('repository', repository)
      if (branch.trim()) params.set('branch', branch.trim())
      return api<Page<Run>>(`/api/runs?${params.toString()}`)
    },
    [status, repository, branch, page],
    20000,
  )

  function reset<T>(setter: (v: T) => void, value: T) {
    setter(value)
    setPage(0)
  }

  const data = runs.data

  return (
    <>
      <PageHeader title="Pipelines" subtitle="Every CI/CD run recorded from GitHub Actions and your own pipelines." />

      <div className="card mb-5 grid gap-3 p-4 sm:grid-cols-3">
        <select className="input" value={status} onChange={(e) => reset(setStatus, e.target.value)} aria-label="Filter by status">
          <option value="">All statuses</option>
          {STATUSES.map((s) => (
            <option key={s} value={s}>
              {s.charAt(0) + s.slice(1).toLowerCase()}
            </option>
          ))}
        </select>
        <select className="input" value={repository} onChange={(e) => reset(setRepository, e.target.value)} aria-label="Filter by repository">
          <option value="">All repositories</option>
          {(repos.data ?? []).map((r) => (
            <option key={r.id} value={r.fullName}>
              {r.fullName}
            </option>
          ))}
        </select>
        <input
          className="input"
          placeholder="Filter by branch, e.g. main"
          value={branch}
          onChange={(e) => reset(setBranch, e.target.value)}
          aria-label="Filter by branch"
        />
      </div>

      {runs.error && <ErrorBox message={runs.error} />}
      {runs.loading && !data ? (
        <Loading />
      ) : (
        <Panel title={`${data?.totalElements ?? 0} runs`}>
          {data && data.content.length > 0 ? (
            <>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[820px]">
                  <thead>
                    <tr>
                      <th className="th">Repository</th>
                      <th className="th">Workflow</th>
                      <th className="th">Branch</th>
                      <th className="th">Commit</th>
                      <th className="th">Actor</th>
                      <th className="th">Status</th>
                      <th className="th">Duration</th>
                      <th className="th">Started</th>
                      <th className="th" />
                    </tr>
                  </thead>
                  <tbody>
                    {data.content.map((r) => (
                      <tr key={r.id} className="hover:bg-slate-50/70">
                        <td className="td font-medium text-slate-900">{r.repository}</td>
                        <td className="td">{r.name}</td>
                        <td className="td">{r.branch ?? '-'}</td>
                        <td className="td font-mono text-xs">{shortSha(r.commitSha)}</td>
                        <td className="td">{r.actor ?? '-'}</td>
                        <td className="td">
                          <StatusBadge status={r.status} />
                        </td>
                        <td className="td">{formatDuration(r.durationSeconds)}</td>
                        <td className="td whitespace-nowrap">{timeAgo(r.startedAt)}</td>
                        <td className="td">
                          {r.url && /^https:\/\//.test(r.url) && (
                            <a href={r.url} target="_blank" rel="noopener noreferrer" className="text-slate-400 hover:text-indigo-600" aria-label="Open run on GitHub">
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
                  Page {data.number + 1} of {Math.max(1, data.totalPages)}
                </span>
                <div className="flex gap-2">
                  <button className="btn btn-ghost" disabled={page === 0} onClick={() => setPage(page - 1)}>
                    <ChevronLeft size={15} /> Prev
                  </button>
                  <button className="btn btn-ghost" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>
                    Next <ChevronRight size={15} />
                  </button>
                </div>
              </div>
            </>
          ) : (
            <Empty title="No runs match" text="Try clearing the filters, or wait for the next pipeline run." />
          )}
        </Panel>
      )}
    </>
  )
}
