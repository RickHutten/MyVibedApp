import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface CommutePreferencesValues {
  wakeUpLeadMinutes: number;
  transitAccessBufferMinutes: number;
  officeArrivalLeadMinutes: number;
}

@Injectable({ providedIn: 'root' })
export class CommutePreferencesService {
  private readonly http = inject(HttpClient);

  getPreferences(): Observable<CommutePreferencesValues> {
    return this.http.get<CommutePreferencesValues>('/api/commute/preferences');
  }

  replacePreferences(preferences: CommutePreferencesValues): Observable<CommutePreferencesValues> {
    return this.http.put<CommutePreferencesValues>('/api/commute/preferences', preferences);
  }
}
