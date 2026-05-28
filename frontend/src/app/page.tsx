import { Header } from "@/components/header";
import { MetricCard } from "@/components/metric-card";
import { PipelineStatus } from "@/components/pipeline-status";
import { SecurityOverview } from "@/components/security-overview";
import { BuildHistory } from "@/components/build-history";
import { NotificationsPanel } from "@/components/notifications-panel";
import { sampleData } from "@/lib/api";
import { Activity, CheckCircle, Rocket, Clock } from "lucide-react";

export default function DashboardPage() {
  const { pipelines, builds, security, notifications, metrics } = sampleData;

  return (
    <div className="min-h-screen">
      <Header />

      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
          <p className="mt-1 text-muted-foreground">
            Monitor your CI/CD pipelines, security metrics, and build activity.
          </p>
        </div>

        {/* Metrics Grid */}
        <div className="mb-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <MetricCard
            title="Total Builds"
            value={metrics.totalBuilds}
            change={metrics.buildChange}
            icon={Activity}
            iconColor="text-accent"
          />
          <MetricCard
            title="Success Rate"
            value={`${metrics.successRate}%`}
            change={metrics.successChange}
            icon={CheckCircle}
            iconColor="text-success"
          />
          <MetricCard
            title="Deployments"
            value={metrics.deployments}
            change={metrics.deploymentChange}
            icon={Rocket}
            iconColor="text-accent"
          />
          <MetricCard
            title="Avg Build Time"
            value={metrics.avgBuildTime}
            change={metrics.buildTimeChange}
            icon={Clock}
            iconColor="text-warning"
          />
        </div>

        {/* Main Content Grid */}
        <div className="grid gap-8 lg:grid-cols-3">
          {/* Left Column - Pipelines & Security */}
          <div className="space-y-8 lg:col-span-2">
            <PipelineStatus pipelines={pipelines} />
            <SecurityOverview metrics={security} />
            <BuildHistory builds={builds} />
          </div>

          {/* Right Column - Notifications */}
          <div>
            <NotificationsPanel notifications={notifications} />
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="mt-16 border-t bg-card">
        <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
          <p className="text-center text-sm text-muted-foreground">
            DevFlow Dashboard - Built with Next.js and Spring Boot
          </p>
        </div>
      </footer>
    </div>
  );
}
