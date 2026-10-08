import { Component, inject, OnInit, signal } from '@angular/core';

import { DashboardLocationService } from '../shared/dashboard-location.service';
import {
  UpcomingScheduleStatus,
  UpcomingWorkDay,
  UpcomingWorkSchedule as UpcomingWorkScheduleData,
  UpcomingWorkScheduleService,
} from './upcoming-work-schedule.service';

@Component({
  selector: 'app-upcoming-work-schedule',
  templateUrl: './upcoming-work-schedule.html',
  styleUrl: './upcoming-work-schedule.scss',
})
export class UpcomingWorkSchedule implements OnInit {
  private readonly service = inject(UpcomingWorkScheduleService);
  protected readonly location = inject(DashboardLocationService).location;
  protected readonly schedule = signal<UpcomingWorkScheduleData | null>(null);
  protected readonly status = signal<'loading' | 'ready' | 'unavailable'>('loading');

  ngOnInit(): void {
    this.service.getUpcomingWorkSchedule().subscribe({
      next: (schedule) => {
        this.schedule.set(schedule);
        this.status.set('ready');
      },
      error: () => this.status.set('unavailable'),
    });
  }

  protected formatDate(date: string): string {
    const instant = new Date(`${date}T12:00:00Z`);
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
  }

  protected statusLabel(status: UpcomingScheduleStatus): string {
    switch (status) {
      case 'OFFICE':
        return 'Office';
      case 'WORK_FROM_HOME':
        return 'Work from home';
      case 'NON_WORKING':
        return 'Not a work day';
    }
  }

  protected locationLabel(day: UpcomingWorkDay): string {
    const status = this.statusLabel(day.status);
    return day.officeLabel === undefined ? status : `${status}: ${day.officeLabel}`;
  }
}
