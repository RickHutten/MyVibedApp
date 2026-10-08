import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { UpcomingWorkSchedule } from './upcoming-work-schedule';

describe('UpcomingWorkSchedule', () => {
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  async function createComponent() {
    await TestBed.configureTestingModule({
      imports: [UpcomingWorkSchedule],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(UpcomingWorkSchedule);
    fixture.detectChanges();
    return fixture;
  }

  it('renders today and the next working day with the resolved labels', async () => {
    const fixture = await createComponent();

    TestBed.inject(HttpTestingController)
      .expectOne('/api/work-schedule/upcoming')
      .flush({
        today: {
          date: '2026-10-07',
          status: 'OFFICE',
          officeLabel: 'Amsterdam office',
        },
        nextWorkingDay: {
          date: '2026-10-08',
          status: 'WORK_FROM_HOME',
        },
      });
    fixture.detectChanges();

    const content = fixture.nativeElement.textContent as string;
    expect(content).toContain('Today');
    expect(content).toContain('Wednesday, 7 October');
    expect(content).toContain('Office: Amsterdam office');
    expect(content).not.toContain('Work location');
    expect(content).toContain('Next working day');
    expect(content).toContain('Thursday, 8 October');
    expect(content).toContain('Work from home');
  });

  it('renders the empty state when no working day is scheduled', async () => {
    const fixture = await createComponent();

    TestBed.inject(HttpTestingController)
      .expectOne('/api/work-schedule/upcoming')
      .flush({
        today: { date: '2026-10-07', status: 'NON_WORKING' },
        nextWorkingDay: null,
      });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No upcoming work scheduled');
  });

  it('renders an unavailable state when the schedule request fails', async () => {
    const fixture = await createComponent();

    TestBed.inject(HttpTestingController)
      .expectOne('/api/work-schedule/upcoming')
      .flush('failed', { status: 503, statusText: 'Unavailable' });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Schedule is unavailable');
  });
});
