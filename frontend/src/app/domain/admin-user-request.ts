import { Role } from './role';

export interface AdminUserRequest {
  lastName: string;
  firstName: string;
  postalAddress: string;
  email: string;
  phone: string | null;
  role: Role;
  clubIds: number[];
}
