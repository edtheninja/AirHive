import { apiRequest } from "./client";
import type { UserRole } from "@/lib/auth/types";

export type AppUser = {
  id: number;
  username: string;
  role: UserRole;
  enabled: boolean;
};

export async function getUsers(): Promise<AppUser[]> {
  return apiRequest<AppUser[]>("/users");
}

export async function updateUserRole(id: number, role: UserRole): Promise<void> {
  await apiRequest<unknown>(`/users/${id}/role`, {
    method: "PATCH",
    body: JSON.stringify({ role }),
  });
}
