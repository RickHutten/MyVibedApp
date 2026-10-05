import { Component, computed, DestroyRef, inject, signal } from '@angular/core';

import { DashboardLocationService } from '../shared/dashboard-location.service';

@Component({
  selector: 'app-clock',
  template: `
    <section aria-label="Current time and date">
      <time class="clock-time">{{ time() }}</time>
      <p class="clock-date">{{ date() }}</p>
    </section>
  `,
  styleUrl: './clock.scss',
})
export class Clock {
  private readonly destroyRef = inject(DestroyRef);
  private readonly location = inject(DashboardLocationService).location;
  private readonly currentInstant = signal(new Date());

  constructor() {
    const intervalId = setInterval(() => this.currentInstant.set(new Date()), 1_000);
    this.destroyRef.onDestroy(() => clearInterval(intervalId));
  }

  protected readonly time = computed(() =>
    new Intl.DateTimeFormat('en-GB', {
      hour: '2-digit',
      minute: '2-digit',
      hourCycle: 'h23',
      timeZone: this.location.timeZone,
    }).format(this.currentInstant()),
  );

  protected readonly date = computed(() => {
    const instant = this.currentInstant();
    const weekday = new Intl.DateTimeFormat('en-GB', {
      weekday: 'long',
      timeZone: this.location.timeZone,
    }).format(instant);
    const calendarDate = new Intl.DateTimeFormat('en-GB', {
      day: 'numeric',
      month: 'long',
      timeZone: this.location.timeZone,
    }).format(instant);

    return `${weekday}, ${calendarDate}`;
  });
}
