import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth-guard';
import { roleGuard } from './core/auth/role-guard';

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
    canActivate: [roleGuard('ADMIN')],
    loadComponent: () =>
      import('./features/clubs/clubs-list-page/clubs-list-page').then((m) => m.ClubsListPage),
  },
  {
    path: 'clubs/nouveau',
    title: 'Ajouter un club',
    canActivate: [roleGuard('ADMIN')],
    loadComponent: () =>
      import('./features/clubs/club-form-page/club-form-page').then((m) => m.ClubFormPage),
  },
  {
    path: 'clubs/:id/modifier',
    title: 'Modifier un club',
    canActivate: [roleGuard('ADMIN')],
    loadComponent: () =>
      import('./features/clubs/club-form-page/club-form-page').then((m) => m.ClubFormPage),
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
    path: 'confirmation-compte',
    title: 'Confirmation du compte',
    loadComponent: () =>
      import('./features/account/confirm-account-creation-page/confirm-account-creation-page').then(
        (m) => m.ConfirmAccountCreationPage,
      ),
  },
  {
    path: 'mon-compte',
    title: 'Modifier mon compte',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/account/my-account-page/my-account-page').then((m) => m.MyAccountPage),
  },
  {
    path: 'admin/comptes',
    title: 'Gestion des comptes utilisateurs',
    canActivate: [roleGuard('ADMIN')],
    loadComponent: () =>
      import('./features/account/users-list-page/users-list-page').then((m) => m.UsersListPage),
  },
  {
    path: '**',
    title: 'Page introuvable',
    loadComponent: () =>
      import('./features/not-found/not-found-page/not-found-page').then((m) => m.NotFoundPage),
  },
];
