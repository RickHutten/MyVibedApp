import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { TestBed } from '@angular/core/testing';

import { routes } from '../app.routes';
import { Settings } from './settings';

describe('Settings', () => {
  it('offers schedule and commute navigation choices', async () => {
    await TestBed.configureTestingModule({
      imports: [Settings],
      providers: [provideRouter([])],
    }).compileComponents();

    const fixture = TestBed.createComponent(Settings);
    fixture.detectChanges();

    const links = Array.from(
      fixture.nativeElement.querySelectorAll('.settings-nav a'),
    ) as HTMLAnchorElement[];
    expect(links.map((link) => link.textContent?.trim())).toEqual([
      'Schedule settings',
      'Commute settings',
    ]);
    expect(links.map((link) => link.getAttribute('href'))).toEqual([
      '/settings/schedule',
      '/settings/commute',
    ]);
  });

  it('navigates back to the dashboard', async () => {
    await TestBed.configureTestingModule({
      imports: [Settings],
      providers: [provideRouter([])],
    }).compileComponents();

    const fixture = TestBed.createComponent(Settings);
    fixture.detectChanges();
    const router = TestBed.inject(Router);
    const backLink = fixture.nativeElement.querySelector('a.back-link') as HTMLAnchorElement;

    expect(backLink.getAttribute('href')).toBe('/');
    expect(router).toBeTruthy();
  });

  it('renders the selected commute settings view inside the shell', async () => {
    await TestBed.configureTestingModule({
      imports: [Settings],
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(Settings);
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/settings/commute');
    fixture.detectChanges();

    TestBed.inject(HttpTestingController)
      .expectOne('/api/commute/preferences')
      .flush({ wakeUpLeadMinutes: 45, transitAccessBufferMinutes: 5, officeArrivalLeadMinutes: 5 });
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Commute settings');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Morning commute');
    expect((fixture.nativeElement as HTMLElement).textContent).not.toContain(
      'Set the time you need before leaving for the office.',
    );
    expect(fixture.nativeElement.querySelector('.settings-nav a.selected')?.textContent).toContain(
      'Commute settings',
    );
    expect(fixture.nativeElement.querySelector('.shell-header h1')?.textContent?.trim()).toBe(
      'Personalise your dashboard',
    );
    expect(
      fixture.nativeElement.querySelector('.settings-content app-commute-preferences h1'),
    ).toBeNull();
    expect(fixture.nativeElement.querySelector('app-commute-preferences a.back-link')).toBeNull();
    expect(fixture.nativeElement.querySelector('app-commute-preferences .page-header')).toBeNull();
  });
});
