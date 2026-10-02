import { Role } from './role';

export type AccountStatus = 'INACTIVE' | 'ACTIVE' | 'ANONYMIZED';

export interface AccountDto {
  id: number;
  lastName: string;
  firstName: string;
  postalAddress: string;
  email: string;
  phone: string | null;
  status: AccountStatus;
  role: Role;
}

export interface ClubSummaryDto {
  id: number;
  name: string;
}

export interface UserProfileDto extends AccountDto {
  clubs: ClubSummaryDto[];
}
