import {Component, signal} from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  imports: [RouterLink, RouterLinkActive],
  selector: 'app-header',
  styleUrl: './header.css',
  templateUrl: './header.html',
})
export class Header {
  // Signal local temporaire : sera remplacé par le service Auth (core/auth) à AUTH-01
  protected readonly isConnected = signal(false);

  protected toggleConnected(): void {
    this.isConnected.update((value) => !value);
  }
}
