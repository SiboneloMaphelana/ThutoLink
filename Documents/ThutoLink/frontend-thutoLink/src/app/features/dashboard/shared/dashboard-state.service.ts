import { Injectable, computed, effect, inject, signal, untracked } from '@angular/core';
import { Store } from '@ngrx/store';
import { firstValueFrom } from 'rxjs';
import {
  AssignmentView,
  ClassView,
  DashboardResponse,
  FileAttachmentView,
  FileUploadPayload,
  NotificationView,
  ParentLink,
  UserRole
} from '../../../core/models/platform.models';
import { DashboardActions } from '../../../core/store/dashboard/dashboard.actions';
import { selectDashboard, selectDashboardError, selectDashboardLoading, selectDashboardNotice } from '../../../core/store/dashboard/dashboard.selectors';
import { AuthActions } from '../../../core/store/auth/auth.actions';
import { selectAuthUser } from '../../../core/store/auth/auth.selectors';
import { PlatformApiService } from '../../../core/services/platform-api.service';

@Injectable({ providedIn: 'root' })
export class DashboardStateService {
  private readonly maxAttachmentSizeBytes = 5 * 1024 * 1024;
  private readonly store = inject(Store);
  private readonly api = inject(PlatformApiService);
  private readonly authUser = this.store.selectSignal(selectAuthUser);

  readonly dashboard = this.store.selectSignal(selectDashboard);
  readonly loading = this.store.selectSignal(selectDashboardLoading);
  readonly busyAction = signal('');
  readonly error = this.store.selectSignal(selectDashboardError);
  readonly notice = this.store.selectSignal(selectDashboardNotice);

  readonly assignmentForm = signal({
    classId: '',
    title: '',
    description: '',
    dueDate: '',
    attachment: null as FileUploadPayload | null
  });

  readonly attendanceForm = signal({
    classId: '',
    date: new Date().toISOString().slice(0, 10),
    entries: {} as Record<string, 'PRESENT' | 'ABSENT' | 'LATE'>
  });

  readonly announcementForm = signal({
    classId: '',
    title: '',
    body: ''
  });

  readonly messageForm = signal({
    classId: '',
    parentId: '',
    subject: '',
    body: ''
  });

  readonly submissionDrafts = signal<Record<string, { content: string; attachment: FileUploadPayload | null }>>({});
  readonly gradeDrafts = signal<Record<string, { score: number; feedback: string }>>({});

  readonly currentUser = computed(() => this.dashboard()?.currentUser ?? this.authUser());
  readonly currentRole = computed<UserRole | null>(() => this.currentUser()?.role ?? null);
  readonly teacherParents = computed(() => this.parentOptionsForClass(this.messageForm().classId));

  constructor() {
    effect(() => {
      const dashboard = this.dashboard();
      if (dashboard) {
        untracked(() => this.initializeForms(dashboard));
      }
    });
  }

  loadDashboard(): void {
    this.store.dispatch(DashboardActions.loadRequested());
  }

  logout(): void {
    this.store.dispatch(AuthActions.logoutRequested());
  }

  createAssignment(): void {
    const form = this.assignmentForm();
    if (!form.classId || !form.title.trim() || !form.description.trim() || !form.dueDate) {
      return;
    }

    this.assignmentForm.set({ classId: form.classId, title: '', description: '', dueDate: '', attachment: null });
    this.runAction('assignment', () => this.store.dispatch(DashboardActions.createAssignmentRequested({ payload: form })));
  }

  submitAssignment(assignmentId: string): void {
    const draft = this.submissionDrafts()[assignmentId] ?? { content: '', attachment: null };
    if (!draft.content.trim() && !draft.attachment) {
      return;
    }

    this.submissionDrafts.update((state) => ({ ...state, [assignmentId]: { content: '', attachment: null } }));
    this.runAction(`submit-${assignmentId}`, () =>
      this.store.dispatch(
        DashboardActions.submitAssignmentRequested({
          assignmentId,
          payload: {
            content: draft.content,
            attachment: draft.attachment
          }
        })
      )
    );
  }

  gradeSubmission(submissionId: string): void {
    const draft = this.gradeDrafts()[submissionId];
    if (!draft || Number.isNaN(Number(draft.score)) || !draft.feedback.trim()) {
      return;
    }

    this.runAction(`grade-${submissionId}`, () =>
      this.store.dispatch(DashboardActions.gradeSubmissionRequested({ submissionId, payload: { score: Number(draft.score), feedback: draft.feedback } }))
    );
  }

  recordAttendance(): void {
    const form = this.attendanceForm();
    if (!form.classId || !form.date) {
      return;
    }

    const entries = Object.entries(form.entries).map(([learnerId, status]) => ({ learnerId, status }));
    if (!entries.length) {
      return;
    }

    this.runAction('attendance', () =>
      this.store.dispatch(DashboardActions.recordAttendanceRequested({ classId: form.classId, payload: { date: form.date, entries } }))
    );
  }

  createAnnouncement(): void {
    const form = this.announcementForm();
    if (!form.classId || !form.title.trim() || !form.body.trim()) {
      return;
    }

    this.announcementForm.set({ classId: form.classId, title: '', body: '' });
    this.runAction('announcement', () => this.store.dispatch(DashboardActions.createAnnouncementRequested({ payload: form })));
  }

  createMessage(): void {
    const form = this.messageForm();
    if (!form.classId || !form.parentId || !form.subject.trim() || !form.body.trim()) {
      return;
    }

    const classId = form.classId;
    this.messageForm.set({ classId, parentId: '', subject: '', body: '' });
    this.runAction('message', () => this.store.dispatch(DashboardActions.createMessageRequested({ payload: form })));
  }

  markNotificationRead(notificationId: string): void {
    this.runAction(`notification-${notificationId}`, () =>
      this.store.dispatch(DashboardActions.markNotificationReadRequested({ notificationId }))
    );
  }

  setAttendanceClass(classId: string): void {
    const schoolClass = this.classById(classId);
    const entries = Object.fromEntries(
      schoolClass?.learners.map((learner) => [learner.id, 'PRESENT' as const]) ?? []
    ) as Record<string, 'PRESENT' | 'ABSENT' | 'LATE'>;
    this.attendanceForm.update((state) => ({ ...state, classId, entries }));
  }

  updateAttendanceStatus(learnerId: string, status: 'PRESENT' | 'ABSENT' | 'LATE'): void {
    this.attendanceForm.update((state) => ({
      ...state,
      entries: {
        ...state.entries,
        [learnerId]: status
      }
    }));
  }

  updateSubmissionDraft(assignmentId: string, value: string): void {
    this.submissionDrafts.update((state) => ({
      ...state,
      [assignmentId]: {
        content: value,
        attachment: state[assignmentId]?.attachment ?? null
      }
    }));
  }

  submissionDraftValue(assignment: AssignmentView): string {
    return this.submissionDrafts()[assignment.id]?.content || assignment.submissions[0]?.content || '';
  }

  submissionAttachment(assignmentId: string): FileUploadPayload | null {
    return this.submissionDrafts()[assignmentId]?.attachment ?? null;
  }

  updateAssignmentField(field: 'classId' | 'title' | 'description' | 'dueDate', value: string): void {
    this.assignmentForm.update((state) => ({ ...state, [field]: value }));
  }

  updateAnnouncementField(field: 'classId' | 'title' | 'body', value: string): void {
    this.announcementForm.update((state) => ({ ...state, [field]: value }));
  }

  updateMessageField(field: 'classId' | 'parentId' | 'subject' | 'body', value: string): void {
    this.messageForm.update((state) => ({ ...state, [field]: value }));
  }

  updateAttendanceDate(value: string): void {
    this.attendanceForm.update((state) => ({ ...state, date: value }));
  }

  updateGradeDraft(submissionId: string, field: 'score' | 'feedback', value: string): void {
    const current = this.gradeDrafts()[submissionId] ?? { score: 0, feedback: '' };
    this.gradeDrafts.update((state) => ({
      ...state,
      [submissionId]: {
        ...current,
        [field]: field === 'score' ? Number(value) : value
      }
    }));
  }

  updateMessageClass(classId: string): void {
    const firstParent = this.parentOptionsForClass(classId)[0]?.parent.id ?? '';
    this.messageForm.update((state) => ({ ...state, classId, parentId: firstParent }));
  }

  pendingReviewCount(assignment: AssignmentView): number {
    return assignment.submissions.filter((submission) => submission.status !== 'Reviewed').length;
  }

  classById(classId: string): ClassView | undefined {
    return this.dashboard()?.classes.find((schoolClass) => schoolClass.id === classId);
  }

  parentOptionsForClass(classId: string): ParentLink[] {
    return this.classById(classId)?.parentLinks ?? [];
  }

  recentNotifications(): NotificationView[] {
    return this.dashboard()?.notifications.slice(0, 6) ?? [];
  }

  unreadNotifications(): number {
    return this.dashboard()?.notifications.filter((notification) => !notification.read).length ?? 0;
  }

  formatRole(role: UserRole | null | undefined): string {
    if (!role) {
      return '';
    }
    return role.charAt(0) + role.slice(1).toLowerCase();
  }

  reportClientError(message: string): void {
    this.store.dispatch(DashboardActions.mutationFailed({ error: message }));
  }

  async updateAssignmentAttachment(file: File | null): Promise<void> {
    const attachment = file ? await this.toUploadPayload(file) : null;
    this.assignmentForm.update((state) => ({ ...state, attachment }));
  }

  async updateSubmissionAttachment(assignmentId: string, file: File | null): Promise<void> {
    const attachment = file ? await this.toUploadPayload(file) : null;
    this.submissionDrafts.update((state) => ({
      ...state,
      [assignmentId]: {
        content: state[assignmentId]?.content ?? '',
        attachment
      }
    }));
  }

  async openAssignmentAttachment(assignmentId: string, attachment: FileAttachmentView | null): Promise<void> {
    if (!attachment) {
      return;
    }
    const blob = await firstValueFrom(this.api.downloadAssignmentAttachment(assignmentId));
    this.downloadBlob(blob, attachment.fileName);
  }

  async openSubmissionAttachment(submissionId: string, attachment: FileAttachmentView | null): Promise<void> {
    if (!attachment) {
      return;
    }
    const blob = await firstValueFrom(this.api.downloadSubmissionAttachment(submissionId));
    this.downloadBlob(blob, attachment.fileName);
  }

  formatAttachmentSize(size: number | null | undefined): string {
    if (!size) {
      return '0 KB';
    }
    if (size >= 1024 * 1024) {
      return `${(size / (1024 * 1024)).toFixed(1)} MB`;
    }
    return `${Math.max(1, Math.round(size / 1024))} KB`;
  }

  private initializeForms(dashboard: DashboardResponse): void {
    const firstClass = dashboard.classes[0]?.id ?? '';
    const currentAssignmentForm = this.assignmentForm();
    const currentAnnouncementForm = this.announcementForm();
    const currentAttendanceForm = this.attendanceForm();
    const currentMessageForm = this.messageForm();

    this.assignmentForm.set({
      classId: currentAssignmentForm.classId || firstClass,
      title: '',
      description: '',
      dueDate: '',
      attachment: null
    });

    this.announcementForm.set({
      classId: currentAnnouncementForm.classId || firstClass,
      title: '',
      body: ''
    });

    const attendanceClassId = currentAttendanceForm.classId || firstClass;
    const attendanceEntries = Object.fromEntries(
      (dashboard.classes.find((schoolClass) => schoolClass.id === attendanceClassId)?.learners ?? []).map((learner) => [learner.id, 'PRESENT'])
    ) as Record<string, 'PRESENT' | 'ABSENT' | 'LATE'>;

    this.attendanceForm.set({
      classId: attendanceClassId,
      date: currentAttendanceForm.date,
      entries: attendanceEntries
    });

    const messageClassId = currentMessageForm.classId || firstClass;
    const defaultParent =
      dashboard.classes.find((schoolClass) => schoolClass.id === messageClassId)?.parentLinks[0]?.parent.id ?? '';
    this.messageForm.set({
      classId: messageClassId,
      parentId: defaultParent,
      subject: '',
      body: ''
    });

    const drafts: Record<string, { score: number; feedback: string }> = {};
    const submissionDrafts: Record<string, { content: string; attachment: FileUploadPayload | null }> = {};
    dashboard.assignments.forEach((assignment) => {
      submissionDrafts[assignment.id] = {
        content: this.submissionDrafts()[assignment.id]?.content ?? assignment.submissions[0]?.content ?? '',
        attachment: this.submissionDrafts()[assignment.id]?.attachment ?? null
      };
      assignment.submissions.forEach((submission) => {
        drafts[submission.id] = {
          score: submission.score ?? 0,
          feedback: submission.feedback ?? ''
        };
      });
    });
    this.gradeDrafts.set(drafts);
    this.submissionDrafts.set(submissionDrafts);
  }

  private runAction(key: string, action: () => void): void {
    this.busyAction.set(key);
    this.store.dispatch(DashboardActions.mutationStarted());
    action();
    this.busyAction.set('');
  }

  private async toUploadPayload(file: File): Promise<FileUploadPayload> {
    const contentType = file.type || this.inferContentType(file.name);
    if (!this.isAllowedAttachmentType(contentType)) {
      throw new Error('Only PDF and image files are supported.');
    }
    if (file.size > this.maxAttachmentSizeBytes) {
      throw new Error('Attachments must be 5 MB or smaller.');
    }

    const base64Data = await this.readFileAsBase64(file);
    return {
      fileName: file.name,
      contentType,
      size: file.size,
      base64Data
    };
  }

  private readFileAsBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const result = typeof reader.result === 'string' ? reader.result : '';
        const [, base64Data = ''] = result.split(',');
        if (!base64Data) {
          reject(new Error('The selected file could not be read.'));
          return;
        }
        resolve(base64Data);
      };
      reader.onerror = () => reject(new Error('The selected file could not be read.'));
      reader.readAsDataURL(file);
    });
  }

  private inferContentType(fileName: string): string {
    const extension = fileName.split('.').pop()?.toLowerCase();
    switch (extension) {
      case 'pdf':
        return 'application/pdf';
      case 'png':
        return 'image/png';
      case 'jpg':
      case 'jpeg':
        return 'image/jpeg';
      case 'gif':
        return 'image/gif';
      case 'webp':
        return 'image/webp';
      default:
        return '';
    }
  }

  private isAllowedAttachmentType(contentType: string): boolean {
    return ['application/pdf', 'image/png', 'image/jpeg', 'image/gif', 'image/webp'].includes(contentType);
  }

  private downloadBlob(blob: Blob, fileName: string): void {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.click();
    URL.revokeObjectURL(url);
  }
}
