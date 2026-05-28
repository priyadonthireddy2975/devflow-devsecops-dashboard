interface StatusBadgeProps {
  status: "success" | "failed" | "running" | "pending" | "warning";
  label?: string;
}

const statusStyles = {
  success: "bg-success/10 text-success",
  failed: "bg-destructive/10 text-destructive",
  running: "bg-accent/10 text-accent",
  pending: "bg-muted text-muted-foreground",
  warning: "bg-warning/10 text-warning",
};

const statusDots = {
  success: "bg-success",
  failed: "bg-destructive",
  running: "bg-accent animate-pulse-dot",
  pending: "bg-muted-foreground",
  warning: "bg-warning",
};

export function StatusBadge({ status, label }: StatusBadgeProps) {
  const displayLabel = label || status.charAt(0).toUpperCase() + status.slice(1);

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium ${statusStyles[status]}`}
    >
      <span className={`h-1.5 w-1.5 rounded-full ${statusDots[status]}`} />
      {displayLabel}
    </span>
  );
}
