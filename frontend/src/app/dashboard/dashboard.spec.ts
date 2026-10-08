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
    fixture.detectChanges();

    const content = fixture.nativeElement.textContent as string;
    expect(content).toContain('15:07');
    expect(content).toContain('Monday, 5 October');
    expect(content).toContain('Amsterdam');
    expect(content).toContain('19.7°C');
  });
});
