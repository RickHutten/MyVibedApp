import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type UpcomingScheduleStatus = 'OFFICE' | 'WORK_FROM_HOME' | 'NON_WORKING';

export interface UpcomingWorkDay {
  readonly date: string;
  readonly status: UpcomingScheduleStatus;
  readonly officeLabel?: string;
}

export interface UpcomingWorkSchedule {
  readonly today: UpcomingWorkDay;
  readonly nextWorkingDay: UpcomingWorkDay | null;
}

@Injectable({ providedIn: 'root' })
export class UpcomingWorkScheduleService {
  private readonly http = inject(HttpClient);

  getUpcomingWorkSchedule(): Observable<UpcomingWorkSchedule> {
    return this.http.get<UpcomingWorkSchedule>('/api/work-schedule/upcoming');
  }
}
