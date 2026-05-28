import { LucideIcon } from "lucide-react";
import { Card } from "./ui/card";

interface MetricCardProps {
  title: string;
  value: string | number;
  change?: {
    value: number;
    trend: "up" | "down" | "neutral";
  };
  icon: LucideIcon;
  iconColor?: string;
}

export function MetricCard({
  title,
  value,
  change,
  icon: Icon,
  iconColor = "text-accent",
}: MetricCardProps) {
  return (
    <Card>
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-muted-foreground">{title}</p>
          <p className="mt-2 text-3xl font-semibold tracking-tight">{value}</p>
          {change && (
            <p
              className={`mt-1 text-sm ${
                change.trend === "up"
                  ? "text-success"
                  : change.trend === "down"
                  ? "text-destructive"
                  : "text-muted-foreground"
              }`}
            >
              {change.trend === "up" ? "+" : change.trend === "down" ? "-" : ""}
              {Math.abs(change.value)}% from last week
            </p>
          )}
        </div>
        <div className={`rounded-lg bg-muted p-3 ${iconColor}`}>
          <Icon className="h-5 w-5" />
        </div>
      </div>
    </Card>
  );
}
