import { Component, input, linkedSignal, output } from '@angular/core';
import { FormField, form, submit, validate } from '@angular/forms/signals';
import { Category } from '../../../../domain/category';
import { EventSearchCriteria } from '../../../../domain/event.model';
import { CategoryLabelPipe } from '../../../../shared/pipes/category-label-pipe';

const CATEGORIES: Category[] = ['CULTURE', 'LEISURE', 'SPORT'];

interface SearchDraft {
  category: Category | '';
  minPrice: string;
  maxPrice: string;
  location: string;
  startDate: string;
  endDate: string;
  keywords: string;
}

function draftFrom(criteria: EventSearchCriteria): SearchDraft {
  return {
    category: criteria.category ?? '',
    minPrice: criteria.minPrice === undefined ? '' : String(criteria.minPrice),
    maxPrice: criteria.maxPrice === undefined ? '' : String(criteria.maxPrice),
    location: criteria.location ?? '',
    startDate: criteria.startDate ?? '',
    endDate: criteria.endDate ?? '',
    keywords: criteria.keywords ?? '',
  };
}

/** Prix saisi sous forme de texte : `undefined` si vide, `NaN` si ce n'est pas un nombre. */
function parsePrice(value: string): number | undefined {
  const text = value.trim().replace(',', '.');
  return text === '' ? undefined : Number(text);
}

function isInvalidPrice(value: string): boolean {
  const price = parsePrice(value);
  return price !== undefined && !(Number.isFinite(price) && price >= 0);
}

function toCriteria(draft: SearchDraft): EventSearchCriteria {
  const criteria: EventSearchCriteria = {};
  const minPrice = parsePrice(draft.minPrice);
  const maxPrice = parsePrice(draft.maxPrice);
  const location = draft.location.trim();
  const keywords = draft.keywords.trim();

  if (draft.category) criteria.category = draft.category;
  if (minPrice !== undefined) criteria.minPrice = minPrice;
  if (maxPrice !== undefined) criteria.maxPrice = maxPrice;
  if (location) criteria.location = location;
  if (draft.startDate) criteria.startDate = draft.startDate;
  if (draft.endDate) criteria.endDate = draft.endDate;
  if (keywords) criteria.keywords = keywords;
  return criteria;
}

@Component({
  selector: 'app-search-form',
  imports: [FormField, CategoryLabelPipe],
  templateUrl: './search-form.html',
  styleUrl: './search-form.css',
})
export class SearchForm {
  /** Critères déjà actifs (lus dans l'URL) : le formulaire s'y pré-remplit. */
  readonly criteria = input<EventSearchCriteria>({});
  readonly criteriaChanged = output<EventSearchCriteria>();

  protected readonly categories = CATEGORIES;

  protected readonly draft = linkedSignal<SearchDraft>(() => draftFrom(this.criteria()));
  protected readonly searchForm = form(this.draft, (path) => {
    validate(path.minPrice, ({ value }) =>
      isInvalidPrice(value())
        ? { kind: 'minPrice', message: 'Le prix minimum doit être un nombre positif.' }
        : undefined,
    );

    validate(path.maxPrice, ({ value, valueOf }) => {
      if (isInvalidPrice(value())) {
        return { kind: 'maxPrice', message: 'Le prix maximum doit être un nombre positif.' };
      }
      const min = parsePrice(valueOf(path.minPrice));
      const max = parsePrice(value());
      return min !== undefined && max !== undefined && max < min
        ? {
            kind: 'priceRange',
            message: 'Le prix maximum doit être supérieur ou égal au prix minimum.',
          }
        : undefined;
    });

    validate(path.endDate, ({ value, valueOf }) => {
      const start = valueOf(path.startDate);
      const end = value();
      return start && end && end < start
        ? { kind: 'dateRange', message: 'La date de fin doit être postérieure à la date de début.' }
        : undefined;
    });
  });

  protected async search(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.searchForm, async (field) => {
      this.criteriaChanged.emit(toCriteria(field().value()));
      return undefined;
    });
  }

  protected reset(): void {
    this.draft.set(draftFrom({}));
    this.criteriaChanged.emit({});
  }
}
