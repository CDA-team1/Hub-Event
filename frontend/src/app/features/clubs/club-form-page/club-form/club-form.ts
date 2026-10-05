import { Component, input, linkedSignal, output } from '@angular/core';
import { email, FormField, form, required, submit } from '@angular/forms/signals';
import { Category } from '../../../../domain/category';
import { ClubFormRequest } from '../../../../domain/club.model';
import { CategoryLabelPipe } from '../../../../shared/pipes/category-label-pipe';

const CATEGORIES: Category[] = ['CULTURE', 'LEISURE', 'SPORT'];

interface ClubDraft {
  name: string;
  category: Category | '';
  postalAddress: string;
  email: string;
  phone: string;
}

function emptyDraft(): ClubDraft {
  return { name: '', category: '', postalAddress: '', email: '', phone: '' };
}

@Component({
  selector: 'app-club-form',
  imports: [FormField, CategoryLabelPipe],
  templateUrl: './club-form.html',
  styleUrl: './club-form.css',
})
export class ClubForm {
  /** Valeurs initiales en modification ; absent en création. */
  readonly initialValue = input<ClubFormRequest>();
  readonly submitted = output<ClubFormRequest>();
  readonly cancelled = output<void>();

  protected readonly categories = CATEGORIES;

  protected readonly draft = linkedSignal<ClubDraft>(() => this.initialValue() ?? emptyDraft());
  protected readonly clubForm = form(this.draft, (path) => {
    required(path.name, { message: 'Le nom du club est obligatoire.' });
    required(path.category, { message: 'La catégorie du club est obligatoire.' });
    required(path.postalAddress, { message: "L'adresse postale est obligatoire." });
    required(path.email, { message: "L'adresse email est obligatoire." });
    email(path.email, { message: 'Cet email n’est pas valide.' });
    required(path.phone, { message: 'Le numéro de téléphone est obligatoire.' });
  });

  protected async save(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.clubForm, async (field) => {
      const value = field().value();
      this.submitted.emit({ ...value, category: value.category as Category });
      return undefined;
    });
  }

  protected cancel(): void {
    this.cancelled.emit();
  }
}
