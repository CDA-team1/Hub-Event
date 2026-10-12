import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { EventCardDto } from '../../../domain/event.model';
import { EventCard } from './event-card';

describe('EventCard', () => {
  const event: EventCardDto = {
    id: 7,
    title: 'Concert de jazz',
    location: 'Toulouse',
    startDateTime: '2026-12-12T19:00:00',
    endDateTime: null,
    affiliatedPrice: 8,
    nonAffiliatedPrice: 12,
    category: 'CULTURE',
    imageUrl: null,
  };

  async function render(value: EventCardDto) {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(EventCard);
    fixture.componentRef.setInput('event', value);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it("affiche l'illustration de sa catégorie quand l'évènement n'a pas de photo", async () => {
    const element = await render(event);

    expect(element.querySelector('img')?.getAttribute('src')).toBe('placeholders/culture.webp');
  });

  it('affiche la photo de preview quand elle existe', async () => {
    const element = await render({ ...event, imageUrl: 'https://i.ibb.co/a/jazz.webp' });

    expect(element.querySelector('img')?.getAttribute('src')).toBe('https://i.ibb.co/a/jazz.webp');
  });
});
