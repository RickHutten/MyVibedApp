import { Component } from '@angular/core';

import { Clock } from '../clock/clock';
import { WeatherCard } from '../weather/weather-card';
import { UpcomingWorkSchedule } from '../work-schedule/upcoming-work-schedule';

@Component({
  selector: 'app-dashboard',
  imports: [Clock, WeatherCard, UpcomingWorkSchedule],
  template: `
    <main>
      <a class="settings-link" href="/settings">Settings</a>
      <app-clock />
      <app-weather-card />
      <app-upcoming-work-schedule />
    </main>
  `,
  styles: `
    :host {
      display: block;
      height: 100dvh;
      overflow: hidden;
    }

    main {
      position: relative;
      display: grid;
      grid-template-columns: minmax(0, 1fr) minmax(18rem, 30rem);
      grid-template-rows: auto auto;
      column-gap: clamp(1rem, 3vw, 2.5rem);
      row-gap: clamp(0.75rem, 2vw, 1.25rem);
      align-items: center;
      align-content: center;
      width: min(100%, 75rem);
      height: 100%;
      min-height: 0;
      margin: 0 auto;
      padding: clamp(0.75rem, 3vw, 2rem);
      overflow: hidden;
    }

    .settings-link {
      position: absolute;
      top: 1rem;
      right: 1rem;
      color: #53627a;
      font-size: 0.85rem;
      text-decoration: none;
    }

    .settings-link:hover,
    .settings-link:focus-visible {
      color: #167c80;
      text-decoration: underline;
    }

    app-upcoming-work-schedule {
      grid-column: 2;
      grid-row: 1 / -1;
      align-self: center;
    }

    app-clock {
      grid-column: 1;
      grid-row: 1;
      align-self: end;
      justify-self: center;
      text-align: center;
    }

    app-weather-card {
      grid-column: 1;
      grid-row: 2;
      align-self: start;
      justify-self: center;
    }

    @media (max-width: 54rem) {
      main {
        grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
        grid-template-rows: auto minmax(0, 1fr);
        gap: clamp(0.5rem, 2vw, 1rem);
        align-content: center;
      }

      app-clock {
        grid-column: 1 / -1;
        grid-row: 1;
        align-self: center;
        justify-self: center;
      }

      app-weather-card {
        grid-column: 1;
        grid-row: 2;
        align-self: center;
        justify-self: center;
      }

      app-upcoming-work-schedule {
        grid-column: 2;
        grid-row: 2;
        align-self: center;
      }
    }

    @media (max-width: 36rem) {
      main {
        grid-template-columns: 1fr;
        grid-template-rows: auto auto auto;
      }

      app-clock {
        grid-column: 1;
        grid-row: 1;
        justify-self: center;
      }

      app-weather-card {
        grid-column: 1;
        grid-row: 2;
        justify-self: center;
      }

      app-upcoming-work-schedule {
        grid-column: 1;
        grid-row: 3;
      }
    }
  `,
})
export class Dashboard {}
