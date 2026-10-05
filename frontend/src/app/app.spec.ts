import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { App } from './app';
import { routes } from './app.routes';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('loads the dashboard route', async () => {
    const fixture = TestBed.createComponent(App);
    const router = TestBed.inject(Router);
    await fixture.whenStable();
    await router.navigateByUrl('/');
    fixture.detectChanges();

    TestBed.inject(HttpTestingController)
      .expectOne((request) => request.url === '/api/weather/current')
      .flush({
        temperatureC: 19.7,
        condition: 'Overcast',
        precipitationProbabilityPercent: 85,
        windSpeedKmh: 25.6,
        windDirection: 'W',
      });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Amsterdam');
  });
});
