import { createContext, useContext, useMemo, useState, type ReactNode } from "react";

import { login as loginRequest } from "./api";
import { clearSession, getSession, saveSession, type AuthSession } from "./session";
import type { LoginRequest } from "./types";

type AuthContextValue = {
  session: AuthSession | null;
  isAuthenticated: boolean;
  login: (request: LoginRequest) => Promise<AuthSession>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

type AuthProviderProps = {
  children: ReactNode;
};

export function AuthProvider({ children }: AuthProviderProps) {
  const [session, setSession] = useState<AuthSession | null>(() => getSession());

  async function login(request: LoginRequest) {
    const response = await loginRequest(request);

    saveSession(response);

    const nextSession: AuthSession = {
      userId: response.userId,
      token: response.token,
      username: response.username,
      role: response.role,
    };

    setSession(nextSession);

    return nextSession;
  }

  function logout() {
    clearSession();
    setSession(null);
  }

  const value = useMemo(
    () => ({
      session,
      isAuthenticated: session !== null,
      login,
      logout,
    }),
    [session],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }

  return context;
}
