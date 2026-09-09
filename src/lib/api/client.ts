import { clearSession, getToken } from "../auth/session";

const API_BASE_URL = "http://localhost:8080/api";

export async function apiRequest<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const token = getToken();

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options?.headers ?? {}),
    },
    ...options,
  });

  if (!response.ok) {
    let message = `API request failed with status ${response.status}`;

    try {
      const error = await response.json();

      if (error?.message) {
        message = error.message;
      }
    } catch {
      // Ignore invalid/non-JSON error responses.
    }

    if (response.status === 401) {
      clearSession();
      message = "Your session has expired. Please log in again.";
    }

    if (response.status === 403) {
      message = "You do not have permission to perform this action.";
    }

    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}
