import { Component } from '@angular/core';

import { Clock } from '../clock/clock';
import { WeatherCard } from '../weather/weather-card';

@Component({
  selector: 'app-dashboard',
  imports: [Clock, WeatherCard],
  template: `
    <main>
      <app-clock />
      <app-weather-card />
    </main>
  `,
  styles: `
    :host {
      display: block;
      min-height: 100dvh;
    }

    main {
      display: grid;
      grid-template-columns: minmax(0, 1fr) minmax(20rem, 34rem);
      gap: clamp(2rem, 6vw, 6rem);
      align-items: center;
      width: min(100%, 75rem);
      min-height: 100dvh;
      margin: 0 auto;
      padding: clamp(1rem, 4vw, 3rem);
    }

    @media (max-width: 54rem) {
      main {
        grid-template-columns: 1fr;
        align-content: center;
      }
    }
  `,
})
export class Dashboard {}
