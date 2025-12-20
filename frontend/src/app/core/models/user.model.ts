export interface User {
  id: number;
  userKey?: string;
  email: string;
  name: string;
  isEnabled: boolean;
  isPasswordTemporary?: boolean;
  isSuperAdmin: boolean;
  isDeactive: boolean;
  roles?: string;
  createdAt?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  name: string;
  roles: string[];
}

