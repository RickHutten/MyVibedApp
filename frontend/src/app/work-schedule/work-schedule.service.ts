import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type ScheduleStatus = 'OFFICE' | 'WORK_FROM_HOME' | 'NON_WORKING';
export type RecurrenceLevel = 'DAYS' | 'WEEKS' | 'MONTHS';
export type MonthlyPatternType = 'CALENDAR_DAY' | 'WEEKDAY_OCCURRENCE';
export type MonthlyOccurrence = 'FIRST' | 'SECOND' | 'THIRD' | 'FOURTH' | 'LAST';

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

export interface MonthlyPattern {
  type: MonthlyPatternType;
  calendarDay: number | null;
  weekday: string | null;
  occurrence: MonthlyOccurrence | null;
}

export interface RecurringRule {
  id: string;
  level: RecurrenceLevel;
  interval: number;
  weekdays: string[];
  monthlyPattern: MonthlyPattern | null;
  startDate: string;
  endDate: string | null;
  status: ScheduleStatus;
  officeId: string | null;
}

export type RecurringRuleInput = Omit<RecurringRule, 'id'>;

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

  getRecurringRules(): Observable<RecurringRule[]> {
    return this.http.get<RecurringRule[]>('/api/work-schedule/recurring-rules');
  }

  addRecurringRule(rule: RecurringRuleInput): Observable<RecurringRule> {
    return this.http.post<RecurringRule>('/api/work-schedule/recurring-rules', rule);
  }

  editRecurringRule(id: string, rule: RecurringRuleInput): Observable<RecurringRule> {
    return this.http.put<RecurringRule>(`/api/work-schedule/recurring-rules/${id}`, rule);
  }

  deleteRecurringRule(id: string): Observable<void> {
    return this.http.delete<void>(`/api/work-schedule/recurring-rules/${id}`);
  }
}
