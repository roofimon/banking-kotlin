import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { SessionService } from '../../services/session.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  constructor(public session: SessionService, private router: Router) {}

  logout(): void {
    this.session.logout();
    this.router.navigate(['/login']);
  }
}
