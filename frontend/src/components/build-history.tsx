"use client";

import { Card, CardHeader } from "./ui/card";
import { StatusBadge } from "./ui/status-badge";
import { User, Hash } from "lucide-react";

export interface Build {
  id: string;
  commit: string;
  branch: string;
  status: "success" | "failed" | "running" | "pending";
  author: string;
  timestamp: string;
  duration: string;
}

interface BuildHistoryProps {
  builds: Build[];
}

export function BuildHistory({ builds }: BuildHistoryProps) {
  return (
    <Card className="overflow-hidden">
      <CardHeader
        title="Build History"
        description="Recent build activity"
        action={
          <button className="text-sm font-medium text-accent transition-colors hover:text-accent/80">
            View all
          </button>
        }
      />

      <div className="-mx-6 -mb-6 overflow-x-auto">
        <table className="w-full min-w-[600px]">
          <thead>
            <tr className="border-t bg-muted/50">
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-muted-foreground">
                Build
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-muted-foreground">
                Status
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-muted-foreground">
                Branch
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-muted-foreground">
                Author
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-muted-foreground">
                Duration
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-muted-foreground">
                Time
              </th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {builds.map((build) => (
              <tr
                key={build.id}
                className="transition-colors hover:bg-muted/30"
              >
                <td className="whitespace-nowrap px-6 py-4">
                  <div className="flex items-center gap-2">
                    <Hash className="h-4 w-4 text-muted-foreground" />
                    <span className="font-mono text-sm">{build.commit}</span>
                  </div>
                </td>
                <td className="whitespace-nowrap px-6 py-4">
                  <StatusBadge status={build.status} />
                </td>
                <td className="whitespace-nowrap px-6 py-4">
                  <span className="rounded bg-muted px-2 py-1 font-mono text-xs">
                    {build.branch}
                  </span>
                </td>
                <td className="whitespace-nowrap px-6 py-4">
                  <div className="flex items-center gap-2">
                    <div className="flex h-6 w-6 items-center justify-center rounded-full bg-muted">
                      <User className="h-3 w-3 text-muted-foreground" />
                    </div>
                    <span className="text-sm">{build.author}</span>
                  </div>
                </td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-muted-foreground">
                  {build.duration}
                </td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-muted-foreground">
                  {build.timestamp}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  );
}
