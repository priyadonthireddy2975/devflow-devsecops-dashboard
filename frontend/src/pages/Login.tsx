import { useState, type FormEvent } from 'react'
import { Loader2, Lock, ShieldCheck, User } from 'lucide-react'
import { useAuth } from '../auth'
import { Logo } from '../ui'

export default function Login() {
  const { login } = useAuth()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function submit(e: FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      await login(username.trim(), password)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Sign in failed')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="grid min-h-full lg:grid-cols-2">
      <div className="relative hidden flex-col justify-between overflow-hidden bg-slate-900 p-12 text-white lg:flex">
        <div className="absolute -right-24 -top-24 h-80 w-80 rounded-full bg-indigo-600/30 blur-3xl" />
        <div className="absolute -bottom-32 -left-16 h-96 w-96 rounded-full bg-emerald-500/20 blur-3xl" />
        <div className="relative">
          <Logo light />
        </div>
        <div className="relative max-w-md">
          <h2 className="text-4xl font-bold leading-tight tracking-tight">See every pipeline and every vulnerability in one place.</h2>
          <p className="mt-4 text-slate-300">
            Track CI/CD runs from GitHub, review security scan results, and catch failing builds before they reach production.
          </p>
          <ul className="mt-8 space-y-3 text-sm text-slate-200">
            {['Live pipeline status from GitHub Actions', 'CodeQL, Trivy, Gitleaks and dependency scan results', 'Role-based access for admins and viewers'].map(
              (t) => (
                <li key={t} className="flex items-center gap-2.5">
                  <ShieldCheck size={16} className="text-emerald-400" /> {t}
                </li>
              ),
            )}
          </ul>
        </div>
        <p className="relative text-xs text-slate-400">DevFlow DevSecOps Dashboard</p>
      </div>

      <div className="flex items-center justify-center bg-white px-6 py-12">
        <form onSubmit={submit} className="w-full max-w-sm">
          <div className="mb-8 lg:hidden">
            <Logo />
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Welcome back</h1>
          <p className="mt-1 text-sm text-slate-500">Sign in to your DevFlow workspace.</p>

          <label className="mt-8 block text-sm font-medium text-slate-700" htmlFor="username">
            Username
          </label>
          <div className="relative mt-1.5">
            <User size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              id="username"
              className="input pl-9"
              autoComplete="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>

          <label className="mt-4 block text-sm font-medium text-slate-700" htmlFor="password">
            Password
          </label>
          <div className="relative mt-1.5">
            <Lock size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              id="password"
              type="password"
              className="input pl-9"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          {error && (
            <p role="alert" className="mt-4 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
              {error}
            </p>
          )}

          <button type="submit" disabled={busy} className="btn btn-primary mt-6 w-full justify-center py-2.5">
            {busy && <Loader2 size={16} className="animate-spin" />} Sign in
          </button>
        </form>
      </div>
    </div>
  )
}
