import { AccountDto } from './account.model';

export type AnonymizationRequestStatus = 'PENDING' | 'VALIDATED';

export interface AnonymizationDto {
  id: number;
  user: AccountDto;
  status: AnonymizationRequestStatus;
  requestDate: string;
}
