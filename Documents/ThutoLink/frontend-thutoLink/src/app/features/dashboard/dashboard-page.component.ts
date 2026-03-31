import { Component, effect, inject } from '@angular/core';
import { Router } from '@angular/router';
import { DashboardStateService } from './shared/dashboard-state.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  template: ''
})
export class DashboardPageComponent {
  private readonly state = inject(DashboardStateService);
  private readonly router = inject(Router);

  constructor() {
    this.state.loadDashboard();
    effect(() => {
      const role = this.state.currentRole();

      if (role === 'ADMIN') {
        void this.router.navigateByUrl('/dashboard/admin');
      } else if (role === 'TEACHER') {
        void this.router.navigateByUrl('/dashboard/teacher');
      } else if (role === 'PARENT') {
        void this.router.navigateByUrl('/dashboard/parent');
      } else if (role === 'LEARNER') {
        void this.router.navigateByUrl('/dashboard/learner');
      }
    });
  }
}
