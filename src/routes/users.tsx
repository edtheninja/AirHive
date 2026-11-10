import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { motion } from "motion/react";
import { PageHeader, SectionCard } from "@/components/ams/primitives";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { getUsers, updateUserRole, type AppUser } from "@/lib/api/users";
import type { UserRole } from "@/lib/auth/types";
import { pageVariants, listVariants } from "@/lib/ams/motion";

const roles: UserRole[] = ["PASSENGER", "VIEWER", "OPERATOR", "ADMIN"];

export const Route = createFileRoute("/users")({
  head: () => ({
    meta: [
      { title: "User Management — AirHive AMS" },
      {
        name: "description",
        content: "Manage AirHive accounts and user roles.",
      },
    ],
  }),
  component: UsersPage,
});

function getErrorMessage(error: unknown): string {
  return error instanceof Error ? error.message : "Something went wrong. Please try again.";
}

function UsersPage() {
  const [users, setUsers] = useState<AppUser[]>([]);
  const [draftRoles, setDraftRoles] = useState<Record<number, UserRole>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [savingId, setSavingId] = useState<number | null>(null);

  useEffect(() => {
    let active = true;

    async function loadUsers() {
      try {
        setLoading(true);
        setError("");

        const data = await getUsers();

        if (active) {
          setUsers(data);
          setDraftRoles(
            Object.fromEntries(data.map((user) => [user.id, user.role])) as Record<
              number,
              UserRole
            >,
          );
        }
      } catch (err) {
        if (active) {
          setError(getErrorMessage(err));
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    void loadUsers();

    return () => {
      active = false;
    };
  }, []);

  function changeRole(id: number, role: UserRole) {
    setDraftRoles((current) => ({
      ...current,
      [id]: role,
    }));
    setMessage("");
  }

  async function saveRole(user: AppUser) {
    const role = draftRoles[user.id];

    if (!role || role === user.role) {
      return;
    }

    try {
      setSavingId(user.id);
      setError("");
      setMessage("");

      await updateUserRole(user.id, role);

      setUsers((current) =>
        current.map((item) => (item.id === user.id ? { ...item, role } : item)),
      );

      setMessage(`Role updated successfully for ${user.username}.`);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSavingId(null);
    }
  }

  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      className="mx-auto max-w-[1600px] space-y-6 py-6"
    >
      <PageHeader
        title="User Management"
        description="View registered accounts and manage their access roles."
      />

      <motion.div variants={listVariants} initial="initial" animate="animate">
        <SectionCard
          title="Registered users"
          subtitle={`${users.length} registered account${users.length === 1 ? "" : "s"}`}
        >
          {loading && <p className="py-6 text-sm text-muted-foreground">Loading users...</p>}

          {!loading && error && (
            <p role="alert" className="py-4 text-sm text-destructive">
              {error}
            </p>
          )}

          {!loading && message && (
            <p role="status" className="mb-4 text-sm text-success">
              {message}
            </p>
          )}

          {!loading && !error && users.length === 0 && (
            <p className="py-6 text-sm text-muted-foreground">No registered users found.</p>
          )}

          {!loading && users.length > 0 && (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[680px] border-separate border-spacing-y-1 text-sm">
                <thead>
                  <tr className="text-left text-xs text-muted-foreground">
                    <th className="px-3 pb-2 font-medium">User</th>
                    <th className="px-3 pb-2 font-medium">User ID</th>
                    <th className="px-3 pb-2 font-medium">Role</th>
                    <th className="px-3 pb-2 font-medium">Status</th>
                    <th className="px-3 pb-2 font-medium">Action</th>
                  </tr>
                </thead>

                <tbody>
                  {users.map((user) => {
                    const selectedRole = draftRoles[user.id] ?? user.role;

                    const changed = selectedRole !== user.role;
                    const saving = savingId === user.id;

                    return (
                      <motion.tr
                        key={user.id}
                        initial={{ opacity: 0, y: 8 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="hover:bg-foreground/4"
                      >
                        <td className="rounded-l-2xl px-3 py-3 font-medium">{user.username}</td>

                        <td className="num px-3 py-3 text-muted-foreground">{user.id}</td>

                        <td className="px-3 py-3">
                          <Select
                            value={selectedRole}
                            onValueChange={(value) => changeRole(user.id, value as UserRole)}
                            disabled={saving}
                          >
                            <SelectTrigger
                              aria-label={`Role for ${user.username}`}
                              className="w-40"
                            >
                              <SelectValue />
                            </SelectTrigger>

                            <SelectContent>
                              {roles.map((role) => (
                                <SelectItem key={role} value={role}>
                                  {role}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </td>

                        <td className="px-3 py-3">
                          <span
                            className={`rounded-full px-2.5 py-1 text-xs ${
                              user.enabled
                                ? "bg-success/14 text-success"
                                : "bg-destructive/14 text-destructive"
                            }`}
                          >
                            {user.enabled ? "Enabled" : "Disabled"}
                          </span>
                        </td>

                        <td className="rounded-r-2xl px-3 py-3">
                          <button
                            type="button"
                            aria-label={`Save role for ${user.username}`}
                            disabled={!changed || saving}
                            onClick={() => void saveRole(user)}
                            className="rounded-xl bg-primary px-3 py-2 text-xs font-medium text-primary-foreground transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-40"
                          >
                            {saving ? "Saving..." : "Save role"}
                          </button>
                        </td>
                      </motion.tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </SectionCard>
      </motion.div>
    </motion.div>
  );
}
