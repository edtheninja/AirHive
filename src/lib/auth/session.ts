import type { LoginResponse, UserRole } from "./types";

const SESSION_KEY = "airhive_auth";

export type AuthSession = {
  userId: number;
  token: string;
  username: string;
  role: UserRole;
};

export function saveSession(response: LoginResponse) {
  if (typeof window === "undefined") {
    return;
  }

  const session: AuthSession = {
    userId: response.userId,
    token: response.token,
    username: response.username,
    role: response.role,
  };

  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function getSession(): AuthSession | null {
  if (typeof window === "undefined") {
    return null;
  }

  const stored = localStorage.getItem(SESSION_KEY);

  if (!stored) {
    return null;
  }

  try {
    return JSON.parse(stored) as AuthSession;
  } catch {
    localStorage.removeItem(SESSION_KEY);
    return null;
  }
}

export function getToken(): string | null {
  return getSession()?.token ?? null;
}

export function clearSession() {
  if (typeof window === "undefined") {
    return;
  }

  localStorage.removeItem(SESSION_KEY);
}

export function isAuthenticated() {
  return getToken() !== null;
}
