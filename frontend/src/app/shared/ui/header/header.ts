import { Component, ElementRef, computed, inject, signal, viewChild } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { Auth } from '../../../core/auth/auth';

@Component({
  imports: [RouterLink, RouterLinkActive],
  selector: 'app-header',
  styleUrl: './header.css',
  templateUrl: './header.html',
  host: {
    '(document:click)': 'onDocumentClick($event)',
  },
})
export class Header {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  private readonly adminMenu = viewChild<ElementRef<HTMLDetailsElement>>('adminMenu');

  protected readonly isConnected = this.auth.isAuthenticated;
  protected readonly isAdmin = computed(() => this.auth.role() === 'ADMIN');
  protected readonly isOrganizer = computed(() => this.auth.role() === 'ORGANIZER');
  protected readonly menuOpen = signal(false);

  protected async logout(): Promise<void> {
    this.auth.logout();
    await this.router.navigate(['/']);
  }

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  /** Referme le menu mobile après un clic sur un lien ou sur « Se déconnecter ». */
  protected onMenuClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).closest('a, .header__logout')) {
      this.menuOpen.set(false);
    }
  }

  protected onDocumentClick(event: MouseEvent): void {
    const menu = this.adminMenu()?.nativeElement;
    if (menu?.open && !menu.contains(event.target as Node)) {
      menu.open = false;
    }
  }
}
