import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Confirmation } from '../../../core/dialog/confirmation';
import { toIsoDate } from '../../../domain/calendar-rules';
import { EventCardDto } from '../../../domain/event.model';
import { CalendarPage } from './calendar-page';

// Toujours dans la semaine courante (vue par défaut), quelle que soit la date d'exécution du test.
function todayEvent(id: number, title: string): EventCardDto {
  return {
    id,
    title,
    location: 'Paris',
    startDateTime: `${toIsoDate(new Date())}T20:00:00`,
    endDateTime: null,
    affiliatedPrice: 0,
    nonAffiliatedPrice: 0,
    category: 'CULTURE',
    imageUrl: null,
  };
}

describe('CalendarPage', () => {
  let http: HttpTestingController;

  // Un `await` supplémentaire (macrotâche) laisse le temps aux chaînes de promesses internes
  // (confirm() → unregister() → reload()) de se terminer avant de vérifier le rendu.
  const stable = async () => {
    await new Promise((resolve) => setTimeout(resolve));
    await TestBed.inject(ApplicationRef).whenStable();
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('charge la semaine courante par défaut et affiche les évènements inscrits', async () => {
    const fixture = TestBed.createComponent(CalendarPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/calendar')).flush([todayEvent(1, 'Concert')]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Concert');
    expect(element.querySelectorAll('.day--highlighted')).toHaveLength(1);
  });

  it("affiche un état vide quand il n'y a aucun évènement sur la période", async () => {
    const fixture = TestBed.createComponent(CalendarPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/calendar')).flush([]);
    await stable();

    expect((fixture.nativeElement as HTMLElement).querySelector('app-empty-state')).not.toBeNull();
  });

  it('recharge avec une nouvelle plage quand on bascule en vue Mois', async () => {
    const fixture = TestBed.createComponent(CalendarPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/calendar')).flush([]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    const monthButton = Array.from(element.querySelectorAll('button')).find(
      (button) => button.textContent?.trim() === 'Mois',
    )!;
    monthButton.click();
    TestBed.tick();

    const request = http.expectOne((req) => req.url.endsWith('/calendar'));
    expect(request.request.params.get('from')?.endsWith('-01')).toBe(true);
    request.flush([]);
    await stable();
  });

  it('se désinscrit après confirmation et recharge la liste', async () => {
    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);

    const fixture = TestBed.createComponent(CalendarPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/calendar')).flush([todayEvent(1, 'Concert')]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('td.actions button:last-child')!.click();
    await stable();

    http.expectOne((req) => req.url.endsWith('/events/1/registrations/me')).flush(null);
    // Pause sans whenStable() ici : whenStable() déclencherait lui-même reload() en vidant la
    // file de changements, verrait la nouvelle requête /calendar en attente, et resterait
    // bloqué dessus puisqu'elle n'est flushée que sur la ligne suivante.
    await new Promise((resolve) => setTimeout(resolve));
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/calendar')).flush([]);
    await stable();

    expect(element.querySelector('app-empty-state')).not.toBeNull();
  });

  it('ne se désinscrit pas si on annule la confirmation', async () => {
    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(false);

    const fixture = TestBed.createComponent(CalendarPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/calendar')).flush([todayEvent(1, 'Concert')]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('td.actions button:last-child')!.click();
    await stable();

    expect(element.textContent).toContain('Concert');
  });
});
