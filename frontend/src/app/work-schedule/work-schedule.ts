import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  AddressSuggestion,
  MonthlyOccurrence,
  MonthlyPatternType,
  OfficeInput,
  RecurrenceLevel,
  RecurringRule,
  RecurringRuleInput,
  SavedOffice,
  ScheduleDay,
  ScheduleStatus,
  WorkScheduleService,
} from './work-schedule.service';

const statuses: readonly ScheduleStatus[] = ['OFFICE', 'WORK_FROM_HOME', 'NON_WORKING'];
const weekdays = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const occurrences: readonly MonthlyOccurrence[] = ['FIRST', 'SECOND', 'THIRD', 'FOURTH', 'LAST'];

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

function emptyRule(): RecurringRuleInput {
  return {
    level: 'DAYS',
    interval: 1,
    weekdays: [],
    monthlyPattern: null,
    startDate: today(),
    endDate: null,
    status: 'WORK_FROM_HOME',
    officeId: null,
  };
}

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
  readonly recurringRules = signal<RecurringRule[]>([]);
  readonly ruleDraft = signal<RecurringRuleInput>(emptyRule());
  readonly ruleSaving = signal(false);
  readonly ruleError = signal<string | null>(null);
  readonly editingRuleId = signal<string | null>(null);
  private searchSequence = 0;

  readonly statuses = statuses;
  readonly recurrenceLevels: readonly RecurrenceLevel[] = ['DAYS', 'WEEKS', 'MONTHS'];
  readonly ruleWeekdays = weekdays;
  readonly monthlyPatternTypes: readonly MonthlyPatternType[] = [
    'CALENDAR_DAY',
    'WEEKDAY_OCCURRENCE',
  ];
  readonly monthlyOccurrences = occurrences;
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
        this.loadRecurringRules();
      },
      error: () => {
        this.error.set('The work schedule could not be loaded.');
        this.loading.set(false);
      },
    });
  }

  loadRecurringRules(): void {
    this.service.getRecurringRules().subscribe({
      next: (rules) => this.recurringRules.set(rules),
      error: () => this.ruleError.set('Recurring rules could not be loaded.'),
    });
  }

  setRuleField<K extends keyof RecurringRuleInput>(field: K, value: RecurringRuleInput[K]): void {
    this.ruleError.set(null);
    this.ruleDraft.update((draft) => ({ ...draft, [field]: value }));
  }

  setRuleLevel(level: RecurrenceLevel): void {
    this.ruleDraft.update((draft) => ({
      ...draft,
      level,
      weekdays: level === 'WEEKS' ? draft.weekdays : [],
      monthlyPattern:
        level === 'MONTHS'
          ? (draft.monthlyPattern ?? {
              type: 'CALENDAR_DAY',
              calendarDay: 1,
              weekday: null,
              occurrence: null,
            })
          : null,
    }));
  }

  toggleRuleWeekday(day: string): void {
    const selected = this.ruleDraft().weekdays;
    this.setRuleField(
      'weekdays',
      selected.includes(day) ? selected.filter((value) => value !== day) : [...selected, day],
    );
  }

  setMonthlyPatternField(
    field: 'type' | 'calendarDay' | 'weekday' | 'occurrence',
    value: string | number | null,
  ): void {
    const current = this.ruleDraft().monthlyPattern ?? {
      type: 'CALENDAR_DAY' as MonthlyPatternType,
      calendarDay: 1,
      weekday: null,
      occurrence: null,
    };
    const next = { ...current, [field]: value };
    if (field === 'type') {
      next.calendarDay = value === 'CALENDAR_DAY' ? 1 : null;
      next.weekday = value === 'WEEKDAY_OCCURRENCE' ? 'MONDAY' : null;
      next.occurrence = value === 'WEEKDAY_OCCURRENCE' ? 'FIRST' : null;
    }
    this.setRuleField('monthlyPattern', next);
  }

  setRuleStatus(status: ScheduleStatus): void {
    this.setRuleField('status', status);
    if (status !== 'OFFICE') {
      this.setRuleField('officeId', null);
    }
  }

  saveRecurringRule(): void {
    const draft = this.ruleDraft();
    if (draft.level === 'WEEKS' && draft.weekdays.length === 0) {
      this.ruleError.set('Choose at least one weekday.');
      return;
    }
    if (draft.status === 'OFFICE' && !draft.officeId) {
      this.ruleError.set('Choose a saved office.');
      return;
    }
    this.ruleSaving.set(true);
    this.ruleError.set(null);
    const editingId = this.editingRuleId();
    const request = editingId
      ? this.service.editRecurringRule(editingId, draft)
      : this.service.addRecurringRule(draft);
    request.subscribe({
      next: (rule) => {
        this.recurringRules.update((rules) =>
          editingId
            ? rules.map((current) => (current.id === rule.id ? rule : current))
            : [...rules, rule],
        );
        this.resetRuleDraft();
        this.ruleSaving.set(false);
      },
      error: (response: HttpErrorResponse) => {
        this.ruleError.set(response.error?.detail ?? 'The recurring rule could not be saved.');
        this.ruleSaving.set(false);
      },
    });
  }

  editRecurringRule(rule: RecurringRule): void {
    this.editingRuleId.set(rule.id);
    this.ruleDraft.set({
      level: rule.level,
      interval: rule.interval,
      weekdays: [...rule.weekdays],
      monthlyPattern: rule.monthlyPattern ? { ...rule.monthlyPattern } : null,
      startDate: rule.startDate,
      endDate: rule.endDate,
      status: rule.status,
      officeId: rule.officeId,
    });
    this.ruleError.set(null);
  }

  deleteRecurringRule(rule: RecurringRule): void {
    this.service.deleteRecurringRule(rule.id).subscribe({
      next: () =>
        this.recurringRules.update((rules) => rules.filter((current) => current.id !== rule.id)),
      error: () => this.ruleError.set('The recurring rule could not be deleted.'),
    });
  }

  resetRuleDraft(): void {
    this.editingRuleId.set(null);
    this.ruleDraft.set(emptyRule());
    this.ruleError.set(null);
  }

  ruleLabel(rule: RecurringRule): string {
    const cadence = this.recurrenceUnit(rule.level, rule.interval);
    return `Every ${rule.interval} ${cadence} · ${this.statusLabel(rule.status)}`;
  }

  recurrenceUnit(level: RecurrenceLevel, interval: number): string {
    let unit: string;
    if (level === 'DAYS') {
      unit = 'day';
    } else if (level === 'WEEKS') {
      unit = 'week';
    } else {
      unit = 'month';
    }
    return interval === 1 ? unit : `${unit}s`;
  }

  monthlyOccurrenceLabel(occurrence: MonthlyOccurrence | null): string {
    return occurrence ? occurrence.charAt(0) + occurrence.slice(1).toLowerCase() : '';
  }

  weekdayLabel(day: string | null): string {
    return day ? (this.dayLabels[day] ?? day) : '';
  }

  statusLabel(status: ScheduleStatus): string {
    if (status === 'WORK_FROM_HOME') {
      return 'Work from home';
    }
    if (status === 'NON_WORKING') {
      return 'Non-working';
    }
    return 'Office';
  }

  officeLabel(rule: RecurringRule): string | null {
    if (rule.status !== 'OFFICE' || !rule.officeId) {
      return null;
    }
    return (
      this.offices().find((office) => office.id === rule.officeId)?.label ??
      'Saved location unavailable'
    );
  }

  setDayStatus(day: string, status: ScheduleStatus): void {
    this.saved.set(false);
    this.days.update((days) =>
      days.map((current) => {
        if (current.day !== day) {
          return current;
        }
        const officeId = status === 'OFFICE' ? current.officeId : null;
        return { ...current, status, officeId };
      }),
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
