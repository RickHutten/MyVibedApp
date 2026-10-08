import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./dashboard/dashboard').then((module) => module.Dashboard),
  },
  {
    path: 'settings',
    loadComponent: () => import('./settings/settings').then((module) => module.Settings),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'schedule' },
      {
        path: 'schedule',
        loadComponent: () =>
          import('./work-schedule/work-schedule').then((module) => module.WorkSchedule),
      },
      {
        path: 'commute',
        loadComponent: () =>
          import('./commute/commute-preferences').then((module) => module.CommutePreferences),
      },
    ],
  },
];
