import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/home/home.component').then((module) => module.HomeComponent),
    pathMatch: 'full',
  },
  {
    path: '',
    loadComponent: () =>
      import('./layouts/app-layout/app-layout.component').then((module) => module.AppLayoutComponent),
    children: [
      {
        path: 'login',
        loadComponent: () => import('./pages/login/login.component').then((module) => module.LoginComponent),
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./pages/register/register.component').then((module) => module.RegisterComponent),
      },
    ],
  },
];
