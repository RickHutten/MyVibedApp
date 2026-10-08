import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { CommutePreferences } from './commute-preferences';

const defaults = {
  wakeUpLeadMinutes: 45,
  transitAccessBufferMinutes: 5,
  officeArrivalLeadMinutes: 5,
};

describe('CommutePreferences', () => {
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('loads the persisted values and does not offer a home-location setting', async () => {
    await TestBed.configureTestingModule({
      imports: [CommutePreferences],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(CommutePreferences);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/commute/preferences').flush(defaults);
    fixture.detectChanges();

    const wakeUpInput = fixture.nativeElement.querySelector(
      'input[name="wakeUpLeadMinutes"]',
    ) as HTMLInputElement;
    expect(wakeUpInput.value).toBe('45');
    const content = (fixture.nativeElement as HTMLElement).textContent;
    expect(content).toContain('First transit access buffer');
    expect(content).not.toContain('Home location');
  });

  it('saves all three timing values through the commute API', async () => {
    await TestBed.configureTestingModule({
      imports: [CommutePreferences],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(CommutePreferences);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/commute/preferences').flush(defaults);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.wakeUpLeadMinutes.set(30);
    component.transitAccessBufferMinutes.set(8);
    component.officeArrivalLeadMinutes.set(10);
    component.save();

    const request = TestBed.inject(HttpTestingController).expectOne('/api/commute/preferences');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      wakeUpLeadMinutes: 30,
      transitAccessBufferMinutes: 8,
      officeArrivalLeadMinutes: 10,
    });
    request.flush({
      wakeUpLeadMinutes: 30,
      transitAccessBufferMinutes: 8,
      officeArrivalLeadMinutes: 10,
    });
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Saved');
  });

  it('rejects negative and fractional timing values before sending them', async () => {
    await TestBed.configureTestingModule({
      imports: [CommutePreferences],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(CommutePreferences);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/commute/preferences').flush(defaults);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.wakeUpLeadMinutes.set(-1);
    component.save();
    expect(component.error()).toContain('whole minutes');
    expect(TestBed.inject(HttpTestingController).match('/api/commute/preferences')).toHaveLength(0);

    component.wakeUpLeadMinutes.set(5.5);
    component.save();
    expect(component.error()).toContain('whole minutes');
    expect(TestBed.inject(HttpTestingController).match('/api/commute/preferences')).toHaveLength(0);
  });
});
