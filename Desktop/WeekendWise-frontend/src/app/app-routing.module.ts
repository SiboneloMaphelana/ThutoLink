import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppShellComponent } from './features/layout/app-shell.component';
import { SearchPageComponent } from './features/search-page/search-page.component';
import { ResultsPageComponent } from './features/results-page/results-page.component';
import { FavoritesPageComponent } from './features/favorites-page/favorites-page.component';
import { DestinationDetailPageComponent } from './features/destination-detail-page/destination-detail-page.component';
import { LoginPageComponent } from './features/auth/login-page/login-page.component';
import { RegisterPageComponent } from './features/auth/register-page/register-page.component';
import { authGuard } from './core/guards/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: AppShellComponent,
    children: [
      { path: '', redirectTo: 'search', pathMatch: 'full' },
      { path: 'search', component: SearchPageComponent },
      { path: 'results', component: ResultsPageComponent },
      { path: 'favorites', component: FavoritesPageComponent, canActivate: [authGuard] },
      { path: 'destination/:id', component: DestinationDetailPageComponent }
    ]
  },
  { path: 'auth/login', component: LoginPageComponent },
  { path: 'auth/register', component: RegisterPageComponent },
  { path: '**', redirectTo: 'search' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
