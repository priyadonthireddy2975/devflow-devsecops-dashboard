import { Pipeline } from "@/components/pipeline-status";
import { Build } from "@/components/build-history";
import { SecurityMetrics } from "@/components/security-overview";
import { Notification } from "@/components/notifications-panel";

// Configure your Spring Boot backend URL
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

// Sample data for when backend is not available
export const sampleData = {
  pipelines: [
    {
      id: "1",
      name: "frontend-deploy",
      branch: "main",
      status: "success" as const,
      lastRun: "2 minutes ago",
      duration: "3m 42s",
    },
    {
      id: "2",
      name: "backend-build",
      branch: "feature/auth",
      status: "running" as const,
      lastRun: "Just now",
      duration: "1m 23s",
    },
    {
      id: "3",
      name: "security-scan",
      branch: "main",
      status: "success" as const,
      lastRun: "15 minutes ago",
      duration: "5m 10s",
    },
    {
      id: "4",
      name: "integration-tests",
      branch: "develop",
      status: "failed" as const,
      lastRun: "1 hour ago",
      duration: "8m 55s",
    },
  ] as Pipeline[],

  builds: [
    {
      id: "1",
      commit: "a1b2c3d",
      branch: "main",
      status: "success" as const,
      author: "Alice Chen",
      timestamp: "2 min ago",
      duration: "3m 42s",
    },
    {
      id: "2",
      commit: "e4f5g6h",
      branch: "feature/auth",
      status: "running" as const,
      author: "Bob Smith",
      timestamp: "5 min ago",
      duration: "1m 23s",
    },
    {
      id: "3",
      commit: "i7j8k9l",
      branch: "develop",
      status: "failed" as const,
      author: "Carol White",
      timestamp: "1 hour ago",
      duration: "8m 55s",
    },
    {
      id: "4",
      commit: "m0n1o2p",
      branch: "main",
      status: "success" as const,
      author: "David Lee",
      timestamp: "2 hours ago",
      duration: "4m 12s",
    },
    {
      id: "5",
      commit: "q3r4s5t",
      branch: "hotfix/login",
      status: "success" as const,
      author: "Eve Brown",
      timestamp: "3 hours ago",
      duration: "2m 58s",
    },
  ] as Build[],

  security: {
    score: 78,
    vulnerabilities: {
      critical: 2,
      high: 5,
      medium: 12,
      low: 23,
    },
    dependencyAlerts: 8,
    lastScan: "10 minutes ago",
  } as SecurityMetrics,

  notifications: [
    {
      id: "1",
      type: "error" as const,
      title: "Build Failed",
      message: "integration-tests pipeline failed on develop branch",
      timestamp: "1 hour ago",
      read: false,
    },
    {
      id: "2",
      type: "warning" as const,
      title: "Security Alert",
      message: "2 critical vulnerabilities found in dependencies",
      timestamp: "2 hours ago",
      read: false,
    },
    {
      id: "3",
      type: "success" as const,
      title: "Deployment Complete",
      message: "frontend-deploy successfully deployed to production",
      timestamp: "2 hours ago",
      read: true,
    },
    {
      id: "4",
      type: "info" as const,
      title: "New Team Member",
      message: "Bob Smith has joined the DevOps team",
      timestamp: "1 day ago",
      read: true,
    },
  ] as Notification[],

  metrics: {
    totalBuilds: 156,
    buildChange: { value: 12, trend: "up" as const },
    successRate: 94.2,
    successChange: { value: 3, trend: "up" as const },
    deployments: 28,
    deploymentChange: { value: 5, trend: "down" as const },
    avgBuildTime: "4m 32s",
    buildTimeChange: { value: 8, trend: "up" as const },
  },
};

// API fetcher functions
export async function fetchDashboardData() {
  try {
    const response = await fetch(`${API_BASE_URL}/api/dashboard`, {
      next: { revalidate: 30 },
    });
    if (!response.ok) throw new Error("Failed to fetch dashboard data");
    return await response.json();
  } catch {
    // Return sample data if API is unavailable
    return sampleData;
  }
}

export async function fetchBuilds(): Promise<Build[]> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/builds`, {
      next: { revalidate: 30 },
    });
    if (!response.ok) throw new Error("Failed to fetch builds");
    return await response.json();
  } catch {
    return sampleData.builds;
  }
}

export async function fetchPipelines(): Promise<Pipeline[]> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/pipelines`, {
      next: { revalidate: 30 },
    });
    if (!response.ok) throw new Error("Failed to fetch pipelines");
    return await response.json();
  } catch {
    return sampleData.pipelines;
  }
}

export async function fetchSecurityMetrics(): Promise<SecurityMetrics> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/security`, {
      next: { revalidate: 60 },
    });
    if (!response.ok) throw new Error("Failed to fetch security metrics");
    return await response.json();
  } catch {
    return sampleData.security;
  }
}

export async function fetchNotifications(): Promise<Notification[]> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/notifications`, {
      next: { revalidate: 10 },
    });
    if (!response.ok) throw new Error("Failed to fetch notifications");
    return await response.json();
  } catch {
    return sampleData.notifications;
  }
}
