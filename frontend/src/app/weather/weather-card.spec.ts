import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import axe from 'axe-core';
import { provideHttpClient } from '@angular/common/http';
import { vi } from 'vitest';

import { WeatherCard } from './weather-card';

describe('WeatherCard', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WeatherCard],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    vi.useRealTimers();
    http.verify();
  });

  it('requests and displays the current weather for Amsterdam once', () => {
    vi.useFakeTimers();
    const fixture = TestBed.createComponent(WeatherCard);
    fixture.detectChanges();

    const request = http.expectOne(
      (candidate) =>
        candidate.url === '/api/weather/current' &&
        candidate.params.get('latitude') === '52.3676' &&
        candidate.params.get('longitude') === '4.9041',
    );
    request.flush({
      temperatureC: 19.7,
      condition: 'Overcast',
      precipitationProbabilityPercent: 85,
      windSpeedKmh: 25.6,
      windDirection: 'W',
    });
    fixture.detectChanges();

    const content = fixture.nativeElement.textContent as string;
    expect(content).toContain('Amsterdam');
    expect(content).toContain('19.7°C');
    expect(content).toContain('Overcast');
    expect(content).toContain('85%');
    expect(content).toContain('25.6 km/h');
    expect(content).toContain('W');
    vi.advanceTimersByTime(60 * 60 * 1000);
    http.expectNone((candidate) => candidate.url === '/api/weather/current');
  });

  it('shows that weather is unavailable when the request fails', () => {
    const fixture = TestBed.createComponent(WeatherCard);
    fixture.detectChanges();

    http
      .expectOne((candidate) => candidate.url === '/api/weather/current')
      .flush(null, {
        status: 503,
        statusText: 'Service Unavailable',
      });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Weather is unavailable');
  });

  it('attributes the weather provider', () => {
    const fixture = TestBed.createComponent(WeatherCard);
    fixture.detectChanges();

    http
      .expectOne((candidate) => candidate.url === '/api/weather/current')
      .flush({
        temperatureC: 19.7,
        condition: 'Overcast',
        precipitationProbabilityPercent: 85,
        windSpeedKmh: 25.6,
        windDirection: 'W',
      });
    fixture.detectChanges();

    const attribution = fixture.nativeElement.querySelector('a') as HTMLAnchorElement | null;
    expect(attribution?.textContent).toContain('Open-Meteo');
    expect(attribution?.href).toBe('https://open-meteo.com/');
  });

  it('has no detectable accessibility violations', async () => {
    const fixture = TestBed.createComponent(WeatherCard);
    fixture.detectChanges();

    http
      .expectOne((candidate) => candidate.url === '/api/weather/current')
      .flush({
        temperatureC: 19.7,
        condition: 'Overcast',
        precipitationProbabilityPercent: 85,
        windSpeedKmh: 25.6,
        windDirection: 'W',
      });
    fixture.detectChanges();

    const results = await axe.run(fixture.nativeElement, {
      rules: {
        'color-contrast': { enabled: false },
      },
    });
    expect(results.violations).toEqual([]);
  });
});
