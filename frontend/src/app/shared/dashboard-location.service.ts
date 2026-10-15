import { Injectable } from '@angular/core';

export interface DashboardLocation {
  readonly name: string;
  readonly latitude: number;
  readonly longitude: number;
  readonly timeZone: string;
}

const AMSTERDAM: DashboardLocation = {
  name: 'Amsterdam',
  latitude: 52.3287776,
  longitude: 4.9647344,
  timeZone: 'Europe/Amsterdam',
};

@Injectable({ providedIn: 'root' })
export class DashboardLocationService {
  readonly location = AMSTERDAM;
}
