import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type ScheduleStatus = 'OFFICE' | 'WORK_FROM_HOME' | 'NON_WORKING';

export interface WorkingHours {
  start: string;
  end: string;
}

export interface ScheduleDay {
  day: string;
  status: ScheduleStatus;
  officeId: string | null;
}

export interface SavedOffice {
  id: string;
  label: string;
  address: string;
  latitude: number;
  longitude: number;
  deleted: boolean;
}

export interface WorkSchedule {
  workingHours: WorkingHours;
  days: ScheduleDay[];
  offices: SavedOffice[];
}

export interface OfficeInput {
  label: string;
  address: string;
  latitude: number;
  longitude: number;
}

export interface AddressSuggestion {
  label: string;
  address: string;
  latitude: number;
  longitude: number;
}

@Injectable({ providedIn: 'root' })
export class WorkScheduleService {
  private readonly http = inject(HttpClient);

  getSchedule(): Observable<WorkSchedule> {
    return this.http.get<WorkSchedule>('/api/work-schedule');
  }

  replaceSchedule(schedule: Pick<WorkSchedule, 'workingHours' | 'days'>): Observable<WorkSchedule> {
    return this.http.put<WorkSchedule>('/api/work-schedule', schedule);
  }

  addOffice(office: OfficeInput): Observable<SavedOffice> {
    return this.http.post<SavedOffice>('/api/offices', office);
  }

  editOffice(id: string, office: OfficeInput): Observable<SavedOffice> {
    return this.http.put<SavedOffice>(`/api/offices/${id}`, office);
  }

  deleteOffice(id: string): Observable<void> {
    return this.http.delete<void>(`/api/offices/${id}`);
  }

  searchAddresses(query: string): Observable<AddressSuggestion[]> {
    return this.http.get<AddressSuggestion[]>('/api/offices/search', { params: { query } });
  }
}
