import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth-guard';

export const routes: Routes = [
  {
    path: '',
    title: 'Accueil',
    loadComponent: () =>
      import('./features/events/events-home-page/events-home-page').then((m) => m.EventsHomePage),
  },

  {
    path: 'evenements/:id',
    title: "Détail de l'événement",
    loadComponent: () =>
      import('./features/events/event-detail-page/event-detail-page').then(
        (m) => m.EventDetailPage,
      ),
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
      import('./features/auth/signup-page/signup-page').then((m) => m.SignupPage),
  },
  {
    path: 'connexion',
    title: 'Connexion',
    loadComponent: () => import('./features/auth/login-page/login-page').then((m) => m.LoginPage),
  },
  {
    path: 'activation-compte',
    title: 'Activation du compte',
    loadComponent: () =>
      import('./features/account/activate-account-page/activate-account-page').then(
        (m) => m.ActivateAccountPage,
      ),
  },
  {
    path: 'mon-calendrier',
    title: 'Mon calendrier',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/calendar/calendar-page/calendar-page').then((m) => m.CalendarPage),
  },
  {
    path: '**',
    title: 'Page introuvable',
    loadComponent: () =>
      import('./features/not-found/not-found-page/not-found-page').then((m) => m.NotFoundPage),
  },
];
