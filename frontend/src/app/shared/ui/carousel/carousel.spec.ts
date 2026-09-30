import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Carousel } from './carousel';

@Component({
  imports: [Carousel],
  template: `
    <app-carousel [items]="items" (indexChanged)="lastIndex = $event">
      <ng-template let-item>{{ item }}</ng-template>
    </app-carousel>
  `,
})
class CarouselTestHost {
  items = ['a', 'b', 'c'];
  lastIndex: number | undefined;
}

describe('Carousel', () => {
  let fixture: ComponentFixture<CarouselTestHost>;
  let host: CarouselTestHost;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CarouselTestHost],
    }).compileComponents();

    fixture = TestBed.createComponent(CarouselTestHost);
    host = fixture.componentInstance;
    fixture.detectChanges();
  });

  function clickNext(): void {
    fixture.nativeElement.querySelector('.nav.next').click();
    fixture.detectChanges();
  }

  function clickPrevious(): void {
    fixture.nativeElement.querySelector('.nav.prev').click();
    fixture.detectChanges();
  }

  it('advances by one on next', () => {
    clickNext();
    expect(host.lastIndex).toBe(1);
  });

  it('goes back by one on previous', () => {
    clickNext();
    clickNext();
    clickPrevious();
    expect(host.lastIndex).toBe(1);
  });

  it('loops back to the first item after the last one', () => {
    clickNext();
    clickNext();
    clickNext();
    expect(host.lastIndex).toBe(0);
  });

  it('loops to the last item when going previous from the first one', () => {
    clickPrevious();
    expect(host.lastIndex).toBe(2);
  });

  it('responds to the right arrow key', () => {
    fixture.nativeElement
      .querySelector('.carousel')
      .dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowRight', bubbles: true }));
    fixture.detectChanges();
    expect(host.lastIndex).toBe(1);
  });

  it('responds to the left arrow key', () => {
    fixture.nativeElement
      .querySelector('.carousel')
      .dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowLeft', bubbles: true }));
    fixture.detectChanges();
    expect(host.lastIndex).toBe(2);
  });
});
