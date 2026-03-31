import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { UserRole } from '../models/platform.models';
import { selectAuthUser, selectIsAuthenticated } from '../store/auth/auth.selectors';

export const authGuard: CanActivateFn = () => {
  const store = inject(Store);
  const router = inject(Router);
  return store.selectSignal(selectIsAuthenticated)() ? true : router.createUrlTree(['/login']);
};

export const guestGuard: CanActivateFn = () => {
  const store = inject(Store);
  const router = inject(Router);
  return store.selectSignal(selectIsAuthenticated)() ? router.createUrlTree(['/dashboard']) : true;
};

export const roleGuard = (role: UserRole): CanActivateFn => () => {
  const store = inject(Store);
  const router = inject(Router);
  const user = store.selectSignal(selectAuthUser)();

  if (!user) {
    return router.createUrlTree(['/login']);
  }

  return user.role === role ? true : router.createUrlTree(['/dashboard']);
};
