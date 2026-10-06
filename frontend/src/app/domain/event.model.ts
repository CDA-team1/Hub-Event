import { Category } from './category';

export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'CANCELLED' | 'FINISHED';

export type RegistrationStatus = 'REGISTERED' | 'WAITING_LIST';

export interface MyRegistrationDto {
  status: RegistrationStatus;
  waitingPosition: number | null;
}

export interface RegistrationDto {
  id: number;
  eventId: number;
  userEmail: string;
  status: RegistrationStatus;
  registrationDate: string;
}

export interface EventCardDto {
  id: number;
  title: string;
  location: string;
  startDateTime: string;
  endDateTime: string | null;
  affiliatedPrice: number;
  nonAffiliatedPrice: number;
  category: Category;
  imageUrl: string | null;
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
  nonAffiliatedPrice: number;
  maxSeats: number;
  category: Category;
  status: EventStatus;
  remainingSeats: number;
  waitingCount: number;
  owner: boolean;
  myRegistration: MyRegistrationDto | null;
  gallery: ImageDto[];
}

export interface OrganizerEventDto {
  id: number;
  title: string;
  category: Category;
  startDateTime: string;
  endDateTime: string | null;
  status: EventStatus;
  maxSeats: number;
  registeredCount: number;
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
  isPreview: boolean;
}

export interface CommentDto {
  id: number;
  eventId: number;
  authorDisplayName: string;
  content: string;
  createdAt: string;
}
