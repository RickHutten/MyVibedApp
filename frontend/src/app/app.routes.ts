import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./dashboard/dashboard').then((module) => module.Dashboard),
  },
  {
    path: 'settings/schedule',
    loadComponent: () =>
      import('./work-schedule/work-schedule').then((module) => module.WorkSchedule),
  },
];
