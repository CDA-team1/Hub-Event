import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClubsListPage } from './clubs-list-page';

describe('ClubsListPage', () => {
  let component: ClubsListPage;
  let fixture: ComponentFixture<ClubsListPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClubsListPage]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ClubsListPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
