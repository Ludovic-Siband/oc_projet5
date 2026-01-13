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
        path: 'register',
        loadComponent: () =>
          import('./pages/register/register.component').then((module) => module.RegisterComponent),
      },
    ],
  },
];
