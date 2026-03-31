import { CommonModule, DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DashboardShellComponent } from '../shared/dashboard-shell.component';
import { DashboardStateService } from '../shared/dashboard-state.service';
import { AppIconComponent } from '../../../shared/app-icon.component';

@Component({
  selector: 'app-learner-dashboard-page',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, DashboardShellComponent, AppIconComponent],
  templateUrl: './learner-dashboard-page.component.html'
})
export class LearnerDashboardPageComponent {
  readonly state = inject(DashboardStateService);

  constructor() {
    this.state.loadDashboard();
  }
}
