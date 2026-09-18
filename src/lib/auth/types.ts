export type UserRole = "ADMIN" | "OPERATOR" | "VIEWER";

export type LoginRequest = {
  username: string;
  password: string;
};

export type SignupRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  userId: number;
  token: string;
  username: string;
  role: UserRole;
};
