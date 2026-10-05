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

/** Champs saisissables pour créer ou modifier un club (id/validityEndDate/members gérés par le système). */
export interface ClubFormRequest {
  name: string;
  category: Category;
  postalAddress: string;
  email: string;
  phone: string;
}
