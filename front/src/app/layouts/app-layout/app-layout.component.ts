import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AppHeaderComponent } from '../../shared/header/app-header.component';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, AppHeaderComponent],
  templateUrl: './app-layout.component.html',
  styleUrls: ['./app-layout.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppLayoutComponent {}
