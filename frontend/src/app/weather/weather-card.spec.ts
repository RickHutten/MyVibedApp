import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import axe from 'axe-core';
import { provideHttpClient } from '@angular/common/http';
import { vi } from 'vitest';

import { DashboardLocationService } from '../shared/dashboard-location.service';
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

  it('refreshes the current weather every ten minutes', () => {
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

    vi.advanceTimersByTime(10 * 60 * 1000 - 1);
    http.expectNone((candidate) => candidate.url === '/api/weather/current');

    vi.advanceTimersByTime(1);
    const refresh = http.expectOne((candidate) => candidate.url === '/api/weather/current');
    refresh.flush({
      temperatureC: 20.1,
      condition: 'Sunny',
      precipitationProbabilityPercent: 5,
      windSpeedKmh: 12.3,
      windDirection: 'E',
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('20.1°C');
    expect(fixture.nativeElement.textContent).toContain('Sunny');
  });

  it('keeps the last weather and shows its refresh time when a refresh fails', () => {
    vi.useFakeTimers();
    const firstRefreshAt = new Date('2026-01-15T10:00:00.000Z');
    vi.setSystemTime(firstRefreshAt);
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

    vi.advanceTimersByTime(10 * 60 * 1000);
    http
      .expectOne((candidate) => candidate.url === '/api/weather/current')
      .flush(null, { status: 503, statusText: 'Service Unavailable' });
    fixture.detectChanges();

    const lastRefresh = new Intl.DateTimeFormat(undefined, {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(firstRefreshAt);
    const staleContent = fixture.nativeElement.textContent as string;
    expect(staleContent).toContain('19.7°C');
    expect(staleContent).toContain(`Weather may be outdated · Last updated ${lastRefresh}`);

    vi.advanceTimersByTime(1000);
    const retry = http.expectOne((candidate) => candidate.url === '/api/weather/current');
    retry.flush({
      temperatureC: 20.1,
      condition: 'Sunny',
      precipitationProbabilityPercent: 5,
      windSpeedKmh: 12.3,
      windDirection: 'E',
    });
    fixture.detectChanges();

    const refreshedContent = fixture.nativeElement.textContent as string;
    expect(refreshedContent).toContain('20.1°C');
    expect(refreshedContent).not.toContain('Weather may be outdated');

    vi.advanceTimersByTime(10 * 60 * 1000 - 1);
    http.expectNone((candidate) => candidate.url === '/api/weather/current');
    vi.advanceTimersByTime(1);
    http
      .expectOne((candidate) => candidate.url === '/api/weather/current')
      .flush({
        temperatureC: 20.1,
        condition: 'Sunny',
        precipitationProbabilityPercent: 5,
        windSpeedKmh: 12.3,
        windDirection: 'E',
      });
  });

  it('retries failures with doubling delays capped at ten minutes', () => {
    vi.useFakeTimers();
    const fixture = TestBed.createComponent(WeatherCard);
    fixture.detectChanges();

    http
      .expectOne((candidate) => candidate.url === '/api/weather/current')
      .flush(null, { status: 503, statusText: 'Service Unavailable' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Weather is unavailable');

    const failRetry = (delayMs: number) => {
      vi.advanceTimersByTime(delayMs - 1);
      http.expectNone((candidate) => candidate.url === '/api/weather/current');
      vi.advanceTimersByTime(1);
      http
        .expectOne((candidate) => candidate.url === '/api/weather/current')
        .flush(null, { status: 503, statusText: 'Service Unavailable' });
    };

    failRetry(1000);
    failRetry(2000);
    failRetry(4000);
    failRetry(8000);
    failRetry(16000);
    failRetry(32000);
    failRetry(64000);
    failRetry(128000);
    failRetry(256000);
    failRetry(512000);
    failRetry(600000);

    vi.advanceTimersByTime(600000 - 1);
    http.expectNone((candidate) => candidate.url === '/api/weather/current');
    vi.advanceTimersByTime(1);
    http
      .expectOne((candidate) => candidate.url === '/api/weather/current')
      .flush({
        temperatureC: 20.1,
        condition: 'Sunny',
        precipitationProbabilityPercent: 5,
        windSpeedKmh: 12.3,
        windDirection: 'E',
      });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('20.1°C');
    expect(fixture.nativeElement.textContent).not.toContain('Weather may be outdated');
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

describe('WeatherCard shared location', () => {
  it('uses the shared dashboard location for the heading and weather request', async () => {
    await TestBed.configureTestingModule({
      imports: [WeatherCard],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: DashboardLocationService,
          useValue: {
            location: {
              name: 'Rotterdam',
              latitude: 51.9244,
              longitude: 4.4777,
              timeZone: 'Europe/Amsterdam',
            },
          },
        },
      ],
    }).compileComponents();
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(WeatherCard);
    fixture.detectChanges();

    const request = http.expectOne(
      (candidate) =>
        candidate.url === '/api/weather/current' &&
        candidate.params.get('latitude') === '51.9244' &&
        candidate.params.get('longitude') === '4.4777',
    );
    request.flush({
      temperatureC: 19.7,
      condition: 'Overcast',
      precipitationProbabilityPercent: 85,
      windSpeedKmh: 25.6,
      windDirection: 'W',
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Rotterdam');
    http.verify();
  });
});
