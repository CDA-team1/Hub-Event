import { Category } from './category';
import { EventSearchCriteria } from './event.model';

/** Lecteur de paramètres d'URL : `ParamMap` d'Angular et `URLSearchParams` le satisfont. */
export interface ParamReader {
  get(name: string): string | null;
}

const CATEGORIES: readonly Category[] = ['CULTURE', 'LEISURE', 'SPORT'];
const ISO_DATE = /^\d{4}-\d{2}-\d{2}$/;

function readText(value: string | null): string | undefined {
  const text = value?.trim();
  return text ? text : undefined;
}

function readPrice(value: string | null): number | undefined {
  const text = readText(value);
  if (text === undefined) {
    return undefined;
  }
  const price = Number(text);
  return Number.isFinite(price) && price >= 0 ? price : undefined;
}

function readDate(value: string | null): string | undefined {
  return value !== null && ISO_DATE.test(value) ? value : undefined;
}

/** Critères de recherche lus dans l'URL ; toute valeur absente ou invalide est ignorée. */
export function criteriaFromParams(params: ParamReader): EventSearchCriteria {
  const category = params.get('category');
  return {
    category: CATEGORIES.find((candidate) => candidate === category),
    minPrice: readPrice(params.get('minPrice')),
    maxPrice: readPrice(params.get('maxPrice')),
    location: readText(params.get('location')),
    startDate: readDate(params.get('startDate')),
    endDate: readDate(params.get('endDate')),
    keywords: readText(params.get('keywords')),
  };
}

/** Paramètres d'URL (identiques à ceux de l'API) correspondant aux critères renseignés. */
export function criteriaToParams(criteria: EventSearchCriteria): Record<string, string> {
  const params: Record<string, string> = {};
  if (criteria.category) params['category'] = criteria.category;
  if (criteria.minPrice !== undefined) params['minPrice'] = String(criteria.minPrice);
  if (criteria.maxPrice !== undefined) params['maxPrice'] = String(criteria.maxPrice);
  if (criteria.location) params['location'] = criteria.location;
  if (criteria.startDate) params['startDate'] = criteria.startDate;
  if (criteria.endDate) params['endDate'] = criteria.endDate;
  if (criteria.keywords) params['keywords'] = criteria.keywords;
  return params;
}
