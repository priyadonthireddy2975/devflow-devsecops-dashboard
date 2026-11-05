import { useState, type FormEvent } from 'react'
import { ExternalLink, Loader2, Plus, RefreshCw, Trash2 } from 'lucide-react'
import { api } from '../api'
import { useAuth } from '../auth'
import { timeAgo } from '../format'
import { useAsync } from '../hooks'
import type { Repo } from '../types'
import { Empty, ErrorBox, Loading, PageHeader, StatusBadge } from '../ui'

export default function Repositories() {
  const { isAdmin } = useAuth()
  const repos = useAsync(() => api<Repo[]>('/api/repositories'), [], 30000)
  const [name, setName] = useState('')
  const [busy, setBusy] = useState<number | 'add' | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function add(e: FormEvent) {
    e.preventDefault()
    setBusy('add')
    setError(null)
    setMessage(null)
    try {
      await api<Repo>('/api/repositories', { method: 'POST', body: JSON.stringify({ fullName: name.trim() }) })
      setName('')
      await repos.reload()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not add repository')
    } finally {
      setBusy(null)
    }
  }

  async function sync(id: number) {
    setBusy(id)
    setError(null)
    setMessage(null)
    try {
      const result = await api<{ synced: number }>(`/api/repositories/${id}/sync`, { method: 'POST' })
      setMessage(
        result.synced > 0
          ? `Synced ${result.synced} runs from GitHub.`
          : 'Nothing synced. Set GITHUB_TOKEN on the server to pull runs from GitHub.',
      )
      await repos.reload()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Sync failed')
    } finally {
      setBusy(null)
    }
  }

  async function remove(id: number, fullName: string) {
    if (!window.confirm(`Stop tracking ${fullName} and delete its runs and scans?`)) return
    setBusy(id)
    setError(null)
    try {
      await api<void>(`/api/repositories/${id}`, { method: 'DELETE' })
      await repos.reload()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed')
    } finally {
      setBusy(null)
    }
  }

  return (
    <>
      <PageHeader title="Repositories" subtitle="GitHub repositories whose pipelines are tracked by DevFlow." />

      {isAdmin && (
        <form onSubmit={add} className="card mb-5 flex flex-col gap-3 p-4 sm:flex-row">
          <input
            className="input"
            placeholder="owner/repository, e.g. priyadonthireddy2975/devflow-devsecops-dashboard"
            value={name}
            onChange={(e) => setName(e.target.value)}
            aria-label="Repository full name"
            required
          />
          <button className="btn btn-primary shrink-0 justify-center" disabled={busy === 'add'}>
            {busy === 'add' ? <Loader2 size={15} className="animate-spin" /> : <Plus size={15} />} Track repository
          </button>
        </form>
      )}

      {error && (
        <div className="mb-4">
          <ErrorBox message={error} />
        </div>
      )}
      {message && <p className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{message}</p>}

      {repos.loading && !repos.data ? (
        <Loading />
      ) : repos.error && !repos.data ? (
        <ErrorBox message={repos.error} />
      ) : repos.data && repos.data.length > 0 ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {repos.data.map((r) => (
            <article key={r.id} className="card flex flex-col p-5">
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <h3 className="truncate text-base font-semibold text-slate-900">{r.fullName}</h3>
                  <p className="mt-0.5 text-xs text-slate-500">
                    {[r.language, r.defaultBranch].filter(Boolean).join(' · ') || 'No metadata yet'}
                  </p>
                </div>
                {/^https:\/\//.test(r.htmlUrl) && (
                  <a href={r.htmlUrl} target="_blank" rel="noopener noreferrer" className="text-slate-400 hover:text-indigo-600" aria-label="Open on GitHub">
                    <ExternalLink size={16} />
                  </a>
                )}
              </div>
              {r.description && <p className="mt-3 line-clamp-2 text-sm text-slate-600">{r.description}</p>}

              <div className="mt-4">
                <div className="mb-1.5 flex justify-between text-xs text-slate-500">
                  <span>Success rate</span>
                  <span className="font-semibold text-slate-800">{r.totalRuns > 0 ? `${r.successRate}%` : 'No runs'}</span>
                </div>
                <div className="h-2 overflow-hidden rounded-full bg-slate-100">
                  <div className="h-full rounded-full bg-emerald-500" style={{ width: `${r.successRate}%` }} />
                </div>
              </div>

              <div className="mt-4 flex items-center justify-between text-sm">
                <div className="text-slate-500">
                  <span className="font-semibold text-slate-800">{r.totalRuns}</span> runs
                  {r.lastRunAt && <span> · {timeAgo(r.lastRunAt)}</span>}
                </div>
                {r.lastStatus && <StatusBadge status={r.lastStatus} />}
              </div>

              {isAdmin && (
                <div className="mt-5 flex gap-2 border-t border-slate-100 pt-4">
                  <button className="btn btn-ghost" disabled={busy === r.id} onClick={() => void sync(r.id)}>
                    {busy === r.id ? <Loader2 size={14} className="animate-spin" /> : <RefreshCw size={14} />} Sync
                  </button>
                  <button className="btn btn-ghost text-rose-600" disabled={busy === r.id} onClick={() => void remove(r.id, r.fullName)}>
                    <Trash2 size={14} /> Remove
                  </button>
                </div>
              )}
            </article>
          ))}
        </div>
      ) : (
        <div className="card">
          <Empty
            title="No repositories tracked yet"
            text={isAdmin ? 'Add a repository above, or point a GitHub webhook at DevFlow and repositories appear automatically.' : 'Ask an administrator to add a repository.'}
          />
        </div>
      )}
    </>
  )
}
