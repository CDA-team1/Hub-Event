import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import localeFr from '@angular/common/locales/fr';
import { ApplicationConfig, LOCALE_ID, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { errorInterceptor } from './core/http/error-interceptor';
import { jwtInterceptor } from './core/http/jwt-interceptor';
import { loadingInterceptor } from './core/http/loading-interceptor';
import { routes } from './app.routes';

registerLocaleData(localeFr);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([loadingInterceptor, jwtInterceptor, errorInterceptor])),
    { provide: LOCALE_ID, useValue: 'fr' },
  ],
};
