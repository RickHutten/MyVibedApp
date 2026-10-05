import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { DashboardLocationService } from '../shared/dashboard-location.service';
import { CurrentWeather } from './current-weather';

@Injectable({ providedIn: 'root' })
export class WeatherService {
  private readonly http = inject(HttpClient);
  private readonly location = inject(DashboardLocationService).location;

  getCurrentWeather() {
    return this.http.get<CurrentWeather>('/api/weather/current', {
      params: {
        latitude: this.location.latitude,
        longitude: this.location.longitude,
      },
    });
  }
}
