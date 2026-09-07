export type UserRole = "ADMIN" | "OPERATOR" | "VIEWER";

export type LoginRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  token: string;
  username: string;
  role: UserRole;
};
