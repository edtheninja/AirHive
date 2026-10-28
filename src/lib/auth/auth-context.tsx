import {
  createContext,
  useContext,
  useMemo,
  useState,
  useEffect,
  useCallback,
  type ReactNode,
} from "react";
import { disconnectWebSocket } from "@/lib/api/websocket";
import { login as loginRequest, signup as signupRequest } from "./api";
import { clearSession, getSession, saveSession, type AuthSession } from "./session";
import type { LoginRequest, SignupRequest } from "./types";

type AuthContextValue = {
  session: AuthSession | null;
  isAuthenticated: boolean;
  login: (request: LoginRequest) => Promise<AuthSession>;
  signup: (request: SignupRequest) => Promise<AuthSession>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

type AuthProviderProps = {
  children: ReactNode;
};

export function AuthProvider({ children }: AuthProviderProps) {
  const [session, setSession] = useState<AuthSession | null>(null);

  useEffect(() => {
    setSession(getSession());
  }, []);

  const createSession = useCallback(
    (response: { userId: number; token: string; username: string; role: AuthSession["role"] }) => {
      saveSession(response);

      const nextSession: AuthSession = {
        userId: response.userId,
        token: response.token,
        username: response.username,
        role: response.role,
      };

      setSession(nextSession);

      return nextSession;
    },
    [],
  );

  const login = useCallback(
    async (request: LoginRequest) => {
      const response = await loginRequest(request);
      return createSession(response);
    },
    [createSession],
  );

  const signup = useCallback(
    async (request: SignupRequest) => {
      const response = await signupRequest(request);
      return createSession(response);
    },
    [createSession],
  );

  const logout = useCallback(() => {
    clearSession();
    setSession(null);
    void disconnectWebSocket();
  }, []);

  const value = useMemo(
    () => ({
      session,
      isAuthenticated: session !== null,
      login,
      signup,
      logout,
    }),
    [session, login, signup, logout],
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
