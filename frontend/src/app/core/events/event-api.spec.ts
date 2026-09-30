import { TestBed } from '@angular/core/testing';
import { EventApi } from './event-api';

describe('EventApi', () => {
  let service: EventApi;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EventApi);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
