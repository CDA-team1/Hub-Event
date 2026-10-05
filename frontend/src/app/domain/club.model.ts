import { Category } from './category';
import { MemberSummaryDto } from './member-summary';

export interface ClubDto {
  id: number;
  name: string;
  category: Category;
  postalAddress: string;
  email: string;
  phone: string;
  validityEndDate: string | null;
  members: MemberSummaryDto[];
}
