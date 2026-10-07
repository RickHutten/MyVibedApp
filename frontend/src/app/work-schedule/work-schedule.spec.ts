import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { WorkSchedule } from './work-schedule';

const schedule = {
  workingHours: { start: '09:00', end: '17:00' },
  days: [
    { day: 'MONDAY', status: 'NON_WORKING' as const, officeId: null },
    { day: 'TUESDAY', status: 'NON_WORKING' as const, officeId: null },
    { day: 'WEDNESDAY', status: 'NON_WORKING' as const, officeId: null },
    { day: 'THURSDAY', status: 'NON_WORKING' as const, officeId: null },
    { day: 'FRIDAY', status: 'NON_WORKING' as const, officeId: null },
    { day: 'SATURDAY', status: 'NON_WORKING' as const, officeId: null },
    { day: 'SUNDAY', status: 'NON_WORKING' as const, officeId: null },
  ],
  offices: [],
};

describe('WorkSchedule', () => {
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('loads the backend schedule and renders all seven days', async () => {
    await TestBed.configureTestingModule({
      imports: [WorkSchedule],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(WorkSchedule);
    fixture.detectChanges();

    TestBed.inject(HttpTestingController).expectOne('/api/work-schedule').flush(schedule);
    TestBed.inject(HttpTestingController).expectOne('/api/work-schedule/recurring-rules').flush([]);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Monday');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Sunday');
    const mondayStatus = fixture.nativeElement.querySelector('#MONDAY-status') as HTMLSelectElement;
    expect(mondayStatus.value).toBe('NON_WORKING');
  });

  it('does not submit an office day until an office is selected', async () => {
    await TestBed.configureTestingModule({
      imports: [WorkSchedule],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(WorkSchedule);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/work-schedule').flush(schedule);
    TestBed.inject(HttpTestingController).expectOne('/api/work-schedule/recurring-rules').flush([]);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.setDayStatus('MONDAY', 'OFFICE');
    component.saveSchedule();

    expect(component.error()).toBe('Monday needs a saved office.');
    expect(TestBed.inject(HttpTestingController).match('/api/work-schedule')).toHaveLength(0);
  });

  it('does not save an office until an address suggestion is selected', async () => {
    await TestBed.configureTestingModule({
      imports: [WorkSchedule],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(WorkSchedule);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/work-schedule').flush(schedule);
    TestBed.inject(HttpTestingController).expectOne('/api/work-schedule/recurring-rules').flush([]);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.officeDraft.set({
      label: 'Office',
      address: 'Typed address',
      latitude: 0,
      longitude: 0,
    });
    component.saveOffice();

    expect(component.officeError()).toBe('Choose a label and an address suggestion.');
    expect(TestBed.inject(HttpTestingController).match('/api/offices')).toHaveLength(0);
  });

  it('loads recurring rules and shows their active range', async () => {
    await TestBed.configureTestingModule({
      imports: [WorkSchedule],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(WorkSchedule);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController)
      .expectOne('/api/work-schedule')
      .flush({
        ...schedule,
        offices: [
          {
            id: 'office-1',
            label: 'Amsterdam office',
            address: 'Damrak 1',
            latitude: 52.37,
            longitude: 4.89,
            deleted: false,
          },
        ],
      });
    TestBed.inject(HttpTestingController)
      .expectOne('/api/work-schedule/recurring-rules')
      .flush([
        {
          id: 'rule-1',
          level: 'WEEKS',
          interval: 2,
          weekdays: ['MONDAY', 'WEDNESDAY'],
          monthlyPattern: null,
          startDate: '2026-01-01',
          endDate: '2026-03-31',
          status: 'OFFICE',
          officeId: 'office-1',
        },
        {
          id: 'rule-2',
          level: 'MONTHS',
          interval: 1,
          weekdays: [],
          monthlyPattern: {
            type: 'WEEKDAY_OCCURRENCE',
            calendarDay: null,
            weekday: 'FRIDAY',
            occurrence: 'FIRST',
          },
          startDate: '2026-01-01',
          endDate: null,
          status: 'OFFICE',
          officeId: 'office-1',
        },
      ]);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Every 2 weeks · Office');
    expect(text).toContain('At Amsterdam office');
    expect(text).toContain('First Friday of the month');
    expect(text).toContain('2026-01-01 – 2026-03-31');
  });

  it('presents recurrence as Every interval unit', async () => {
    await TestBed.configureTestingModule({
      imports: [WorkSchedule],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(WorkSchedule);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/work-schedule').flush(schedule);
    TestBed.inject(HttpTestingController).expectOne('/api/work-schedule/recurring-rules').flush([]);

    const component = fixture.componentInstance;
    component.setRuleField('interval', 2);
    component.setRuleLevel('WEEKS');
    fixture.detectChanges();

    const interval = fixture.nativeElement.querySelector('#rule-interval') as HTMLInputElement;
    const level = fixture.nativeElement.querySelector('#rule-level') as HTMLSelectElement;
    expect(interval.type).toBe('number');
    expect(interval.value).toBe('2');
    expect(level.value).toBe('WEEKS');
    expect(Array.from(level.options).map((option) => option.textContent?.trim())).toEqual([
      'days',
      'weeks',
      'months',
    ]);

    const location = fixture.nativeElement.querySelector(
      'select[name="rule-location"]',
    ) as HTMLSelectElement;
    expect(location.value).toBe('WORK_FROM_HOME');
    component.offices.set([
      {
        id: 'office-1',
        label: 'Amsterdam office',
        address: 'Damrak 1',
        latitude: 52.37,
        longitude: 4.89,
        deleted: false,
      },
    ]);
    component.setRuleStatus('OFFICE');
    fixture.detectChanges();

    const office = fixture.nativeElement.querySelector(
      'select[name="rule-office"]',
    ) as HTMLSelectElement | null;
    expect(office).not.toBeNull();
    expect(office?.options[1]?.textContent?.trim()).toBe('Amsterdam office');

    component.setRuleLevel('MONTHS');
    component.setMonthlyPatternField('type', 'WEEKDAY_OCCURRENCE');
    fixture.detectChanges();
    const occurrence = fixture.nativeElement.querySelector(
      'select[name="rule-occurrence"]',
    ) as HTMLSelectElement;
    expect(Array.from(occurrence.options).map((option) => option.textContent?.trim())).toEqual([
      'First',
      'Second',
      'Third',
      'Fourth',
      'Last',
    ]);
  });

  it('requires a weekday before saving a weekly recurring rule', async () => {
    await TestBed.configureTestingModule({
      imports: [WorkSchedule],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(WorkSchedule);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/work-schedule').flush(schedule);
    TestBed.inject(HttpTestingController).expectOne('/api/work-schedule/recurring-rules').flush([]);

    const component = fixture.componentInstance;
    component.setRuleLevel('WEEKS');
    component.saveRecurringRule();

    expect(component.ruleError()).toBe('Choose at least one weekday.');
    expect(
      TestBed.inject(HttpTestingController).match('/api/work-schedule/recurring-rules'),
    ).toHaveLength(0);
  });
});
