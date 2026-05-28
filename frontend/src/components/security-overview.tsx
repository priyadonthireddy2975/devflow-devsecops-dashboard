"use client";

import { Card, CardHeader } from "./ui/card";
import { Shield, AlertTriangle, Bug, CheckCircle } from "lucide-react";

export interface SecurityMetrics {
  score: number;
  vulnerabilities: {
    critical: number;
    high: number;
    medium: number;
    low: number;
  };
  dependencyAlerts: number;
  lastScan: string;
}

interface SecurityOverviewProps {
  metrics: SecurityMetrics;
}

export function SecurityOverview({ metrics }: SecurityOverviewProps) {
  const getScoreColor = (score: number) => {
    if (score >= 80) return "text-success";
    if (score >= 60) return "text-warning";
    return "text-destructive";
  };

  const getScoreRingColor = (score: number) => {
    if (score >= 80) return "stroke-success";
    if (score >= 60) return "stroke-warning";
    return "stroke-destructive";
  };

  return (
    <Card>
      <CardHeader
        title="Security Overview"
        description={`Last scan: ${metrics.lastScan}`}
      />

      <div className="flex flex-col gap-6 sm:flex-row sm:items-center">
        {/* Security Score */}
        <div className="flex items-center justify-center">
          <div className="relative">
            <svg className="h-32 w-32 -rotate-90 transform">
              <circle
                cx="64"
                cy="64"
                r="56"
                fill="none"
                strokeWidth="8"
                className="stroke-muted"
              />
              <circle
                cx="64"
                cy="64"
                r="56"
                fill="none"
                strokeWidth="8"
                strokeLinecap="round"
                strokeDasharray={`${(metrics.score / 100) * 352} 352`}
                className={`${getScoreRingColor(metrics.score)} transition-all duration-500`}
              />
            </svg>
            <div className="absolute inset-0 flex flex-col items-center justify-center">
              <span className={`text-3xl font-bold ${getScoreColor(metrics.score)}`}>
                {metrics.score}
              </span>
              <span className="text-xs text-muted-foreground">Score</span>
            </div>
          </div>
        </div>

        {/* Vulnerability breakdown */}
        <div className="flex-1 space-y-3">
          <div className="flex items-center justify-between rounded-lg bg-destructive/10 px-4 py-3">
            <div className="flex items-center gap-3">
              <AlertTriangle className="h-5 w-5 text-destructive" />
              <span className="font-medium">Critical</span>
            </div>
            <span className="text-lg font-semibold text-destructive">
              {metrics.vulnerabilities.critical}
            </span>
          </div>

          <div className="flex items-center justify-between rounded-lg bg-warning/10 px-4 py-3">
            <div className="flex items-center gap-3">
              <Bug className="h-5 w-5 text-warning" />
              <span className="font-medium">High</span>
            </div>
            <span className="text-lg font-semibold text-warning">
              {metrics.vulnerabilities.high}
            </span>
          </div>

          <div className="flex items-center justify-between rounded-lg bg-accent/10 px-4 py-3">
            <div className="flex items-center gap-3">
              <Shield className="h-5 w-5 text-accent" />
              <span className="font-medium">Medium / Low</span>
            </div>
            <span className="text-lg font-semibold text-accent">
              {metrics.vulnerabilities.medium + metrics.vulnerabilities.low}
            </span>
          </div>

          <div className="flex items-center justify-between rounded-lg bg-muted px-4 py-3">
            <div className="flex items-center gap-3">
              <CheckCircle className="h-5 w-5 text-muted-foreground" />
              <span className="font-medium">Dependency Alerts</span>
            </div>
            <span className="text-lg font-semibold">{metrics.dependencyAlerts}</span>
          </div>
        </div>
      </div>
    </Card>
  );
}
