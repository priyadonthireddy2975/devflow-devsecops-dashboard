import { NavLink, Outlet } from 'react-router-dom'
import { GitBranch, LayoutDashboard, LogOut, PlayCircle, Settings, ShieldCheck } from 'lucide-react'
import { useAuth } from './auth'
import { Logo } from './ui'

const NAV = [
  { to: '/', label: 'Dashboard', Icon: LayoutDashboard, end: true },
  { to: '/pipelines', label: 'Pipelines', Icon: PlayCircle, end: false },
  { to: '/security', label: 'Security', Icon: ShieldCheck, end: false },
  { to: '/repositories', label: 'Repositories', Icon: GitBranch, end: false },
]

function linkClass(active: boolean) {
  return `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${
    active ? 'bg-white/10 text-white' : 'text-slate-400 hover:bg-white/5 hover:text-white'
  }`
}

export default function Layout() {
  const { session, isAdmin, logout } = useAuth()
  const items = isAdmin ? [...NAV, { to: '/settings', label: 'Settings', Icon: Settings, end: false }] : NAV

  return (
    <div className="min-h-full lg:flex">
      <aside className="hidden w-64 shrink-0 flex-col bg-slate-900 px-4 py-6 lg:flex">
        <div className="px-2 pb-8">
          <Logo light />
        </div>
        <nav className="flex flex-1 flex-col gap-1">
          {items.map(({ to, label, Icon, end }) => (
            <NavLink key={to} to={to} end={end} className={({ isActive }) => linkClass(isActive)}>
              <Icon size={18} /> {label}
            </NavLink>
          ))}
        </nav>
        <div className="rounded-xl bg-white/5 p-3">
          <p className="truncate text-sm font-semibold text-white">{session?.username}</p>
          <p className="text-xs text-slate-400">{session?.role === 'ADMIN' ? 'Administrator' : 'Viewer'}</p>
          <button
            onClick={logout}
            className="mt-3 flex w-full cursor-pointer items-center justify-center gap-2 rounded-lg bg-white/10 px-3 py-2 text-xs font-semibold text-slate-200 hover:bg-white/20"
          >
            <LogOut size={14} /> Sign out
          </button>
        </div>
      </aside>

      <div className="min-w-0 flex-1">
        <header className="flex items-center justify-between gap-3 bg-slate-900 px-4 py-3 lg:hidden">
          <Logo light />
          <button onClick={logout} className="cursor-pointer text-xs font-semibold text-slate-300">
            Sign out
          </button>
        </header>
        <nav className="flex gap-1 overflow-x-auto bg-slate-900 px-3 pb-3 lg:hidden">
          {items.map(({ to, label, Icon, end }) => (
            <NavLink key={to} to={to} end={end} className={({ isActive }) => linkClass(isActive) + ' whitespace-nowrap'}>
              <Icon size={16} /> {label}
            </NavLink>
          ))}
        </nav>
        <main className="mx-auto w-full max-w-[1280px] px-4 py-6 sm:px-8 sm:py-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
