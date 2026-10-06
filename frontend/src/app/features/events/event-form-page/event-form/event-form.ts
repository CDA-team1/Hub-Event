import { Component, input, linkedSignal, output } from '@angular/core';
import { disabled, FormField, form, min, required, submit } from '@angular/forms/signals';
import { Category } from '../../../../domain/category';
import { ClubDto } from '../../../../domain/club.model';
import { CategoryLabelPipe } from '../../../../shared/pipes/category-label-pipe';

const CATEGORIES: Category[] = ['CULTURE', 'LEISURE', 'SPORT'];

export interface EventFormValue {
  title: string;
  description: string;
  location: string;
  category: Category | '';
  startDateTime: string;
  endDateTime: string;
  affiliatedPrice: number | null;
  nonAffiliatedPrice: number | null;
  maxSeats: number | null;
  /** Toujours une chaîne : un <select> natif ne restitue jamais autre chose (voir club.category). */
  clubId: string;
}

function emptyDraft(): EventFormValue {
  return {
    title: '',
    description: '',
    location: '',
    category: '',
    startDateTime: '',
    endDateTime: '',
    affiliatedPrice: null,
    nonAffiliatedPrice: null,
    maxSeats: null,
    clubId: '',
  };
}

@Component({
  selector: 'app-event-form',
  imports: [FormField, CategoryLabelPipe],
  templateUrl: './event-form.html',
  styleUrl: './event-form.css',
})
export class EventForm {
  /** Valeurs initiales en modification ; absent en création. */
  readonly initialValue = input<EventFormValue>();
  /** Clubs actifs de l'organisateur, pour le choix du club à la création uniquement. */
  readonly clubs = input<ClubDto[]>();
  /** Événement FINISHED (EVT-06) : formulaire entièrement verrouillé. */
  readonly locked = input(false);

  readonly submitted = output<EventFormValue>();
  readonly cancelled = output<void>();

  protected readonly categories = CATEGORIES;
  protected readonly isEditMode = () => this.clubs() === undefined;

  protected readonly draft = linkedSignal<EventFormValue>(() => this.initialValue() ?? emptyDraft());
  protected readonly eventForm = form(this.draft, (path) => {
    required(path.title, { message: 'Le titre est obligatoire.' });
    required(path.description, { message: 'La description est obligatoire.' });
    required(path.location, { message: 'Le lieu est obligatoire.' });
    required(path.category, { message: 'La catégorie est obligatoire.' });
    required(path.startDateTime, { message: 'La date de début est obligatoire.' });
    required(path.affiliatedPrice, { message: 'Le tarif affilié est obligatoire.' });
    min(path.affiliatedPrice, 0, { message: 'Le tarif affilié doit être supérieur ou égal à zéro.' });
    required(path.nonAffiliatedPrice, { message: 'Le tarif non affilié est obligatoire.' });
    min(path.nonAffiliatedPrice, 0, {
      message: 'Le tarif non affilié doit être supérieur ou égal à zéro.',
    });
    required(path.maxSeats, { message: 'Le nombre de places est obligatoire.' });
    min(path.maxSeats, 1, { message: 'Le nombre de places doit être supérieur à zéro.' });
    required(path.clubId, {
      message: 'Le club organisateur est obligatoire.',
      when: () => !this.isEditMode(),
    });

    disabled(path, () => this.locked());
  });

  protected async save(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.eventForm, async (field) => {
      this.submitted.emit(field().value());
      return undefined;
    });
  }

  protected cancel(): void {
    this.cancelled.emit();
  }
}
