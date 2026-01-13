import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-auth-header',
  standalone: true,
  templateUrl: './auth-header.component.html',
  styleUrls: ['./auth-header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthHeaderComponent {}
