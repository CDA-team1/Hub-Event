import { TestBed } from '@angular/core/testing';

import { EventDetailResponse, MyRegistrationDto } from '../../../domain/event.model';
import { RegistrationPanel } from './registration-panel';

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
  owner: false,
  myRegistration: null,
  gallery: [],
};

describe('RegistrationPanel', () => {
  async function render(
    event: Partial<EventDetailResponse> = {},
    inputs: Record<string, unknown> = {},
  ) {
    const fixture = TestBed.createComponent(RegistrationPanel);
    fixture.componentRef.setInput('event', { ...EVENT, ...event });
    for (const [name, value] of Object.entries(inputs)) {
      fixture.componentRef.setInput(name, value);
    }
    await fixture.whenStable();
    return { fixture, element: fixture.nativeElement as HTMLElement };
  }

  const button = (element: HTMLElement) =>
    element.querySelector<HTMLButtonElement>('.registration__button');

  it("propose un bouton S'inscrire actif pour un événement publié", async () => {
    const { element } = await render();

    expect(button(element)?.textContent?.trim()).toBe("S'inscrire");
    expect(button(element)?.disabled).toBe(false);
    expect(element.querySelector('.registration__hint')).toBeNull();
  });

  it('émet registerClicked au clic sur le bouton', async () => {
    const { fixture, element } = await render();
    const emitted: void[] = [];
    fixture.componentInstance.registerClicked.subscribe(() => emitted.push(undefined));

    button(element)!.click();

    expect(emitted).toHaveLength(1);
  });

  it.each(['DRAFT', 'CANCELLED', 'FINISHED'] as const)(
    "bloque le bouton et explique pourquoi quand l'événement est %s",
    async (status) => {
      const { element } = await render({ status });

      expect(button(element)?.disabled).toBe(true);
      expect(element.querySelector('.registration__hint')?.textContent).toContain(
        'ne sont pas ouvertes',
      );
    },
  );

  it('bloque le bouton pendant une inscription en cours', async () => {
    const { element } = await render({}, { pending: true });

    expect(button(element)?.disabled).toBe(true);
  });

  it('affiche le statut inscrit à la place du bouton', async () => {
    const myRegistration: MyRegistrationDto = { status: 'REGISTERED', waitingPosition: null };

    const { element } = await render({ myRegistration });

    expect(button(element)).toBeNull();
    expect(element.querySelector('.registration__status')?.textContent).toContain(
      'Vous êtes inscrit',
    );
  });

  it("affiche la position en liste d'attente", async () => {
    const myRegistration: MyRegistrationDto = { status: 'WAITING_LIST', waitingPosition: 3 };

    const { element } = await render({ myRegistration });

    expect(button(element)).toBeNull();
    const status = element.querySelector('.registration__status')?.textContent;
    expect(status).toContain("liste d'attente");
    expect(status).toContain('position 3');
  });

  it("affiche le message d'erreur reçu", async () => {
    const { element } = await render(
      {},
      { errorMessage: 'Vous êtes déjà inscrit à cet événement.' },
    );

    const error = element.querySelector('.registration__error');
    expect(error?.textContent).toBe('Vous êtes déjà inscrit à cet événement.');
    expect(error?.getAttribute('role')).toBe('alert');
  });
});
