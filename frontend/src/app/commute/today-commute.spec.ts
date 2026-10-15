import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { TodayCommute } from './today-commute';

describe('TodayCommute', () => {
  afterEach(() => TestBed.inject(HttpTestingController).verify());

  async function createComponent() {
    await TestBed.configureTestingModule({
      imports: [TodayCommute],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(TodayCommute);
    fixture.detectChanges();
    return fixture;
  }

  it('renders timings and the complete itinerary with explicit locations', async () => {
    const fixture = await createComponent();
    TestBed.inject(HttpTestingController)
      .expectOne(
        (request) =>
          request.url === '/api/work-schedule/commute/today' &&
          request.params.get('latitude') === '52.3287776' &&
          request.params.get('longitude') === '4.9647344',
      )
      .flush({
        target: 'TODAY',
        status: 'AVAILABLE',
        date: '2026-10-07',
        wakeUpTime: '07:30:00',
        leaveHomeTime: '08:17:00',
        plannedArrivalTime: '08:55:00',
        destinationOffice: 'Amsterdam office',
        totalDurationMinutes: 38,
        transferCount: 1,
        segments: [
          {
            type: 'WALKING',
            departureTime: '08:17:00',
            arrivalTime: '08:25:00',
            from: 'Home',
            to: 'Amsterdam Zuid',
          },
          {
            type: 'PUBLIC_TRANSPORT',
            transferNumber: 0,
            mode: 'BUS',
            service: '34',
            direction: 'Central Station',
            departureTime: '08:25:00',
            arrivalTime: '08:45:00',
            from: 'Amsterdam Zuid',
            to: 'Rokin',
            platform: '3',
          },
          {
            type: 'WALKING',
            departureTime: '08:45:00',
            arrivalTime: '08:48:00',
            from: 'Rokin',
            to: 'Rokin tram stop',
          },
          {
            type: 'PUBLIC_TRANSPORT',
            transferNumber: 1,
            mode: 'TRAM',
            service: '2',
            direction: 'Museumplein',
            departureTime: '08:48:00',
            arrivalTime: '08:55:00',
            from: 'Rokin tram stop',
            to: 'Museumplein',
          },
          {
            type: 'WALKING',
            departureTime: '08:55:00',
            arrivalTime: '09:00:00',
            from: 'Museumplein',
            to: 'Amsterdam office',
          },
        ],
      });
    fixture.detectChanges();

    const content = fixture.nativeElement.textContent as string;
    expect(content).toContain("Today's commute");
    expect(content).toContain('Wake up07:30');
    expect(content).toContain('Leave home08:17');
    expect(content).toContain('Amsterdam office');
    expect(content).not.toContain('Arrive at Amsterdam office by 08:55');
    expect(fixture.nativeElement.querySelector('.commute-date')).toBeNull();
    expect(fixture.nativeElement.querySelector('.commute-destination')).toBeNull();
    expect(content).not.toContain('Journey');
    expect(content).not.toContain('Transfers');
    const segments = fixture.nativeElement.querySelectorAll('.segments > li');
    expect(segments.length).toBe(5);
    expect(segments[0].querySelector('.segment-mode')?.textContent).toContain('Walk');
    expect(segments[0].querySelector('.segment-route')?.textContent).toContain('Amsterdam Zuid');
    expect(segments[1].querySelector('.segment-mode')?.textContent).toContain('Bus 34');
    expect(segments[1].querySelector('.segment-route')?.textContent).toContain(
      'Amsterdam Zuid → Rokin',
    );
    expect(segments[1].querySelector('.segment-mode-row .segment-mode')?.textContent).toContain(
      'Bus 34',
    );
    expect(segments[1].querySelector('.segment-mode-row .segment-meta')?.textContent).toContain(
      'toward Central Station · platform 3',
    );
    expect(segments[2].querySelector('.segment-route')?.textContent).toContain(
      'Rokin → Rokin tram stop',
    );
    expect(segments[3].querySelector('.segment-mode')?.textContent).toContain('Tram 2');
    expect(segments[3].querySelector('.segment-meta')?.textContent).toContain('toward Museumplein');
    expect(segments[4].querySelector('.segment-route')?.textContent).toContain(
      'Museumplein → Amsterdam office',
    );
    expect(content).not.toContain('Board BUS');
    expect(content).not.toContain('get off at');
    expect(content).not.toContain('platform undefined');
    expect(
      fixture.nativeElement.querySelector('ol[aria-label="Complete commute itinerary"]'),
    ).not.toBeNull();
  });

  it('uses concise natural labels for walking and rail segments', async () => {
    const fixture = await createComponent();
    TestBed.inject(HttpTestingController)
      .expectOne((request) => request.url === '/api/work-schedule/commute/today')
      .flush({
        status: 'AVAILABLE',
        destinationOffice: 'Amsterdam Amstel',
        segments: [
          {
            type: 'WALKING',
            departureTime: '08:08:00',
            arrivalTime: '08:21:00',
            from: 'START',
            to: 'Amsterdam, Waterlooplein',
          },
          {
            type: 'PUBLIC_TRANSPORT',
            mode: 'REGIONAL_RAIL',
            service: '53',
            direction: 'Gaasperplas',
            departureTime: '08:21:00',
            arrivalTime: '08:34:00',
            from: 'Amsterdam, Waterlooplein',
            to: 'Amsterdam, Amstelstation',
            platform: '1',
          },
          {
            type: 'WALKING',
            departureTime: '08:34:00',
            arrivalTime: '08:38:00',
            from: 'Amsterdam, Amstelstation',
            to: 'END',
          },
        ],
      });
    fixture.detectChanges();

    const content = fixture.nativeElement.textContent as string;
    const segments = fixture.nativeElement.querySelectorAll('.segments > li');
    expect(segments[0].querySelector('.segment-route')?.textContent).toContain(
      'Amsterdam, Waterlooplein',
    );
    expect(segments[1].querySelector('.segment-mode')?.textContent).toContain('Train 53');
    expect(segments[1].querySelector('.segment-route')?.textContent).toContain(
      'Amsterdam, Waterlooplein → Amsterdam, Amstelstation',
    );
    expect(segments[1].querySelector('.segment-meta')?.textContent).toContain(
      'toward Gaasperplas · platform 1',
    );
    expect(segments[2].querySelector('.segment-route')?.textContent).toContain(
      'Amsterdam, Amstelstation → Amsterdam Amstel',
    );
    expect(content).not.toContain('get off at Amsterdam, Amstelstation at 08:34');
    expect(content).not.toContain('START');
    expect(content).not.toContain('END');
  });

  it('renders the exact unavailable state without route details', async () => {
    const fixture = await createComponent();
    TestBed.inject(HttpTestingController)
      .expectOne((request) => request.url === '/api/work-schedule/commute/today')
      .flush({ status: 'UNAVAILABLE', segments: [] });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No public transport route available');
  });

  it('renders the next office day target with its date and commute summary', async () => {
    const fixture = await createComponent();
    TestBed.inject(HttpTestingController)
      .expectOne((request) => request.url === '/api/work-schedule/commute/today')
      .flush({
        target: 'NEXT_OFFICE_DAY',
        status: 'AVAILABLE',
        date: '2026-10-09',
        wakeUpTime: '07:30:00',
        leaveHomeTime: '08:17:00',
        plannedArrivalTime: '08:55:00',
        destinationOffice: 'Amsterdam office',
        segments: [],
      });
    fixture.detectChanges();

    const content = fixture.nativeElement.textContent as string;
    expect(content).toContain('Next office day');
    expect(fixture.nativeElement.querySelector('.commute-date')).toBeNull();
    expect(fixture.nativeElement.querySelector('.commute-destination')).toBeNull();
    expect(content).not.toContain('Journey');
    expect(content).not.toContain('Transfers');
  });

  it('renders the no-upcoming-office-day state without commute details', async () => {
    const fixture = await createComponent();
    TestBed.inject(HttpTestingController)
      .expectOne((request) => request.url === '/api/work-schedule/commute/today')
      .flush({ target: 'NEXT_OFFICE_DAY', status: 'NO_UPCOMING_OFFICE_DAY', segments: [] });
    fixture.detectChanges();

    const content = fixture.nativeElement.textContent as string;
    expect(content).toContain('No upcoming office day scheduled');
    expect(content).not.toContain('Wake up');
    expect(content).not.toContain('No public transport route available');
  });

  it('omits optional transit details when the provider does not supply them', async () => {
    const fixture = await createComponent();
    TestBed.inject(HttpTestingController)
      .expectOne((request) => request.url === '/api/work-schedule/commute/today')
      .flush({
        status: 'AVAILABLE',
        plannedArrivalTime: '08:45:00',
        destinationOffice: 'Amsterdam office',
        segments: [
          {
            type: 'PUBLIC_TRANSPORT',
            departureTime: '08:25:00',
            arrivalTime: '08:45:00',
            direction: 'Central Station',
          },
        ],
      });
    fixture.detectChanges();

    const content = fixture.nativeElement.textContent as string;
    expect(content).toContain('Public transport');
    expect(content).toContain('toward Central Station');
    expect(content).not.toContain('undefined');
    expect(content).not.toContain('platform');
  });

  it('does not render the card when the commute is hidden', async () => {
    const fixture = await createComponent();
    TestBed.inject(HttpTestingController)
      .expectOne((request) => request.url === '/api/work-schedule/commute/today')
      .flush({ status: 'HIDDEN', segments: [] });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.commute-card')).toBeNull();
  });
});
