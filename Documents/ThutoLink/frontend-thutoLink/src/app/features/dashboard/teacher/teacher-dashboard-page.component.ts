import { CommonModule, DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DashboardShellComponent } from '../shared/dashboard-shell.component';
import { DashboardStateService } from '../shared/dashboard-state.service';
import { AppIconComponent } from '../../../shared/app-icon.component';

@Component({
  selector: 'app-teacher-dashboard-page',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, DashboardShellComponent, AppIconComponent],
  templateUrl: './teacher-dashboard-page.component.html'
})
export class TeacherDashboardPageComponent {
  readonly state = inject(DashboardStateService);

  constructor() {
    this.state.loadDashboard();
  }

  async onAssignmentFileSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    try {
      await this.state.updateAssignmentAttachment(file);
    } catch (error) {
      this.state.reportClientError(error instanceof Error ? error.message : 'The selected file could not be attached.');
      input.value = '';
    }
  }

  async downloadAssignmentAttachment(assignmentId: string): Promise<void> {
    const assignment = this.state.dashboard()?.assignments.find((item) => item.id === assignmentId);
    try {
      await this.state.openAssignmentAttachment(assignmentId, assignment?.attachment ?? null);
    } catch (error) {
      this.state.reportClientError(error instanceof Error ? error.message : 'The attachment could not be downloaded.');
    }
  }

  async downloadSubmissionAttachment(submissionId: string, assignmentId: string): Promise<void> {
    const submission = this.state.dashboard()?.assignments.find((assignment) => assignment.id === assignmentId)?.submissions.find((item) => item.id === submissionId);
    try {
      await this.state.openSubmissionAttachment(submissionId, submission?.attachment ?? null);
    } catch (error) {
      this.state.reportClientError(error instanceof Error ? error.message : 'The attachment could not be downloaded.');
    }
  }
}
