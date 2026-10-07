import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, concat, defer, map, Observable, of, switchMap, timer } from 'rxjs';

import { DashboardLocationService } from '../shared/dashboard-location.service';
import { CurrentWeather } from './current-weather';

export const WEATHER_REFRESH_INTERVAL_MS = 10 * 60 * 1000;
export const WEATHER_RETRY_INITIAL_DELAY_MS = 1000;
export const WEATHER_RETRY_MAX_DELAY_MS = WEATHER_REFRESH_INTERVAL_MS;

export type WeatherRefreshEvent =
  | {
      type: 'success';
      weather: CurrentWeather;
      refreshedAt: Date;
    }
  | {
      type: 'failure';
    };

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

  watchCurrentWeather(): Observable<WeatherRefreshEvent> {
    return this.refreshCycle();
  }

  private refreshCycle(): Observable<WeatherRefreshEvent> {
    return concat(
      this.refreshUntilSuccessful(WEATHER_RETRY_INITIAL_DELAY_MS),
      timer(WEATHER_REFRESH_INTERVAL_MS).pipe(switchMap(() => this.refreshCycle())),
    );
  }

  private refreshUntilSuccessful(retryDelayMs: number): Observable<WeatherRefreshEvent> {
    return defer(() => this.getCurrentWeather()).pipe(
      map((weather): WeatherRefreshEvent => ({
        type: 'success',
        weather,
        refreshedAt: new Date(),
      })),
      catchError(() =>
        concat(
          of<WeatherRefreshEvent>({ type: 'failure' }),
          timer(retryDelayMs).pipe(
            switchMap(() =>
              this.refreshUntilSuccessful(Math.min(retryDelayMs * 2, WEATHER_RETRY_MAX_DELAY_MS)),
            ),
          ),
        ),
      ),
    );
  }
}
