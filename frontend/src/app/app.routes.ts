import {Routes} from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    title: 'Accueil',
    loadComponent: () =>
      import('./features/events/events-home-page/events-home-page').then((m) => m.EventsHomePage,
      ),
  },
  {
    path: 'clubs',
    title: 'Clubs',
    loadComponent: () =>
      import('./features/clubs/clubs-list-page/clubs-list-page').then((m) => m.ClubsListPage,
      ),
  },
  {
    path: 'connexion',
    title: 'Connexion',
    loadComponent: () =>
      import('./features/auth/login-page/login-page').then((m) => m.LoginPage,
      ),
  },
  {
    path: '**',
    title: 'Page introuvable',
    loadComponent: () =>
      import('./features/not-found/not-found-page/not-found-page').then((m) => m.NotFoundPage,
      ),
  },
];
