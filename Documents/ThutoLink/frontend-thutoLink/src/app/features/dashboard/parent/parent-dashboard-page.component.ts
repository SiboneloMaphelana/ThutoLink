import { CommonModule, DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { DashboardShellComponent } from '../shared/dashboard-shell.component';
import { DashboardStateService } from '../shared/dashboard-state.service';
import { AppIconComponent } from '../../../shared/app-icon.component';

@Component({
  selector: 'app-parent-dashboard-page',
  standalone: true,
  imports: [CommonModule, DatePipe, DashboardShellComponent, AppIconComponent],
  templateUrl: './parent-dashboard-page.component.html'
})
export class ParentDashboardPageComponent {
  readonly state = inject(DashboardStateService);

  constructor() {
    this.state.loadDashboard();
  }
}
