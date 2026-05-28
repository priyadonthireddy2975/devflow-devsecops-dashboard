"use client";

import { Card, CardHeader } from "./ui/card";
import {
  AlertCircle,
  CheckCircle,
  Info,
  AlertTriangle,
  X,
} from "lucide-react";

export interface Notification {
  id: string;
  type: "success" | "error" | "warning" | "info";
  title: string;
  message: string;
  timestamp: string;
  read: boolean;
}

interface NotificationsPanelProps {
  notifications: Notification[];
  onDismiss?: (id: string) => void;
}

const notificationStyles = {
  success: {
    bg: "bg-success/10",
    border: "border-success/20",
    icon: CheckCircle,
    iconColor: "text-success",
  },
  error: {
    bg: "bg-destructive/10",
    border: "border-destructive/20",
    icon: AlertCircle,
    iconColor: "text-destructive",
  },
  warning: {
    bg: "bg-warning/10",
    border: "border-warning/20",
    icon: AlertTriangle,
    iconColor: "text-warning",
  },
  info: {
    bg: "bg-accent/10",
    border: "border-accent/20",
    icon: Info,
    iconColor: "text-accent",
  },
};

export function NotificationsPanel({
  notifications,
  onDismiss,
}: NotificationsPanelProps) {
  return (
    <Card>
      <CardHeader
        title="Notifications"
        description={`${notifications.filter((n) => !n.read).length} unread alerts`}
        action={
          <button className="text-sm font-medium text-accent transition-colors hover:text-accent/80">
            Mark all read
          </button>
        }
      />

      <div className="space-y-3">
        {notifications.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-8 text-center">
            <CheckCircle className="h-12 w-12 text-muted-foreground/50" />
            <p className="mt-2 text-sm text-muted-foreground">
              No notifications
            </p>
          </div>
        ) : (
          notifications.map((notification) => {
            const style = notificationStyles[notification.type];
            const Icon = style.icon;

            return (
              <div
                key={notification.id}
                className={`group relative rounded-lg border p-4 transition-colors ${style.bg} ${style.border} ${
                  notification.read ? "opacity-60" : ""
                }`}
              >
                <div className="flex gap-3">
                  <Icon className={`h-5 w-5 shrink-0 ${style.iconColor}`} />
                  <div className="flex-1 space-y-1">
                    <p className="font-medium leading-none">
                      {notification.title}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {notification.message}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {notification.timestamp}
                    </p>
                  </div>
                  {onDismiss && (
                    <button
                      onClick={() => onDismiss(notification.id)}
                      className="absolute right-2 top-2 rounded p-1 text-muted-foreground opacity-0 transition-opacity hover:bg-background/50 hover:text-foreground group-hover:opacity-100"
                      aria-label="Dismiss notification"
                    >
                      <X className="h-4 w-4" />
                    </button>
                  )}
                </div>
              </div>
            );
          })
        )}
      </div>
    </Card>
  );
}
