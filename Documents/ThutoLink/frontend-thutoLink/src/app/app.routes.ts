import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/services/auth.guards';
import { AuthPageComponent } from './features/auth/auth-page.component';
import { DashboardPageComponent } from './features/dashboard/dashboard-page.component';
import { AdminDashboardPageComponent } from './features/dashboard/admin/admin-dashboard-page.component';
import { TeacherDashboardPageComponent } from './features/dashboard/teacher/teacher-dashboard-page.component';
import { ParentDashboardPageComponent } from './features/dashboard/parent/parent-dashboard-page.component';
import { LearnerDashboardPageComponent } from './features/dashboard/learner/learner-dashboard-page.component';

export const routes: Routes = [
  {
    path: 'login',
    component: AuthPageComponent
  },
  {
    path: 'dashboard/admin',
    component: AdminDashboardPageComponent,
    canActivate: [authGuard, roleGuard('ADMIN')]
  },
  {
    path: 'dashboard/teacher',
    component: TeacherDashboardPageComponent,
    canActivate: [authGuard, roleGuard('TEACHER')]
  },
  {
    path: 'dashboard/parent',
    component: ParentDashboardPageComponent,
    canActivate: [authGuard, roleGuard('PARENT')]
  },
  {
    path: 'dashboard/learner',
    component: LearnerDashboardPageComponent,
    canActivate: [authGuard, roleGuard('LEARNER')]
  },
  {
    path: 'dashboard',
    pathMatch: 'full',
    component: DashboardPageComponent,
    canActivate: [authGuard]
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login'
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
