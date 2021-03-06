import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { JQueryStatic } from 'jquery';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

declare var $:JQueryStatic;

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  public form: FormGroup;
  public credentials = {login: '', password: ''};
  public loading = false;
  public error = false;

  constructor(private auth: AuthService, private router: Router, private formBuilder: FormBuilder) {
  }

  ngOnInit() {
    this.form = this.formBuilder.group({
      login: ['', [ Validators.required, Validators.pattern('^[a-zA-Z0-9_]*$') ]],
      password: ['', [ Validators.required, Validators.pattern('^[a-zA-Z0-9]*$') ]]
    });
  }

  login() {
    this.loading = true;
    this.error = false;

    this.auth.authenticate(this.credentials, false, () => {
        this.form.reset();
        this.credentials.password = '';
        this.credentials.login = '';
        this.error = false;
        this.loading = false;

        this.auth.current_user.subscribe(
          current_user => {
            console.log(current_user);
            $('#signInModal').modal('hide');
            this.router.navigate(['users', current_user.id]);
          });
      },
      () => {
        this.error = true,
        this.loading = false
      });
  }

  toRecover(){
    $('#signInModal').modal('hide');
    this.router.navigateByUrl('/recover/password');
  }

}
