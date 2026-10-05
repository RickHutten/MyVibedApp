import { TestBed } from '@angular/core/testing';
import axe from 'axe-core';
import { vi } from 'vitest';

import { DashboardLocationService } from '../shared/dashboard-location.service';
import { Clock } from './clock';

describe('Clock', () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it('shows the current time and date in the dashboard location timezone', async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-10-05T13:07:00Z'));

    await TestBed.configureTestingModule({
      imports: [Clock],
    }).compileComponents();

    const fixture = TestBed.createComponent(Clock);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.clock-time')?.textContent.trim()).toBe('15:07');
    expect(fixture.nativeElement.querySelector('.clock-date')?.textContent.trim()).toBe(
      'Monday, 5 October',
    );
  });

  it('updates the time and date across midnight without reloading', async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-10-05T21:59:30Z'));

    await TestBed.configureTestingModule({
      imports: [Clock],
    }).compileComponents();

    const fixture = TestBed.createComponent(Clock);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.clock-time')?.textContent.trim()).toBe('23:59');
    expect(fixture.nativeElement.querySelector('.clock-date')?.textContent.trim()).toBe(
      'Monday, 5 October',
    );

    vi.advanceTimersByTime(60_000);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.clock-time')?.textContent.trim()).toBe('00:00');
    expect(fixture.nativeElement.querySelector('.clock-date')?.textContent.trim()).toBe(
      'Tuesday, 6 October',
    );
  });

  it('uses the timezone supplied by the shared dashboard location', async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-10-05T13:07:00Z'));

    await TestBed.configureTestingModule({
      imports: [Clock],
      providers: [
        {
          provide: DashboardLocationService,
          useValue: {
            location: {
              name: 'UTC test location',
              latitude: 0,
              longitude: 0,
              timeZone: 'UTC',
            },
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(Clock);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.clock-time')?.textContent.trim()).toBe('13:07');
  });

  it('has no detectable accessibility violations', async () => {
    await TestBed.configureTestingModule({
      imports: [Clock],
    }).compileComponents();

    const fixture = TestBed.createComponent(Clock);
    fixture.detectChanges();

    const results = await axe.run(fixture.nativeElement, {
      rules: {
        'color-contrast': { enabled: false },
      },
    });
    expect(results.violations).toEqual([]);
  });
});
