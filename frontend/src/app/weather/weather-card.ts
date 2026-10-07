import { computed, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { DashboardLocationService } from '../shared/dashboard-location.service';
import { CurrentWeather } from './current-weather';
import { WeatherService } from './weather.service';

@Component({
  selector: 'app-weather-card',
  templateUrl: './weather-card.html',
  styleUrl: './weather-card.scss',
})
export class WeatherCard implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly weatherService = inject(WeatherService);

  protected readonly location = inject(DashboardLocationService).location;
  protected readonly weather = signal<CurrentWeather | null>(null);
  protected readonly status = signal<'loading' | 'ready' | 'stale' | 'unavailable'>('loading');
  protected readonly lastSuccessfulRefreshAt = signal<Date | null>(null);
  protected readonly lastSuccessfulRefreshLabel = computed(() => {
    const timestamp = this.lastSuccessfulRefreshAt();
    return timestamp === null
      ? null
      : new Intl.DateTimeFormat(undefined, {
          dateStyle: 'short',
          timeStyle: 'short',
        }).format(timestamp);
  });

  ngOnInit() {
    this.weatherService
      .watchCurrentWeather()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((event) => {
        if (event.type === 'failure') {
          this.status.set(this.weather() === null ? 'unavailable' : 'stale');
          return;
        }

        this.weather.set(event.weather);
        this.lastSuccessfulRefreshAt.set(event.refreshedAt);
        this.status.set('ready');
      });
  }
}
