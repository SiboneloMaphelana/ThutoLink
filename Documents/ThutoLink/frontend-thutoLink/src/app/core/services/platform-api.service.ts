import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AssignmentView,
  AttendanceView,
  DashboardResponse,
  LoginResponse,
  MessageView,
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
  }): Observable<AssignmentView> {
    return this.http.post<AssignmentView>(`${this.baseUrl}/assignments`, payload, { headers: this.authHeaders() });
  }

  submitAssignment(assignmentId: string, content: string): Observable<SubmissionView> {
    return this.http.post<SubmissionView>(
      `${this.baseUrl}/assignments/${assignmentId}/submit`,
      { content },
      { headers: this.authHeaders() }
    );
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

  private authHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.authState.token()}`
    });
  }
}
