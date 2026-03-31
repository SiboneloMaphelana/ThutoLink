import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import { isDevMode } from '@angular/core';

import { routes } from './app.routes';
import { authReducer } from './core/store/auth/auth.reducer';
import { dashboardReducer } from './core/store/dashboard/dashboard.reducer';
import { AuthEffects } from './core/store/auth/auth.effects';
import { DashboardEffects } from './core/store/dashboard/dashboard.effects';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(),
    provideRouter(routes),
    provideStore({
      auth: authReducer,
      dashboard: dashboardReducer
    }),
    provideEffects([AuthEffects, DashboardEffects]),
    provideStoreDevtools({
      maxAge: 25,
      logOnly: !isDevMode()
    })
  ]
};
