import { CommonModule } from '@angular/common';
import { Component, computed, inject, input } from '@angular/core';
import { AppIconName } from '../../../core/icons/icons';
import { DashboardStateService } from './dashboard-state.service';
import { AppIconComponent } from '../../../shared/app-icon.component';

@Component({
  selector: 'app-dashboard-shell',
  standalone: true,
  imports: [CommonModule, AppIconComponent],
  templateUrl: './dashboard-shell.component.html'
})
export class DashboardShellComponent {
  readonly title = input.required<string>();
  readonly description = input.required<string>();
  readonly icon = input.required<AppIconName>();

  readonly state = inject(DashboardStateService);
  readonly data = computed(() => this.state.dashboard());
}
