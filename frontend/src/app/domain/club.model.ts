import { Category } from './category';

export interface ClubCardDto {
  id: number;
  name: string;
  category: Category;
  postalAddress: string;
  email: string;
  phone: string;
  validityEndDate: string | null;
}
