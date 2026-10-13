import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { pageVariants } from "@/lib/ams/motion";
import { GlassCard, PageHeader, SectionCard } from "@/components/ams/primitives";
import { useAuth } from "@/lib/auth/auth-context";

export const Route = createFileRoute("/profile")({
  head: () => ({
    meta: [
      { title: "User Profile — AirHive AMS" },
      {
        name: "description",
        content: "Operations account details, roles, permissions and recent activity.",
      },
      { property: "og:title", content: "User Profile — AirHive AMS" },
      {
        property: "og:description",
        content: "Operations account details, roles and recent activity.",
      },
    ],
  }),
  component: ProfilePage,
});

function formatRole(role: string) {
  return role.charAt(0) + role.slice(1).toLowerCase();
}

function getInitials(username: string) {
  const parts = username
    .trim()
    .split(/[\s._-]+/)
    .filter(Boolean);

  if (parts.length === 0) {
    return "U";
  }

  return parts
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");
}

function ProfilePage() {
  const { session } = useAuth();

  const username = session?.username ?? "Unknown user";
  const role = session?.role;
  const initials = getInitials(username);

  const permissions = [
    {
      name: "View flights, routes and airports",
      allowed: Boolean(role),
    },
    {
      name: "Manage flight operations",
      allowed: role === "ADMIN" || role === "OPERATOR",
    },
    {
      name: "Manage aircraft and airport records",
      allowed: role === "ADMIN" || role === "OPERATOR",
    },
    {
      name: "Manage user accounts",
      allowed: role === "ADMIN",
    },
  ];

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1000px] space-y-6 py-6"
    >
      <PageHeader title="User Profile" description="Operations profile and access overview." />

      <GlassCard hover={false} className="flex flex-wrap items-center gap-5 p-6">
        <span className="num flex h-16 w-16 items-center justify-center rounded-3xl bg-primary text-lg font-semibold text-primary-foreground">
          {initials}
        </span>

        <div>
          <h2 className="text-lg font-semibold">{username}</h2>

          <p className="text-sm text-muted-foreground">
            {role ? `${formatRole(role)} Account` : "Account details unavailable"}
          </p>

          <p className="num mt-1 text-xs text-muted-foreground">
            User ID: {session?.userId ?? "Unavailable"}
          </p>
        </div>
      </GlassCard>

      <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
        <SectionCard title="Permissions" subtitle="Access associated with your account role">
          <ul className="space-y-3 text-sm">
            {permissions.map((permission) => (
              <li key={permission.name} className="flex items-center justify-between gap-3">
                <span className="text-muted-foreground">{permission.name}</span>

                <span
                  className={
                    permission.allowed
                      ? "shrink-0 rounded-full bg-success/14 px-2.5 py-1 text-xs text-success"
                      : "shrink-0 rounded-full bg-muted px-2.5 py-1 text-xs text-muted-foreground"
                  }
                >
                  {permission.allowed ? "Available" : "Restricted"}
                </span>
              </li>
            ))}
          </ul>
        </SectionCard>

        <SectionCard title="Recent activity" subtitle="Recorded account activity">
          <div className="py-4 text-sm text-muted-foreground">
            No activity history is available yet.
          </div>
        </SectionCard>
      </div>
    </motion.div>
  );
}
