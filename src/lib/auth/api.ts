import { apiRequest } from "../api/client";
import type { LoginRequest, LoginResponse, SignupRequest } from "./types";

export function login(request: LoginRequest) {
  return apiRequest<LoginResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function signup(request: SignupRequest) {
  return apiRequest<LoginResponse>("/auth/signup", {
    method: "POST",
    body: JSON.stringify(request),
  });
}
