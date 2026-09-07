import { apiRequest } from "../api/client";
import type { LoginRequest, LoginResponse } from "./types";

export function login(request: LoginRequest) {
  return apiRequest<LoginResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify(request),
  });
}
