import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'
import { api, loadSession, saveSession, type Session } from './api'
import type { LoginResponse } from './types'

interface AuthState {
  session: Session | null
  isAdmin: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(() => loadSession())

  const login = useCallback(async (username: string, password: string) => {
    const result = await api<LoginResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    })
    const next: Session = {
      token: result.token,
      username: result.username,
      role: result.role,
      expiresAt: result.expiresAt,
    }
    saveSession(next)
    setSession(next)
  }, [])

  const logout = useCallback(() => {
    saveSession(null)
    setSession(null)
  }, [])

  const value = useMemo(
    () => ({ session, isAdmin: session?.role === 'ADMIN', login, logout }),
    [session, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
