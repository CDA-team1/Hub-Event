import { TestBed } from '@angular/core/testing';

import { EventDetailResponse } from '../../../domain/event.model';
import { OwnerActions } from './owner-actions';

const EVENT: EventDetailResponse = {
  title: 'Soirée jeux au café ludique',
  description: 'Retrouvez vos amis autour de jeux de société.',
  location: 'Café ludique Les Quatre As, 69001 Lyon',
  startDateTime: '2026-10-16T19:30:00',
  endDateTime: '2026-10-16T23:00:00',
  affiliatedPrice: 3,
  nonAffiliatedPrice: 6,
  maxSeats: 24,
  category: 'LEISURE',
  status: 'PUBLISHED',
  remainingSeats: 24,
  waitingCount: 0,
  owner: true,
  myRegistration: null,
  gallery: [],
};

type OutputName =
  'editClicked' | 'publishClicked' | 'finishClicked' | 'cancelClicked' | 'deleteClicked';

const CLICKS: [OutputName, string, Partial<EventDetailResponse>][] = [
  ['editClicked', 'Modifier', { status: 'DRAFT' }],
  ['publishClicked', 'Publier', { status: 'DRAFT' }],
  ['deleteClicked', 'Supprimer', { status: 'DRAFT' }],
  ['finishClicked', 'Terminer', { status: 'PUBLISHED' }],
  ['cancelClicked', "Annuler l'événement", { status: 'PUBLISHED', remainingSeats: 20 }],
];

describe('OwnerActions', () => {
  async function render(
    event: Partial<EventDetailResponse> = {},
    inputs: Record<string, unknown> = {},
  ) {
    const fixture = TestBed.createComponent(OwnerActions);
    fixture.componentRef.setInput('event', { ...EVENT, ...event });

    for (const [name, value] of Object.entries(inputs)) {
      fixture.componentRef.setInput(name, value);
    }

    await fixture.whenStable();

    return {
      fixture,
      element: fixture.nativeElement as HTMLElement,
    };
  }

  const buttons = (element: HTMLElement) =>
    Array.from(element.querySelectorAll<HTMLButtonElement>('.owner-actions__button'));

  const labels = (element: HTMLElement) => buttons(element).map((b) => b.textContent?.trim());

  it('propose Modifier, Publier et Supprimer pour un brouillon', async () => {
    const { element } = await render({ status: 'DRAFT' });

    expect(labels(element)).toEqual(['Modifier', 'Publier', 'Supprimer']);
  });

  it('propose Modifier, Terminer et Annuler pour un événement publié avec des inscrits', async () => {
    const { element } = await render({ status: 'PUBLISHED', remainingSeats: 20 });

    expect(labels(element)).toEqual(['Modifier', 'Terminer', "Annuler l'événement"]);
  });

  it("propose Supprimer à la place d'Annuler pour un événement publié sans inscrit", async () => {
    const { element } = await render({ status: 'PUBLISHED', remainingSeats: 24 });

    expect(labels(element)).toEqual(['Modifier', 'Terminer', 'Supprimer']);
  });

  it.each(['CANCELLED', 'FINISHED'] as const)(
    "ne propose que Modifier quand l'événement est %s",
    async (status) => {
      const { element } = await render({ status });

      expect(labels(element)).toEqual(['Modifier']);
    },
  );

  it.each(CLICKS)('émet %s au clic sur « %s »', async (output, label, event) => {
    const { fixture, element } = await render(event);
    const emitted: void[] = [];

    fixture.componentInstance[output].subscribe(() => emitted.push(undefined));

    buttons(element)
      .find((b) => b.textContent?.trim() === label)!
      .click();

    expect(emitted).toHaveLength(1);
  });

  it('bloque tous les boutons pendant une action en cours', async () => {
    const { element } = await render({ status: 'DRAFT' }, { pending: true });

    expect(buttons(element)).toHaveLength(3);
    expect(buttons(element).every((b) => b.disabled)).toBe(true);
  });

  it("n'affiche pas de message d'erreur par défaut", async () => {
    const { element } = await render();

    expect(element.querySelector('.owner-actions__error')).toBeNull();
  });

  it("affiche le message d'erreur reçu", async () => {
    const { element } = await render(
      {},
      { errorMessage: 'Cet événement ne peut pas être publié.' },
    );

    const error = element.querySelector('.owner-actions__error');

    expect(error?.textContent).toBe('Cet événement ne peut pas être publié.');
    expect(error?.getAttribute('role')).toBe('alert');
  });
});
