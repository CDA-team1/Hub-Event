export type ClubCategory = 'SPORT' | 'CULTURE' | 'LEISURE';

export interface ClubCard {
  id: number;
  name: string;
  category: ClubCategory;
  postalAddress: string;
  email: string;
  phone: string;
  validityEndDate: string | null;
}
