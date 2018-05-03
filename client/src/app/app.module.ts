import { BrowserModule } from '@angular/platform-browser';
import { NgModule, Injectable } from '@angular/core';
import { HttpClientModule, HttpInterceptor, HttpHandler, HttpRequest, HTTP_INTERCEPTORS, HttpHeaders } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Ng2CloudinaryModule } from 'ng2-cloudinary';
import { FileUploadModule } from 'ng2-file-upload';

import { environment }           from '../environments/environment';
import { AppRoutingModule }      from './modules/app-routing.module';
import { AuthService }           from './services/auth.service';
import { UserService }           from './services/user.service';
import { LoggerService }         from './services/logger.service';
import { AppComponent }          from './app.component';
import { LoginComponent }        from './components/login/login.component';
import { RegistrationComponent } from './components/registration/registration.component';
import { HelloComponent }        from './components/hello/hello.component';
import { HomeComponent }         from './components/home/home.component';
import { EmailVerificationComponent } from './components/email-verification/email-verification.component';
import { CreateEventComponent }   from './components/createEvent/createEvent.component';
import { ViewEventComponent }   from './components/viewEvent/viewEvent.component';
import { EventEditComponent } from './components/event-edit/event-edit.component';
import { EventService } from "./services/event.service";
import { UserComponent } from './components/user/user.component';
import { UserListComponent } from './components/user/user-list.component';
import { UserEditComponent } from './components/user/user-edit.component';
import { EventListComponent } from './components/event-list/event-list.component';
import { UserEditImageComponent } from './components/user/user-edit-image.component';
import { UserSearchComponent } from './components/user/user-search.component';
import {ImageUploaderService} from "./services/image-uploader.service";
import {WishListComponent} from './components/wishlist/wishlist.component';
import {WishListService} from './services/wishlist.service';
import {CreateItemComponent} from './components/wishlist/item/create-item/create-item.component';
import {ItemService} from './services/item.service';
import { ViewItemComponent } from './components/wishlist/item/view-item/view-item.component';
import { ChatComponent } from './components/chat/chat.component';
import { ExportEventsPlanComponent } from './components/export-events-plan/export-events-plan.component';
import { UserEditPasswordComponent } from './components/user/user-edit-password/user-edit-password.component';

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
    CreateEventComponent,
    ViewEventComponent,
    EventEditComponent,
    EventListComponent,
    UserComponent,
    UserListComponent,
    UserEditComponent,
    UserEditImageComponent,
    UserEditPasswordComponent,
    UserSearchComponent,
    WishListComponent,
    CreateItemComponent,
    ViewItemComponent,
    ChatComponent,
    ExportEventsPlanComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    Ng2CloudinaryModule,
    FileUploadModule
  ],
  providers: [
    AuthService,
    EventService,
    UserService,
    LoggerService,
    ImageUploaderService,
    WishListService,
    ItemService,
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