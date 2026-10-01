import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-event-detail-page',
  styleUrl: './event-detail-page.css',
  templateUrl: './event-detail-page.html',
})
export class EventDetailPage {
  private readonly route = inject(ActivatedRoute);

  protected readonly eventId = this.route.snapshot.paramMap.get('id');
}
