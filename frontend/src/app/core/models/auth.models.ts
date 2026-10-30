export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresInMs: number;
  username: string;
  fullName: string;
  roles: string[];
}

export interface CurrentUser {
  username: string;
  fullName: string;
  roles: string[];
}

export interface LoginRequest {
  username: string;
  password: string;
}
