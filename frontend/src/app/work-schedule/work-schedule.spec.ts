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
});
