import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, finalize, of } from 'rxjs';
import { ApiError } from '../../shared/models/api-error.model';
import { FeedPost, FeedSort } from './feed.model';
import { FeedService } from './feed.service';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [DatePipe, MatButtonModule, MatCardModule, MatIconModule, MatSnackBarModule, RouterLink],
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedComponent implements OnInit {
  private readonly feedService = inject(FeedService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  readonly posts = signal<FeedPost[]>([]);
  readonly loading = signal(false);
  readonly sort = signal<FeedSort>('desc');

  ngOnInit(): void {
    this.loadFeed();
  }

  loadFeed(): void {
    this.loading.set(true);
    this.feedService
      .getFeed(this.sort())
      .pipe(
        catchError((error) => {
          this.handleError(error);
          return of([]);
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((posts) => this.posts.set(posts));
  }

  toggleSort(): void {
    this.sort.set(this.sort() === 'desc' ? 'asc' : 'desc');
    this.loadFeed();
  }

  private handleError(error: { error?: ApiError }): void {
    const message = error?.error?.message ?? 'Une erreur est survenue';
    this.snackBar.open(message, 'Fermer', { duration: 4000 });
  }
}
