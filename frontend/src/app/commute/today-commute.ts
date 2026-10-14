import { Component, inject, OnInit, signal } from '@angular/core';

import { DashboardLocationService } from '../shared/dashboard-location.service';
import {
  TodayCommute as TodayCommuteData,
  TodayCommuteSegment,
  TodayCommuteService,
} from './today-commute.service';

@Component({
  selector: 'app-today-commute',
  templateUrl: './today-commute.html',
  styleUrl: './today-commute.scss',
})
export class TodayCommute implements OnInit {
  private readonly service = inject(TodayCommuteService);
  private readonly location = inject(DashboardLocationService).location;

  protected readonly commute = signal<TodayCommuteData | null>(null);
  protected readonly status = signal<'loading' | 'ready' | 'unavailable'>('loading');

  ngOnInit(): void {
    this.service.getTodayCommute(this.location.latitude, this.location.longitude).subscribe({
      next: (commute) => {
        this.commute.set(commute);
        this.status.set('ready');
      },
      error: () => this.status.set('unavailable'),
    });
  }

  protected time(value: string | undefined): string {
    return value?.slice(0, 5) ?? '';
  }

  protected segmentMode(segment: TodayCommuteSegment): string {
    if (segment.type === 'WALKING') {
      return 'Walk';
    }

    const mode = this.modeLabel(segment.mode);
    const service = segment.service?.trim();
    return (
      [mode, service === '' ? undefined : service]
        .filter((value) => value !== undefined)
        .join(' ') || 'Public transport'
    );
  }

  protected segmentRoute(
    segment: TodayCommuteSegment,
    index: number,
    segments: readonly TodayCommuteSegment[],
    destinationOffice: string | undefined,
  ): string | undefined {
    const from = this.displayLocation(segment.from);
    const to =
      this.displayLocation(segment.to) ??
      (segment.type === 'WALKING' && index === segments.length - 1
        ? this.displayLocation(destinationOffice)
        : undefined);
    const locations = [from, to].filter((value): value is string => value !== undefined);
    return locations.length === 0 ? undefined : locations.join(' → ');
  }

  protected segmentMeta(segment: TodayCommuteSegment): string | undefined {
    if (segment.type === 'WALKING') {
      return undefined;
    }

    const direction = segment.direction === undefined ? undefined : `toward ${segment.direction}`;
    const platform = segment.platform === undefined ? undefined : `platform ${segment.platform}`;
    const details = [direction, platform].filter((value): value is string => value !== undefined);
    return details.length === 0 ? undefined : details.join(' · ');
  }

  private displayLocation(value: string | undefined): string | undefined {
    return value === undefined || /^(START|END)$/i.test(value.trim()) ? undefined : value;
  }

  private modeLabel(value: string | undefined): string | undefined {
    if (value === undefined) {
      return undefined;
    }

    if (value.trim().toUpperCase() === 'REGIONAL_RAIL') {
      return 'Train';
    }

    return value
      .trim()
      .toLowerCase()
      .split(/[_\s-]+/)
      .filter((part) => part.length > 0)
      .map((part) => `${part.charAt(0).toUpperCase()}${part.slice(1)}`)
      .join(' ');
  }
}
