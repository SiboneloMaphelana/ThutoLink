import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { LoginResponse } from '../../models/platform.models';

export const AuthActions = createActionGroup({
  source: 'Auth',
  events: {
    'Login Requested': props<{ email: string; password: string }>(),
    'Login Succeeded': props<{ session: LoginResponse }>(),
    'Login Failed': props<{ error: string }>(),
    'Logout Requested': emptyProps(),
    'Logout Completed': emptyProps()
  }
});
