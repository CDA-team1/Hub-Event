import { HttpClient, HttpContext, HttpErrorResponse } from '@angular/common/http';
import { Service, computed, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { Role } from '../../domain/role';
import { SKIP_AUTH_REDIRECT } from '../http/error-interceptor';
import { API_URL } from '../http/api-url';
import { persistedSignal } from '../storage/persisted-signal';

interface Session {
  readonly token: string;
  readonly role: Role;
}

interface LoginResponse {
  readonly token: string;
  readonly role: Role;
}

const SESSION_KEY = 'hub-event.session.v1';

function isSession(value: unknown): value is Session | null {
  if (value === null) {
    return true;
  }
  const session = value as Partial<Record<keyof Session, unknown>>;
  return typeof session.token === 'string' && typeof session.role === 'string';
}

/**
 * Session de l'utilisateur connecté : jeton JWT et rôle, conservés dans le `sessionStorage`
 * (perdus à la fermeture de l'onglet, contrairement à un `localStorage`).
 */
@Service()
export class Auth {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_URL);
  private readonly session = persistedSignal<Session | null>(
    SESSION_KEY,
    null,
    isSession,
    sessionStorage,
  );

  readonly isAuthenticated = computed(() => this.session() !== null);
  readonly role = computed(() => this.session()?.role);
  readonly token = computed(() => this.session()?.token);

  /**
   * Connecte l'utilisateur. Un 401 (identifiants invalides, compte non actif ou suspendu) ne
   * doit pas déclencher la redirection générique de session expirée : la requête est marquée
   * avec {@link SKIP_AUTH_REDIRECT}, le message d'erreur réel du back reste inchangé.
   */
  async login(email: string, password: string): Promise<void> {
    const response = await firstValueFrom(
      this.http.post<LoginResponse>(
        `${this.apiUrl}/auth/login`,
        { email, password },
        { context: new HttpContext().set(SKIP_AUTH_REDIRECT, true) },
      ),
    );
    this.session.set({ token: response.token, role: response.role });
  }

  logout(): void {
    this.session.set(null);
  }
}

/** Message d'erreur à afficher pour un login refusé, tel que renvoyé par le back. */
export function loginErrorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse && typeof error.error === 'string') {
    return error.error;
  }
  return 'La connexion a échoué.';
}
