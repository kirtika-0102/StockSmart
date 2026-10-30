export const USER_ROLES = ['ADMIN', 'INVENTORY_MANAGER', 'STAFF'] as const;

export type UserRole = (typeof USER_ROLES)[number];

export interface UserRecord {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  active: boolean;
  roles: string[];
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: UserRole;
}

export interface UpdateUserRequest {
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  active: boolean;
}
