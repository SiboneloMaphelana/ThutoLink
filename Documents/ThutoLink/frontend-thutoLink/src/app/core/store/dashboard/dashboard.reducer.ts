import { createReducer, on } from '@ngrx/store';
import { DashboardResponse } from '../../models/platform.models';
import { AuthActions } from '../auth/auth.actions';
import { DashboardActions } from './dashboard.actions';

export interface DashboardState {
  dashboard: DashboardResponse | null;
  loading: boolean;
  error: string;
  notice: string;
}

export const initialDashboardState: DashboardState = {
  dashboard: null,
  loading: false,
  error: '',
  notice: ''
};

export const dashboardReducer = createReducer(
  initialDashboardState,
  on(DashboardActions.loadRequested, (state) => ({ ...state, loading: true, error: '' })),
  on(DashboardActions.loadSucceeded, (state, { dashboard }) => ({ ...state, dashboard, loading: false, error: '' })),
  on(DashboardActions.loadFailed, (state, { error }) => ({ ...state, loading: false, error })),
  on(DashboardActions.clearNotice, (state) => ({ ...state, notice: '' })),
  on(DashboardActions.mutationStarted, (state) => ({ ...state, error: '', notice: '' })),
  on(DashboardActions.mutationSucceeded, (state, { notice }) => ({ ...state, notice, error: '' })),
  on(DashboardActions.mutationFailed, (state, { error }) => ({ ...state, error })),
  on(AuthActions.logoutCompleted, () => initialDashboardState)
);
