import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type TodayCommuteSegmentType = 'WALKING' | 'PUBLIC_TRANSPORT';

export interface TodayCommuteSegment {
  readonly type: TodayCommuteSegmentType;
  readonly transferNumber?: number;
  readonly mode?: string;
  readonly service?: string;
  readonly direction?: string;
  readonly departureTime: string;
  readonly arrivalTime: string;
  readonly from?: string;
  readonly to?: string;
  readonly platform?: string;
}

export type TodayCommuteStatus = 'HIDDEN' | 'UNAVAILABLE' | 'AVAILABLE';

export interface TodayCommute {
  readonly status: TodayCommuteStatus;
  readonly date?: string;
  readonly wakeUpTime?: string;
  readonly leaveHomeTime?: string;
  readonly plannedArrivalTime?: string;
  readonly destinationOffice?: string;
  readonly totalDurationMinutes?: number;
  readonly transferCount?: number;
  readonly segments: readonly TodayCommuteSegment[];
}

@Injectable({ providedIn: 'root' })
export class TodayCommuteService {
  private readonly http = inject(HttpClient);

  getTodayCommute(latitude: number, longitude: number): Observable<TodayCommute> {
    return this.http.get<TodayCommute>('/api/work-schedule/commute/today', {
      params: { latitude, longitude },
    });
  }
}
