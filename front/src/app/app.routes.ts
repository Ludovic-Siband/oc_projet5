import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/home/home.component').then((module) => module.HomeComponent),
    pathMatch: 'full',
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./layouts/auth-layout/auth-layout.component').then((module) => module.AuthLayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () => import('./pages/login/login.component').then((module) => module.LoginComponent),
      },
    ],
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./layouts/auth-layout/auth-layout.component').then((module) => module.AuthLayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./pages/register/register.component').then((module) => module.RegisterComponent),
      },
    ],
  },
  {
    path: 'feed',
    loadComponent: () =>
      import('./layouts/app-layout/app-layout.component').then((module) => module.AppLayoutComponent),
    canMatch: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./pages/feed/feed.component').then((module) => module.FeedComponent),
      },
    ],
  },
  {
    path: 'user',
    loadComponent: () =>
      import('./layouts/app-layout/app-layout.component').then((module) => module.AppLayoutComponent),
    canMatch: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./pages/user/user.component').then((module) => module.UserComponent),
      },
    ],
  },
];
