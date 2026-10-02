import { Category } from './category';

export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'CANCELLED' | 'FINISHED';

export interface EventCardDto {
  id: number;
  title: string;
  location: string;
  startDateTime: string;
  endDateTime: string | null;
  affiliatedPrice: number;
  nonAffiliatedPrice: number;
  category: Category;
}

export interface EventListDto {
  cultureEvents: EventCardDto[];
  leisureEvents: EventCardDto[];
  sportEvents: EventCardDto[];
  pastEvents: EventCardDto[];
}

export interface EventDetailResponse {
  title: string;
  description: string;
  location: string;
  startDateTime: string;
  endDateTime: string | null;
  affiliatedPrice: number;
  maxSeats: number;
}

export interface EventDto {
  id: number;
  title: string;
  description: string;
  location: string;
  startDateTime: string;
  endDateTime: string | null;
  affiliatedPrice: number;
  nonAffiliatedPrice: number;
  maxSeats: number;
  status: EventStatus;
  category: Category;
  organizerId: number;
  clubId: number;
}

export interface EventSearchCriteria {
  category?: Category;
  minPrice?: number;
  maxPrice?: number;
  location?: string;
  startDate?: string;
  endDate?: string;
  keywords?: string;
}

export interface CreateEventRequest {
  title: string;
  description: string;
  location: string;
  startDateTime: string;
  endDateTime: string | null;
  affiliatedPrice: number;
  nomAffiliatedPrice: number;
  maxSeats: number;
  category: Category;
  clubId: number;
}

export interface UpdateEventRequest {
  title: string;
  description: string;
  location: string;
  startDateTime: string;
  endDateTime: string | null;
  affiliatedPrice: number;
  nonAffiliatedPrice: number;
  maxSeats: number;
  category: Category;
}

export interface ImageDto {
  id: number;
  eventId: number;
  url: string;
}

export interface CommentDto {
  id: number;
  eventId: number;
  authorDisplayName: string;
  content: string;
  createdAt: string;
}
