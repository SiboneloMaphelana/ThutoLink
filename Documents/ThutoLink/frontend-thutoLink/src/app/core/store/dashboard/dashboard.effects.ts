import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, concatMap, map, mergeMap, of, switchMap } from 'rxjs';
import { PlatformApiService } from '../../services/platform-api.service';
import { DashboardActions } from './dashboard.actions';
import { AuthActions } from '../auth/auth.actions';

@Injectable()
export class DashboardEffects {
  private readonly actions$ = inject(Actions);
  private readonly api = inject(PlatformApiService);

  readonly loadDashboard$ = createEffect(() =>
    this.actions$.pipe(
      ofType(DashboardActions.loadRequested, AuthActions.loginSucceeded),
      switchMap(() =>
        this.api.dashboard().pipe(
          map((dashboard) => DashboardActions.loadSucceeded({ dashboard })),
          catchError((error) => of(DashboardActions.loadFailed({ error: error?.error?.message ?? 'Unable to load your dashboard.' })))
        )
      )
    )
  );

  readonly createAssignment$ = createEffect(() =>
    this.actions$.pipe(
      ofType(DashboardActions.createAssignmentRequested),
      concatMap(({ payload }) =>
        this.api.createAssignment(payload).pipe(
          mergeMap(() => [
            DashboardActions.mutationSucceeded({ notice: 'Assignment published for the selected class.' }),
            DashboardActions.loadRequested()
          ]),
          catchError((error) => of(DashboardActions.mutationFailed({ error: error?.error?.message ?? 'Assignment could not be created.' })))
        )
      )
    )
  );

  readonly submitAssignment$ = createEffect(() =>
    this.actions$.pipe(
      ofType(DashboardActions.submitAssignmentRequested),
      concatMap(({ assignmentId, content }) =>
        this.api.submitAssignment(assignmentId, content).pipe(
          mergeMap(() => [
            DashboardActions.mutationSucceeded({ notice: 'Submission sent to your teacher.' }),
            DashboardActions.loadRequested()
          ]),
          catchError((error) => of(DashboardActions.mutationFailed({ error: error?.error?.message ?? 'Submission failed.' })))
        )
      )
    )
  );

  readonly gradeSubmission$ = createEffect(() =>
    this.actions$.pipe(
      ofType(DashboardActions.gradeSubmissionRequested),
      concatMap(({ submissionId, payload }) =>
        this.api.gradeSubmission(submissionId, payload).pipe(
          mergeMap(() => [
            DashboardActions.mutationSucceeded({ notice: 'Feedback has been returned to the learner.' }),
            DashboardActions.loadRequested()
          ]),
          catchError((error) => of(DashboardActions.mutationFailed({ error: error?.error?.message ?? 'Grading update failed.' })))
        )
      )
    )
  );

  readonly recordAttendance$ = createEffect(() =>
    this.actions$.pipe(
      ofType(DashboardActions.recordAttendanceRequested),
      concatMap(({ classId, payload }) =>
        this.api.recordAttendance(classId, payload).pipe(
          mergeMap(() => [
            DashboardActions.mutationSucceeded({ notice: 'Attendance register saved and alerts are now visible to families.' }),
            DashboardActions.loadRequested()
          ]),
          catchError((error) => of(DashboardActions.mutationFailed({ error: error?.error?.message ?? 'Attendance could not be saved.' })))
        )
      )
    )
  );

  readonly createAnnouncement$ = createEffect(() =>
    this.actions$.pipe(
      ofType(DashboardActions.createAnnouncementRequested),
      concatMap(({ payload }) =>
        this.api.createAnnouncement(payload).pipe(
          mergeMap(() => [
            DashboardActions.mutationSucceeded({ notice: 'Announcement sent to the whole class.' }),
            DashboardActions.loadRequested()
          ]),
          catchError((error) => of(DashboardActions.mutationFailed({ error: error?.error?.message ?? 'Announcement could not be sent.' })))
        )
      )
    )
  );

  readonly createMessage$ = createEffect(() =>
    this.actions$.pipe(
      ofType(DashboardActions.createMessageRequested),
      concatMap(({ payload }) =>
        this.api.createMessage(payload).pipe(
          mergeMap(() => [
            DashboardActions.mutationSucceeded({ notice: 'Message sent to the selected parent.' }),
            DashboardActions.loadRequested()
          ]),
          catchError((error) => of(DashboardActions.mutationFailed({ error: error?.error?.message ?? 'Message could not be delivered.' })))
        )
      )
    )
  );
}
