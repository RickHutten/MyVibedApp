import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  AddressSuggestion,
  OfficeInput,
  SavedOffice,
  ScheduleDay,
  ScheduleStatus,
  WorkScheduleService,
} from './work-schedule.service';

const statuses: readonly ScheduleStatus[] = ['OFFICE', 'WORK_FROM_HOME', 'NON_WORKING'];

@Component({
  selector: 'app-work-schedule',
  imports: [FormsModule],
  templateUrl: './work-schedule.html',
  styleUrl: './work-schedule.scss',
})
export class WorkSchedule implements OnInit {
  readonly days = signal<ScheduleDay[]>([]);
  readonly offices = signal<SavedOffice[]>([]);
  readonly start = signal('09:00');
  readonly end = signal('17:00');
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly saved = signal(false);
  readonly error = signal<string | null>(null);
  readonly officeError = signal<string | null>(null);
  readonly suggestions = signal<AddressSuggestion[]>([]);
  readonly searching = signal(false);
  readonly officeDraft = signal<OfficeInput>({
    label: '',
    address: '',
    latitude: 0,
    longitude: 0,
  });
  readonly locationSelected = signal(false);
  readonly editingOfficeId = signal<string | null>(null);
  private searchSequence = 0;

  readonly statuses = statuses;
  readonly dayLabels: Record<string, string> = {
    MONDAY: 'Monday',
    TUESDAY: 'Tuesday',
    WEDNESDAY: 'Wednesday',
    THURSDAY: 'Thursday',
    FRIDAY: 'Friday',
    SATURDAY: 'Saturday',
    SUNDAY: 'Sunday',
  };

  private readonly service = inject(WorkScheduleService);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.getSchedule().subscribe({
      next: (schedule) => {
        this.start.set(schedule.workingHours.start);
        this.end.set(schedule.workingHours.end);
        this.days.set(schedule.days);
        this.offices.set(schedule.offices);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('The work schedule could not be loaded.');
        this.loading.set(false);
      },
    });
  }

  setDayStatus(day: string, status: ScheduleStatus): void {
    this.saved.set(false);
    this.days.update((days) =>
      days.map((current) =>
        current.day === day
          ? { ...current, status, officeId: status === 'OFFICE' ? current.officeId : null }
          : current,
      ),
    );
  }

  setStartFromEvent(event: Event): void {
    this.start.set((event.target as HTMLInputElement).value);
    this.saved.set(false);
  }

  setEndFromEvent(event: Event): void {
    this.end.set((event.target as HTMLInputElement).value);
    this.saved.set(false);
  }

  setDayOffice(day: string, officeId: string): void {
    this.saved.set(false);
    this.days.update((days) =>
      days.map((current) =>
        current.day === day ? { ...current, officeId: officeId || null } : current,
      ),
    );
  }

  setStatusFromEvent(day: string, event: Event): void {
    const status = (event.target as HTMLSelectElement).value as ScheduleStatus;
    this.setDayStatus(day, status);
  }

  setOfficeFromEvent(day: string, event: Event): void {
    this.setDayOffice(day, (event.target as HTMLSelectElement).value);
  }

  saveSchedule(): void {
    const officeDayWithoutOffice = this.days().find(
      (day) => day.status === 'OFFICE' && !day.officeId,
    );
    if (officeDayWithoutOffice) {
      this.error.set(`${this.dayLabels[officeDayWithoutOffice.day]} needs a saved office.`);
      return;
    }

    this.saving.set(true);
    this.saved.set(false);
    this.error.set(null);
    this.service
      .replaceSchedule({
        workingHours: { start: this.start(), end: this.end() },
        days: this.days(),
      })
      .subscribe({
        next: (schedule) => {
          this.days.set(schedule.days);
          this.offices.set(schedule.offices);
          this.saving.set(false);
          this.saved.set(true);
        },
        error: (response: HttpErrorResponse) => {
          this.error.set(response.error?.detail ?? 'The work schedule could not be saved.');
          this.saving.set(false);
        },
      });
  }

  updateDraft(field: keyof OfficeInput, event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.officeDraft.update((draft) => ({
      ...draft,
      [field]: field === 'latitude' || field === 'longitude' ? Number(value) : value,
    }));
  }

  searchAddresses(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    const sequence = ++this.searchSequence;
    this.officeDraft.update((draft) => ({ ...draft, address: query }));
    this.locationSelected.set(false);
    this.suggestions.set([]);
    if (query.trim().length < 3) {
      this.searching.set(false);
      return;
    }
    this.searching.set(true);
    this.officeError.set(null);
    this.service.searchAddresses(query).subscribe({
      next: (suggestions) => {
        if (sequence !== this.searchSequence) {
          return;
        }
        this.suggestions.set(suggestions);
        this.searching.set(false);
      },
      error: () => {
        if (sequence !== this.searchSequence) {
          return;
        }
        this.officeError.set('Address search is unavailable.');
        this.searching.set(false);
      },
    });
  }

  selectSuggestion(suggestion: AddressSuggestion): void {
    this.officeDraft.update((draft) => ({
      ...draft,
      address: suggestion.address,
      latitude: suggestion.latitude,
      longitude: suggestion.longitude,
    }));
    this.locationSelected.set(true);
    this.suggestions.set([]);
  }

  saveOffice(): void {
    const draft = this.officeDraft();
    if (!draft.label.trim() || !draft.address.trim() || !this.locationSelected()) {
      this.officeError.set('Choose a label and an address suggestion.');
      return;
    }
    this.officeError.set(null);
    const editingId = this.editingOfficeId();
    const request = editingId
      ? this.service.editOffice(editingId, draft)
      : this.service.addOffice(draft);
    request.subscribe({
      next: () => {
        this.resetOfficeDraft();
        this.load();
      },
      error: (response: HttpErrorResponse) => {
        this.officeError.set(response.error?.detail ?? 'The office could not be saved.');
      },
    });
  }

  editOffice(office: SavedOffice): void {
    this.editingOfficeId.set(office.id);
    this.officeDraft.set({
      label: office.label,
      address: office.address,
      latitude: office.latitude,
      longitude: office.longitude,
    });
    this.locationSelected.set(true);
    this.officeError.set(null);
  }

  deleteOffice(office: SavedOffice): void {
    this.service.deleteOffice(office.id).subscribe({
      next: () => this.load(),
      error: () => this.officeError.set('The office could not be deleted.'),
    });
  }

  resetOfficeDraft(): void {
    this.editingOfficeId.set(null);
    this.officeDraft.set({ label: '', address: '', latitude: 0, longitude: 0 });
    this.locationSelected.set(false);
    this.suggestions.set([]);
  }

  activeOffices(): SavedOffice[] {
    return this.offices().filter((office) => !office.deleted);
  }

  officesFor(day: ScheduleDay): SavedOffice[] {
    const active = this.activeOffices();
    if (!day.officeId || active.some((office) => office.id === day.officeId)) {
      return active;
    }
    const assignedOffice = this.offices().find((office) => office.id === day.officeId);
    return assignedOffice ? [...active, assignedOffice] : active;
  }
}
