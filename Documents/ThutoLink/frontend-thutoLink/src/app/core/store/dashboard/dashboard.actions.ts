import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { DashboardResponse, FileUploadPayload } from '../../models/platform.models';

export const DashboardActions = createActionGroup({
  source: 'Dashboard',
  events: {
    'Load Requested': emptyProps(),
    'Load Succeeded': props<{ dashboard: DashboardResponse }>(),
    'Load Failed': props<{ error: string }>(),
    'Clear Notice': emptyProps(),
    'Create Assignment Requested': props<{ payload: { classId: string; title: string; description: string; dueDate: string; attachment: FileUploadPayload | null } }>(),
    'Submit Assignment Requested': props<{ assignmentId: string; payload: { content: string; attachment: FileUploadPayload | null } }>(),
    'Grade Submission Requested': props<{ submissionId: string; payload: { score: number; feedback: string } }>(),
    'Record Attendance Requested': props<{ classId: string; payload: { date: string; entries: Array<{ learnerId: string; status: 'PRESENT' | 'ABSENT' | 'LATE' }> } }>(),
    'Create Announcement Requested': props<{ payload: { classId: string; title: string; body: string } }>(),
    'Create Message Requested': props<{ payload: { classId: string; parentId: string; subject: string; body: string } }>(),
    'Mark Notification Read Requested': props<{ notificationId: string }>(),
    'Mutation Started': emptyProps(),
    'Mutation Succeeded': props<{ notice: string }>(),
    'Mutation Failed': props<{ error: string }>()
  }
});
