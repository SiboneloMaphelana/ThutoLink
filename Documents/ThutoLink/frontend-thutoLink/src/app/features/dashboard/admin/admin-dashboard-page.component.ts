import { CommonModule, DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { DashboardShellComponent } from '../shared/dashboard-shell.component';
import { DashboardStateService } from '../shared/dashboard-state.service';
import { AppIconComponent } from '../../../shared/app-icon.component';

@Component({
  selector: 'app-admin-dashboard-page',
  standalone: true,
  imports: [CommonModule, DatePipe, DashboardShellComponent, AppIconComponent],
  templateUrl: './admin-dashboard-page.component.html'
})
export class AdminDashboardPageComponent {
  readonly state = inject(DashboardStateService);

  constructor() {
    this.state.loadDashboard();
  }
}
