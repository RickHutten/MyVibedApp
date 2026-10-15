import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';

import { CommutePreferencesService, CommutePreferencesValues } from './commute-preferences.service';

@Component({
  selector: 'app-commute-preferences',
  templateUrl: './commute-preferences.html',
  styleUrl: './commute-preferences.scss',
})
export class CommutePreferences implements OnInit {
  readonly wakeUpLeadMinutes = signal(45);
  readonly transitAccessBufferMinutes = signal(5);
  readonly officeArrivalLeadMinutes = signal(5);
  readonly visibilityCutoff = signal('12:00');
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly saved = signal(false);
  readonly error = signal<string | null>(null);
  readonly hasLoaded = signal(false);

  private readonly service = inject(CommutePreferencesService);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.getPreferences().subscribe({
      next: (preferences) => {
        this.setValues(preferences);
        this.loading.set(false);
        this.hasLoaded.set(true);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(
          error.status === 0
            ? 'Commute preferences are unavailable right now.'
            : 'Could not load commute preferences.',
        );
      },
    });
  }

  save(): void {
    this.saved.set(false);
    this.error.set(null);
    const preferences = this.values();
    if (!this.isValid(preferences)) {
      this.error.set('Use non-negative whole minutes for each preference.');
      return;
    }

    this.saving.set(true);
    this.service.replacePreferences(preferences).subscribe({
      next: (savedPreferences) => {
        this.setValues(savedPreferences);
        this.saving.set(false);
        this.saved.set(true);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.error.set(
          error.status === 0
            ? 'Commute preferences are unavailable right now.'
            : 'Could not save commute preferences.',
        );
      },
    });
  }

  setWakeUpLeadMinutes(event: Event): void {
    this.wakeUpLeadMinutes.set(this.numberFromInput(event));
  }

  setTransitAccessBufferMinutes(event: Event): void {
    this.transitAccessBufferMinutes.set(this.numberFromInput(event));
  }

  setOfficeArrivalLeadMinutes(event: Event): void {
    this.officeArrivalLeadMinutes.set(this.numberFromInput(event));
  }

  setVisibilityCutoff(event: Event): void {
    this.visibilityCutoff.set((event.target as HTMLInputElement).value);
  }

  private values(): CommutePreferencesValues {
    return {
      wakeUpLeadMinutes: this.wakeUpLeadMinutes(),
      transitAccessBufferMinutes: this.transitAccessBufferMinutes(),
      officeArrivalLeadMinutes: this.officeArrivalLeadMinutes(),
      visibilityCutoff: this.visibilityCutoff(),
    };
  }

  private setValues(preferences: CommutePreferencesValues): void {
    this.wakeUpLeadMinutes.set(preferences.wakeUpLeadMinutes);
    this.transitAccessBufferMinutes.set(preferences.transitAccessBufferMinutes);
    this.officeArrivalLeadMinutes.set(preferences.officeArrivalLeadMinutes);
    this.visibilityCutoff.set(this.timeInputValue(preferences.visibilityCutoff));
  }

  private isValid(preferences: CommutePreferencesValues): boolean {
    return (
      [
        preferences.wakeUpLeadMinutes,
        preferences.transitAccessBufferMinutes,
        preferences.officeArrivalLeadMinutes,
      ].every((value) => Number.isFinite(value) && Number.isInteger(value) && value >= 0) &&
      /^([01]\d|2[0-3]):[0-5]\d$/.test(preferences.visibilityCutoff)
    );
  }

  private numberFromInput(event: Event): number {
    return Number((event.target as HTMLInputElement).value);
  }

  private timeInputValue(value: string | null | undefined): string {
    if (value == null) {
      return '12:00';
    }

    return /^([01]\d|2[0-3]):[0-5]\d(?::[0-5]\d(?:\.\d{1,9})?)?$/.test(value)
      ? value.slice(0, 5)
      : value;
  }
}
