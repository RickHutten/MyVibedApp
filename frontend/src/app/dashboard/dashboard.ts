import { Component } from '@angular/core';

import { Clock } from '../clock/clock';
import { TodayCommute } from '../commute/today-commute';
import { WeatherCard } from '../weather/weather-card';
import { UpcomingWorkSchedule } from '../work-schedule/upcoming-work-schedule';

@Component({
  selector: 'app-dashboard',
  imports: [Clock, TodayCommute, WeatherCard, UpcomingWorkSchedule],
  template: `
    <main>
      <a class="settings-button" href="/settings" aria-label="Settings" title="Settings">
        <span class="settings-icon" aria-hidden="true">⚙</span>
        <span class="settings-label">Settings</span>
      </a>
      <section class="dashboard-overview" aria-label="Current overview">
        <app-clock />
        <app-weather-card />
      </section>
      <aside class="dashboard-rail" aria-label="Schedule and commute">
        <app-upcoming-work-schedule />
        <app-today-commute />
      </aside>
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
      grid-template-rows: minmax(0, 1fr);
      column-gap: clamp(1rem, 3vw, 2.5rem);
      width: 100%;
      height: 100%;
      max-height: 100dvh;
      margin: 0 auto;
      padding: clamp(0.75rem, 3vw, 2rem);
      overflow: hidden;
    }

    .dashboard-overview {
      display: grid;
      grid-template-rows: auto auto;
      align-content: center;
      align-items: center;
      gap: clamp(0.75rem, 2vw, 1.25rem);
      min-width: 0;
      min-height: 0;
    }

    .dashboard-rail {
      display: flex;
      flex-direction: column;
      align-items: stretch;
      gap: clamp(0.75rem, 2vw, 1.25rem);
      min-width: 0;
      min-height: 0;
    }

    .settings-button {
      position: fixed;
      z-index: 10;
      bottom: 0.75rem;
      left: 0.75rem;
      display: flex;
      align-items: flex-start;
      justify-content: flex-start;
      box-sizing: border-box;
      width: 2.5rem;
      height: 2.5rem;
      gap: 0;
      overflow: hidden;
      border: 1px solid #dce3ea;
      border-radius: 1.25rem;
      background: rgb(255 255 255 / 90%);
      color: #53627a;
      font-size: 1rem;
      line-height: 1;
      text-decoration: none;
      transition:
        width 250ms ease,
        color 250ms ease;
    }

    .settings-icon {
      display: grid;
      width: 2rem;
      height: 2rem;
      flex: 0 0 2.3rem;
      place-items: center;
      font-size: 2rem;
      line-height: 1;
    }

    .settings-label {
      max-width: 0;
      position: absolute;
      top: 50%;
      left: 2.25rem;
      overflow: hidden;
      opacity: 0;
      white-space: nowrap;
      line-height: 1.25;
      margin: 0;
      transform: translateY(-50%);
      transition:
        max-width 160ms ease,
        opacity 100ms ease;
    }

    .settings-button:hover,
    .settings-button:focus-visible {
      width: 7rem;
      border-radius: 1.25rem;
      color: #167c80;
      border-color: #167c80;
    }

    .settings-button:hover .settings-label,
    .settings-button:focus-visible .settings-label {
      max-width: 6rem;
      opacity: 1;
    }

    app-upcoming-work-schedule {
      flex: 0 0 auto;
      width: 100%;
    }

    app-today-commute {
      display: block;
      flex: 0 1 auto;
      min-width: 0;
      min-height: 0;
    }

    app-clock,
    app-weather-card {
      min-width: 0;
      justify-self: center;
    }

    app-clock {
      justify-self: center;
      text-align: center;
    }

    app-weather-card {
      justify-self: center;
    }

    @media (max-width: 54rem) {
      main {
        grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
        gap: clamp(0.5rem, 2vw, 1rem);
      }
    }

    @media (max-width: 36rem) {
      main {
        grid-template-columns: 1fr;
        grid-template-rows: auto minmax(0, 1fr);
        row-gap: clamp(0.5rem, 2vw, 1rem);
      }

      .dashboard-overview {
        grid-column: 1;
        grid-row: 1;
        align-content: start;
        overflow: hidden;
      }

      .dashboard-rail {
        grid-column: 1;
        grid-row: 2;
      }

      app-clock,
      app-weather-card {
        justify-self: stretch;
      }
    }
  `,
})
export class Dashboard {}
