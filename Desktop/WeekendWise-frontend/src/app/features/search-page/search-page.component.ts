import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { TripStyle } from 'src/app/core/models/trip.model';
import { TripSearchService } from 'src/app/core/services/trip-search.service';


@Component({
  selector: 'app-search-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './search-page.component.html',
  styleUrls: ['./search-page.component.css']
})
export class SearchPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly tripSearchService = inject(TripSearchService);
  private readonly router = inject(Router);

  readonly tripStyles: TripStyle[] = ['RELAXING', 'ADVENTURE', 'NIGHTLIFE', 'CULTURE', 'FAMILY'];
  isSubmitting = false;

  readonly form = this.fb.nonNullable.group({
    origin: ['JNB', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
    startDate: ['2026-03-20', Validators.required],
    endDate: ['2026-03-22', Validators.required],
    budget: [8000, [Validators.required, Validators.min(1000)]],
    travelers: [2, [Validators.required, Validators.min(1), Validators.max(6)]],
    tripStyle: ['CULTURE' as TripStyle, Validators.required],
    region: ['AFRICA', Validators.required]
  });

  search(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.tripSearchService.searchTrips(this.form.getRawValue()).pipe(
      finalize(() => this.isSubmitting = false)
    ).subscribe(() => {
      void this.router.navigate(['/results']);
    });
  }
}
