import { BrowserModule } from '@angular/platform-browser';
import { NgModule, Injectable } from '@angular/core';
import { HttpClientModule, HttpInterceptor, HttpHandler, HttpRequest, HTTP_INTERCEPTORS, HttpHeaders } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { environment }           from '../environments/environment';
import { AppRoutingModule }      from './modules/app-routing.module';
import { AuthService }           from './services/auth.service';
import { UsersService }           from './services/users.service';
import { LoggerService }         from './services/logger.service';
import { AppComponent }          from './app.component';
import { LoginComponent }        from './components/login/login.component';
import { RegistrationComponent } from './components/registration/registration.component';
import { HelloComponent }        from './components/hello/hello.component';
import { HomeComponent }         from './components/home/home.component';
import { EmailVerificationComponent } from './components/email-verification/email-verification.component';
import { CreateEventComponent }   from './components/createEvent/createEvent.component'
import { EventService } from "./services/event.service";

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler) {
    req = req.clone({
        setHeaders: {
          Authorization: sessionStorage.getItem('authToken') 
        }
    });
    return next.handle(req);
  }
}

@Injectable()
export class AddressInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler) {
    req = req.clone({
      url: environment.base_url + req.url
    });
    return next.handle(req);
  }
}

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegistrationComponent,
    HelloComponent,
    HomeComponent,
    EmailVerificationComponent,
    CreateEventComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [
    AuthService,
    EventService,
    UsersService,
    LoggerService,
    { provide: HTTP_INTERCEPTORS,
      useClass: AddressInterceptor,
      multi: true},
    { provide: HTTP_INTERCEPTORS,
      useClass: AuthenticationInterceptor,
      multi: true}
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
