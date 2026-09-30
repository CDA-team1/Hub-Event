import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EventsHomePage } from './events-home-page';

describe('EventsHomePage', () => {
  let component: EventsHomePage;
  let fixture: ComponentFixture<EventsHomePage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventsHomePage]
    })
      .compileComponents();

    fixture = TestBed.createComponent(EventsHomePage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
