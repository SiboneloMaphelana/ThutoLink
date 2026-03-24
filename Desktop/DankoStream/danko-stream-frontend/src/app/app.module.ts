import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';
import { CompareComponent } from './features/compare/compare.component';
import { HomeComponent } from './features/home/home.component';
import { LoginComponent } from './features/login/login.component';
import { RegisterComponent } from './features/register/register.component';
import { MovieDetailsComponent } from './features/movie-details/movie-details.component';
import { RecommendationFormComponent } from './features/recommendation-form/recommendation-form.component';
import { ResultsComponent } from './features/results/results.component';
import { WatchlistComponent } from './features/watchlist/watchlist.component';
import { MovieCardComponent } from './shared/components/movie-card/movie-card.component';
import { SectionHeaderComponent } from './shared/components/section-header/section-header.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    LoginComponent,
    RegisterComponent,
    RecommendationFormComponent,
    ResultsComponent,
    MovieDetailsComponent,
    CompareComponent,
    WatchlistComponent,
    MovieCardComponent,
    SectionHeaderComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
