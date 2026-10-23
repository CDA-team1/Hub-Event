import { AccountDto, AdminUserDto } from './account.model';

export type AnonymizationRequestStatus = 'PENDING' | 'VALIDATED';

export interface AnonymizationDto {
  id: number;
  user: AccountDto;
  status: AnonymizationRequestStatus;
  requestDate: string;
}

export interface AdminAnonymizationDto {
  id: number;
  user: AdminUserDto;
  status: AnonymizationRequestStatus;
  requestDate: string;
}
