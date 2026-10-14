import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { Dashboard } from './dashboard';

describe('Dashboard', () => {
  afterEach(() => {
    vi.useRealTimers();
    TestBed.inject(HttpTestingController).verify();
  });

  it('shows the clock and weather for the shared dashboard location', async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-10-05T13:07:00Z'));

    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(Dashboard);
    fixture.detectChanges();

    TestBed.inject(HttpTestingController)
      .expectOne(
        (request) =>
          request.url === '/api/weather/current' &&
          request.params.get('latitude') === '52.3676' &&
          request.params.get('longitude') === '4.9041',
      )
      .flush({
        temperatureC: 19.7,
        condition: 'Overcast',
        precipitationProbabilityPercent: 85,
        windSpeedKmh: 25.6,
        windDirection: 'W',
      });
    TestBed.inject(HttpTestingController)
      .expectOne('/api/work-schedule/upcoming')
      .flush({
        today: {
          date: '2026-10-05',
          status: 'WORK_FROM_HOME',
        },
        nextWorkingDay: null,
      });
    TestBed.inject(HttpTestingController)
      .expectOne((request) => request.url === '/api/work-schedule/commute/today')
      .flush({ status: 'HIDDEN', segments: [] });
    fixture.detectChanges();

    const content = fixture.nativeElement.textContent as string;
    expect(content).toContain('15:07');
    expect(content).toContain('Monday, 5 October');
    expect(content).toContain('Amsterdam');
    expect(content).toContain('19.7°C');

    const settingsButton = fixture.nativeElement.querySelector('.settings-button');
    expect(settingsButton.getAttribute('href')).toBe('/settings');
    expect(settingsButton.getAttribute('aria-label')).toBe('Settings');
    expect(settingsButton.textContent).toContain('⚙');
    expect(settingsButton.querySelector('.settings-label')?.textContent).toContain('Settings');
  });

  it('groups the schedule and commute in the dashboard rail', async () => {
    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(Dashboard);
    fixture.detectChanges();

    const http = TestBed.inject(HttpTestingController);
    http
      .expectOne((request) => request.url === '/api/weather/current')
      .flush({
        temperatureC: 19.7,
        condition: 'Overcast',
        precipitationProbabilityPercent: 85,
        windSpeedKmh: 25.6,
        windDirection: 'W',
      });
    http.expectOne('/api/work-schedule/upcoming').flush({
      today: { date: '2026-10-05', status: 'WORK_FROM_HOME' },
      nextWorkingDay: null,
    });
    http
      .expectOne((request) => request.url === '/api/work-schedule/commute/today')
      .flush({ status: 'HIDDEN', segments: [] });
    fixture.detectChanges();

    const dashboardSections = Array.from(
      fixture.nativeElement.querySelectorAll(
        'main > :not(.settings-button)',
      ) as NodeListOf<Element>,
    ).map((element) => element.className);

    expect(dashboardSections).toEqual(['dashboard-overview', 'dashboard-rail']);

    const railSections = Array.from(
      fixture.nativeElement.querySelectorAll('.dashboard-rail > *') as NodeListOf<Element>,
    ).map((element) => element.tagName.toLowerCase());

    expect(railSections).toEqual(['app-upcoming-work-schedule', 'app-today-commute']);
  });
});
