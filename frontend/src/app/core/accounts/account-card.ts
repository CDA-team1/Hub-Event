export type AccountStatus = 'INACTIVE' | 'ACTIVE' | 'ANONYMIZED';

export type AccountRole = 'MEMBER' | 'ORGANIZER' | 'ADMIN';

export interface AccountCard {
  id: number;
  lastName: string;
  firstName: string;
  postalAddress: string;
  email: string;
  phone: string | null;
  status: AccountStatus;
  role: AccountRole;
}
