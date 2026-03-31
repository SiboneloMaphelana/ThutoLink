import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, of, switchMap, tap } from 'rxjs';
import { PlatformApiService } from '../../services/platform-api.service';
import { AuthStateService } from '../../services/auth-state.service';
import { AuthActions } from './auth.actions';

@Injectable()
export class AuthEffects {
  private readonly actions$ = inject(Actions);
  private readonly api = inject(PlatformApiService);
  private readonly storage = inject(AuthStateService);
  private readonly router = inject(Router);

  readonly login$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.loginRequested),
      switchMap(({ email, password }) =>
        this.api.login(email, password).pipe(
          map((session) => AuthActions.loginSucceeded({ session })),
          catchError((error) => of(AuthActions.loginFailed({ error: error?.error?.message ?? 'Unable to sign in right now.' })))
        )
      )
    )
  );

  readonly persistLogin$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.loginSucceeded),
        tap(({ session }) => {
          this.storage.setSession(session);
          void this.router.navigateByUrl('/dashboard');
        })
      ),
    { dispatch: false }
  );

  readonly logout$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.logoutRequested),
      map(() => {
        this.storage.clearSession();
        void this.router.navigateByUrl('/login');
        return AuthActions.logoutCompleted();
      })
    )
  );
}
