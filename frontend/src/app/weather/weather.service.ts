import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { CurrentWeather } from './current-weather';

const AMSTERDAM = {
  latitude: 52.3676,
  longitude: 4.9041,
} as const;

@Injectable({ providedIn: 'root' })
export class WeatherService {
  private readonly http = inject(HttpClient);

  getCurrentWeather() {
    return this.http.get<CurrentWeather>('/api/weather/current', {
      params: AMSTERDAM,
    });
  }
}
