import { createReducer, on } from '@ngrx/store';
import { DemoCredential, UserSummary } from '../../models/platform.models';
import { AuthActions } from './auth.actions';

const defaultDemoCredentials: DemoCredential[] = [
  { label: 'Admin', email: 'principal@thutolink.school', password: 'admin123', role: 'ADMIN' },
  { label: 'Teacher', email: 'teacher.nkosi@thutolink.school', password: 'teacher123', role: 'TEACHER' },
  { label: 'Parent', email: 'parent.dlamini@thutolink.school', password: 'parent123', role: 'PARENT' },
  { label: 'Learner', email: 'amahle@thutolink.school', password: 'learner123', role: 'LEARNER' }
];

function storedToken(): string | null {
  return localStorage.getItem('thutolink.token');
}

function storedUser(): UserSummary | null {
  const user = localStorage.getItem('thutolink.user');
  if (!user) {
    return null;
  }

  try {
    return JSON.parse(user) as UserSummary;
  } catch {
    return null;
  }
}

export interface AuthState {
  token: string | null;
  user: UserSummary | null;
  loading: boolean;
  error: string;
  demoCredentials: DemoCredential[];
}

export const initialAuthState: AuthState = {
  token: storedToken(),
  user: storedUser(),
  loading: false,
  error: '',
  demoCredentials: defaultDemoCredentials
};

export const authReducer = createReducer(
  initialAuthState,
  on(AuthActions.loginRequested, (state) => ({ ...state, loading: true, error: '' })),
  on(AuthActions.loginSucceeded, (state, { session }) => ({
    ...state,
    token: session.token,
    user: session.user,
    loading: false,
    error: '',
    demoCredentials: session.demoCredentials
  })),
  on(AuthActions.loginFailed, (state, { error }) => ({ ...state, loading: false, error })),
  on(AuthActions.logoutCompleted, () => ({
    ...initialAuthState,
    token: null,
    user: null
  }))
);
