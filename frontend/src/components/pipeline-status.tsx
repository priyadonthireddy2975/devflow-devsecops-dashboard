"use client";

import { Card, CardHeader } from "./ui/card";
import { StatusBadge } from "./ui/status-badge";
import { GitBranch, Clock, ExternalLink } from "lucide-react";

export interface Pipeline {
  id: string;
  name: string;
  branch: string;
  status: "success" | "failed" | "running" | "pending";
  lastRun: string;
  duration: string;
}

interface PipelineStatusProps {
  pipelines: Pipeline[];
}

export function PipelineStatus({ pipelines }: PipelineStatusProps) {
  return (
    <Card>
      <CardHeader
        title="Pipeline Status"
        description="Active CI/CD pipelines"
        action={
          <button className="text-sm font-medium text-accent transition-colors hover:text-accent/80">
            View all
          </button>
        }
      />
      <div className="space-y-3">
        {pipelines.map((pipeline) => (
          <div
            key={pipeline.id}
            className="group flex items-center justify-between rounded-lg border bg-background p-4 transition-colors hover:bg-muted/50"
          >
            <div className="flex items-center gap-4">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-muted">
                <GitBranch className="h-5 w-5 text-muted-foreground" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <p className="font-medium">{pipeline.name}</p>
                  <StatusBadge status={pipeline.status} />
                </div>
                <p className="mt-0.5 text-sm text-muted-foreground">
                  {pipeline.branch}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-4">
              <div className="text-right text-sm">
                <p className="text-muted-foreground">{pipeline.lastRun}</p>
                <div className="mt-0.5 flex items-center justify-end gap-1 text-muted-foreground">
                  <Clock className="h-3 w-3" />
                  <span>{pipeline.duration}</span>
                </div>
              </div>
              <button
                className="rounded-lg p-2 text-muted-foreground opacity-0 transition-all hover:bg-muted hover:text-foreground group-hover:opacity-100"
                aria-label="View pipeline details"
              >
                <ExternalLink className="h-4 w-4" />
              </button>
            </div>
          </div>
        ))}
      </div>
    </Card>
  );
}
