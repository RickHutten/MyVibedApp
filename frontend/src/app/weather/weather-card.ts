import { Component, inject, OnInit, signal } from '@angular/core';

import { CurrentWeather } from './current-weather';
import { WeatherService } from './weather.service';

@Component({
  selector: 'app-weather-card',
  templateUrl: './weather-card.html',
  styleUrl: './weather-card.scss',
})
export class WeatherCard implements OnInit {
  private readonly weatherService = inject(WeatherService);

  protected readonly weather = signal<CurrentWeather | null>(null);
  protected readonly status = signal<'loading' | 'ready' | 'unavailable'>('loading');

  ngOnInit() {
    this.weatherService.getCurrentWeather().subscribe({
      next: (weather) => {
        this.weather.set(weather);
        this.status.set('ready');
      },
      error: () => this.status.set('unavailable'),
    });
  }
}
