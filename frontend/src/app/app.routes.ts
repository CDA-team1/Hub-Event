import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    title: 'Accueil',
    loadComponent: () =>
      import('./features/events/events-home-page/events-home-page').then((m) => m.EventsHomePage),
  },
  {
    path: 'clubs',
    title: 'Clubs',
    loadComponent: () =>
      import('./features/clubs/clubs-list-page/clubs-list-page').then((m) => m.ClubsListPage),
  },
  {
    path: 'inscription',
    title: 'Créer un compte',
    loadComponent: () =>
      import('./features/account/signup-page/signup-page').then((m) => m.SignupPage),
  },
  {
    path: '**',
    title: 'Page introuvable',
    loadComponent: () =>
      import('./features/not-found/not-found-page/not-found-page').then((m) => m.NotFoundPage),
  },
];
