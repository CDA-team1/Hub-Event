import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { Auth } from '../../../core/auth/auth';

@Component({
  imports: [RouterLink, RouterLinkActive],
  selector: 'app-header',
  styleUrl: './header.css',
  templateUrl: './header.html',
})
export class Header {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  protected readonly isConnected = this.auth.isAuthenticated;
  protected readonly isAdmin = computed(() => this.auth.role() === 'ADMIN');

  protected async logout(): Promise<void> {
    this.auth.logout();
    await this.router.navigate(['/']);
  }
}
