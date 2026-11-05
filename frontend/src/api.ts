const TOKEN_KEY = 'devflow.session'

export interface Session {
  token: string
  username: string
  role: 'ADMIN' | 'VIEWER'
  expiresAt: string
}

export function loadSession(): Session | null {
  try {
    const raw = localStorage.getItem(TOKEN_KEY)
    if (!raw) return null
    const session = JSON.parse(raw) as Session
    if (new Date(session.expiresAt).getTime() <= Date.now()) {
      localStorage.removeItem(TOKEN_KEY)
      return null
    }
    return session
  } catch {
    return null
  }
}

export function saveSession(session: Session | null) {
  try {
    if (session) localStorage.setItem(TOKEN_KEY, JSON.stringify(session))
    else localStorage.removeItem(TOKEN_KEY)
  } catch {
    return
  }
}

export class ApiError extends Error {
  status: number
  details?: Record<string, string>

  constructor(status: number, message: string, details?: Record<string, string>) {
    super(message)
    this.status = status
    this.details = details
  }
}

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const session = loadSession()
  const headers = new Headers(init.headers)
  if (init.body) headers.set('Content-Type', 'application/json')
  if (session) headers.set('Authorization', `Bearer ${session.token}`)

  const response = await fetch(path, { ...init, headers })

  if (response.status === 401 && !path.startsWith('/api/auth/login')) {
    saveSession(null)
    window.location.assign('/login')
    throw new ApiError(401, 'Session expired')
  }
  if (!response.ok) {
    let message = `Request failed (${response.status})`
    let details: Record<string, string> | undefined
    try {
      const body = await response.json()
      if (body.message) message = body.message
      details = body.details
    } catch {
      message = `Request failed (${response.status})`
    }
    throw new ApiError(response.status, message, details)
  }
  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}
