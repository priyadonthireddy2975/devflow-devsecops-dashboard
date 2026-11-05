import { useState, type FormEvent } from 'react'
import { Check, CheckCircle2, Copy, Loader2, MinusCircle, UserPlus } from 'lucide-react'
import { api } from '../api'
import { timeAgo } from '../format'
import { useAsync } from '../hooks'
import type { Role, SettingsInfo, UserInfo } from '../types'
import { ErrorBox, Loading, PageHeader, Panel } from '../ui'

function Flag({ ok, label, hint }: { ok: boolean; label: string; hint: string }) {
  return (
    <li className="flex items-start gap-3 px-5 py-3.5">
      {ok ? <CheckCircle2 size={18} className="mt-0.5 text-emerald-600" /> : <MinusCircle size={18} className="mt-0.5 text-slate-400" />}
      <div>
        <p className="text-sm font-semibold text-slate-800">
          {label} <span className={ok ? 'text-emerald-600' : 'text-slate-400'}>{ok ? 'configured' : 'not configured'}</span>
        </p>
        <p className="text-xs text-slate-500">{hint}</p>
      </div>
    </li>
  )
}

function CopyField({ value }: { value: string }) {
  const [copied, setCopied] = useState(false)
  async function copy() {
    try {
      await navigator.clipboard.writeText(value)
      setCopied(true)
      window.setTimeout(() => setCopied(false), 1500)
    } catch {
      setCopied(false)
    }
  }
  return (
    <div className="flex items-center gap-2">
      <code className="min-w-0 flex-1 overflow-x-auto rounded-lg bg-slate-100 px-3 py-2 text-xs text-slate-800">{value}</code>
      <button className="btn btn-ghost shrink-0" onClick={() => void copy()}>
        {copied ? <Check size={14} /> : <Copy size={14} />} {copied ? 'Copied' : 'Copy'}
      </button>
    </div>
  )
}

export default function SettingsPage() {
  const info = useAsync(() => api<SettingsInfo>('/api/settings/info'), [])
  const users = useAsync(() => api<UserInfo[]>('/api/users'), [])
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState<Role>('VIEWER')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function createUser(e: FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      await api<UserInfo>('/api/users', { method: 'POST', body: JSON.stringify({ username: username.trim(), password, role }) })
      setUsername('')
      setPassword('')
      await users.reload()
    } catch (err) {
      const details = err instanceof Error && 'details' in err ? (err as { details?: Record<string, string> }).details : undefined
      const detail = details ? Object.values(details)[0] : null
      setError(detail ?? (err instanceof Error ? err.message : 'Could not create user'))
    } finally {
      setBusy(false)
    }
  }

  if (info.loading && !info.data) return <Loading />
  if (info.error && !info.data) return <ErrorBox message={info.error} />
  const i = info.data
  if (!i) return null

  const origin = window.location.origin
  const snippet = `curl -X POST ${origin}/api/scans \\
  -H "X-API-Key: $DEVFLOW_API_KEY" \\
  -H "Content-Type: application/json" \\
  -d '{"repository":"owner/repo","tool":"Trivy","critical":0,"high":2,"medium":5,"low":9}'`

  return (
    <>
      <PageHeader title="Settings" subtitle="Connect GitHub, configure your CI pipeline and manage who can sign in." />

      <div className="grid gap-6 lg:grid-cols-2">
        <Panel title="Integrations" subtitle="Configured through environment variables on the server">
          <ul className="divide-y divide-slate-100">
            <Flag ok={i.webhookSecretConfigured} label="GitHub webhook secret" hint="GITHUB_WEBHOOK_SECRET. Required to accept webhook deliveries." />
            <Flag ok={i.githubTokenConfigured} label="GitHub API token" hint="GITHUB_TOKEN. Enables repository sync and scheduled polling." />
            <Flag ok={i.apiKeyConfigured} label="CI ingest API key" hint="DEVFLOW_API_KEY. Lets your pipeline report runs and scan results." />
            <Flag ok={i.dockerAvailable} label="Docker socket" hint="Mount /var/run/docker.sock to show running containers." />
          </ul>
          {i.demoData && <p className="border-t border-slate-100 px-5 py-3 text-xs text-amber-700">Demo data is enabled (DEMO_DATA=true). Sample repositories are labelled as such.</p>}
        </Panel>

        <Panel title="Connect GitHub webhook" subtitle="Repository Settings, Webhooks, Add webhook">
          <div className="space-y-4 px-5 py-5 text-sm text-slate-600">
            <div>
              <p className="mb-1.5 font-medium text-slate-800">Payload URL</p>
              <CopyField value={`${origin}${i.webhookPath}`} />
            </div>
            <ul className="list-disc space-y-1 pl-5 text-xs text-slate-500">
              <li>Content type: application/json</li>
              <li>Secret: the value of GITHUB_WEBHOOK_SECRET</li>
              <li>Events: select &quot;Workflow runs&quot;</li>
            </ul>
          </div>
        </Panel>

        <Panel title="Report scan results from CI" subtitle="Use the same call for /api/runs">
          <div className="space-y-3 px-5 py-5">
            <pre className="overflow-x-auto rounded-lg bg-slate-900 p-4 text-xs leading-relaxed text-slate-100">{snippet}</pre>
            <CopyField value={snippet.replace(/\\\n\s*/g, '')} />
          </div>
        </Panel>

        <Panel title="Users" subtitle="Admins can change data. Viewers are read-only.">
          <ul className="divide-y divide-slate-100">
            {(users.data ?? []).map((u) => (
              <li key={u.id} className="flex items-center justify-between px-5 py-3 text-sm">
                <span className="font-medium text-slate-800">{u.username}</span>
                <span className="text-xs text-slate-500">
                  {u.role === 'ADMIN' ? 'Admin' : 'Viewer'} · added {timeAgo(u.createdAt)}
                </span>
              </li>
            ))}
          </ul>
          <form onSubmit={createUser} className="space-y-3 border-t border-slate-100 px-5 py-5">
            <div className="grid gap-3 sm:grid-cols-2">
              <input className="input" placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} aria-label="New username" required />
              <input
                className="input"
                type="password"
                placeholder="Password (min 8 characters)"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                aria-label="New password"
                minLength={8}
                required
              />
            </div>
            <div className="flex items-center gap-3">
              <select className="input !w-auto" value={role} onChange={(e) => setRole(e.target.value as Role)} aria-label="Role">
                <option value="VIEWER">Viewer</option>
                <option value="ADMIN">Admin</option>
              </select>
              <button className="btn btn-primary" disabled={busy}>
                {busy ? <Loader2 size={15} className="animate-spin" /> : <UserPlus size={15} />} Add user
              </button>
            </div>
            {error && <p className="text-sm text-rose-600">{error}</p>}
          </form>
        </Panel>
      </div>
    </>
  )
}
