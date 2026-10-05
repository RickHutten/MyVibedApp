import { Component } from '@angular/core';

import { WeatherCard } from '../weather/weather-card';

@Component({
  selector: 'app-dashboard',
  imports: [WeatherCard],
  template: `
    <main>
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
      min-height: 100dvh;
      place-items: center;
      padding: clamp(1rem, 4vw, 3rem);
    }
  `,
})
export class Dashboard {}
