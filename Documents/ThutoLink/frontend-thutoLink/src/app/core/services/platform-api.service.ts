import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AssignmentView,
  AttendanceView,
  DashboardResponse,
  FileUploadPayload,
  LoginResponse,
  MessageView,
  NotificationView,
  SubmissionView
} from '../models/platform.models';
import { AuthStateService } from './auth-state.service';

@Injectable({ providedIn: 'root' })
export class PlatformApiService {
  private readonly http = inject(HttpClient);
  private readonly authState = inject(AuthStateService);
  private readonly baseUrl = 'http://localhost:8080/api';

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, { email, password });
  }

  dashboard(): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(`${this.baseUrl}/dashboard`, { headers: this.authHeaders() });
  }

  createAssignment(payload: {
    classId: string;
    title: string;
    description: string;
    dueDate: string;
    attachment: FileUploadPayload | null;
  }): Observable<AssignmentView> {
    return this.http.post<AssignmentView>(`${this.baseUrl}/assignments`, payload, { headers: this.authHeaders() });
  }

  submitAssignment(assignmentId: string, payload: { content: string; attachment: FileUploadPayload | null }): Observable<SubmissionView> {
    return this.http.post<SubmissionView>(
      `${this.baseUrl}/assignments/${assignmentId}/submit`,
      payload,
      { headers: this.authHeaders() }
    );
  }

  downloadAssignmentAttachment(assignmentId: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/assignments/${assignmentId}/attachment`, {
      headers: this.authHeaders(),
      responseType: 'blob'
    });
  }

  downloadSubmissionAttachment(submissionId: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/submissions/${submissionId}/attachment`, {
      headers: this.authHeaders(),
      responseType: 'blob'
    });
  }

  gradeSubmission(submissionId: string, payload: { score: number; feedback: string }): Observable<SubmissionView> {
    return this.http.post<SubmissionView>(
      `${this.baseUrl}/submissions/${submissionId}/grade`,
      payload,
      { headers: this.authHeaders() }
    );
  }

  recordAttendance(classId: string, payload: {
    date: string;
    entries: Array<{ learnerId: string; status: 'PRESENT' | 'ABSENT' | 'LATE' }>;
  }): Observable<AttendanceView> {
    return this.http.post<AttendanceView>(
      `${this.baseUrl}/classes/${classId}/attendance`,
      payload,
      { headers: this.authHeaders() }
    );
  }

  createAnnouncement(payload: { classId: string; title: string; body: string }) {
    return this.http.post(`${this.baseUrl}/announcements`, payload, { headers: this.authHeaders() });
  }

  createMessage(payload: { classId: string; parentId: string; subject: string; body: string }): Observable<MessageView> {
    return this.http.post<MessageView>(`${this.baseUrl}/messages`, payload, { headers: this.authHeaders() });
  }

  markNotificationRead(notificationId: string): Observable<NotificationView> {
    return this.http.post<NotificationView>(`${this.baseUrl}/notifications/${notificationId}/read`, {}, { headers: this.authHeaders() });
  }

  private authHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.authState.token()}`
    });
  }
}
