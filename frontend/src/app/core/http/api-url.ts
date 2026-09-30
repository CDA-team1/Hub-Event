import { InjectionToken } from '@angular/core';
import { environment } from '../../../environments/environment';

/** Adresse de base de l'API ; en développement, le proxy Angular la redirige vers le back local. */
export const API_URL = new InjectionToken<string>('API_URL', {
  factory: () => environment.apiUrl,
});
