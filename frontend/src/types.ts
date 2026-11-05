export type Role = 'ADMIN' | 'VIEWER'

export type RunStatus = 'QUEUED' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'CANCELLED'

export interface LoginResponse {
  token: string
  username: string
  role: Role
  expiresAt: string
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface ToolSummary {
  tool: string
  critical: number
  high: number
  medium: number
  low: number
  repositories: number
}

export interface SecuritySummary {
  critical: number
  high: number
  medium: number
  low: number
  tools: ToolSummary[]
}

export interface DashboardSummary {
  totalRuns: number
  successfulRuns: number
  failedRuns: number
  activeRuns: number
  successRate: number
  averageDurationSeconds: number
  repositories: number
  containersRunning: number
  dockerAvailable: boolean
  security: SecuritySummary
}

export interface TrendPoint {
  date: string
  success: number
  failed: number
}

export interface Run {
  id: number
  repository: string
  name: string
  branch?: string
  commitSha?: string
  actor?: string
  trigger?: string
  status: RunStatus
  url?: string
  startedAt: string
  finishedAt?: string
  durationSeconds?: number
}

export interface Scan {
  id: number
  repository: string
  tool: string
  commitSha?: string
  critical: number
  high: number
  medium: number
  low: number
  reportUrl?: string
  scannedAt: string
}

export interface Repo {
  id: number
  fullName: string
  htmlUrl: string
  defaultBranch?: string
  language?: string
  description?: string
  lastSyncedAt?: string
  totalRuns: number
  successRate: number
  lastStatus?: RunStatus
  lastRunAt?: string
}

export interface UserInfo {
  id: number
  username: string
  role: Role
  createdAt: string
}

export interface SettingsInfo {
  webhookPath: string
  webhookSecretConfigured: boolean
  githubTokenConfigured: boolean
  apiKeyConfigured: boolean
  dockerAvailable: boolean
  demoData: boolean
}
