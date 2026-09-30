export type EventCategory = 'SPORT' | 'CULTURE' | 'LEISURE';

export interface EventCard {
  id: number;
  title: string;
  location: string;
  startDateTime: string;
  endDateTime: string | null;
  affiliatedPrice: number;
  nonAffiliatedPrice: number;
  category: EventCategory;
}
