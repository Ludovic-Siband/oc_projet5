import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { PostDetail, PostComment } from './post-detail.model';
import { PostDetailService } from './post-detail.service';

@Component({
  selector: 'app-post-detail',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSnackBarModule,
    RouterLink,
  ],
  templateUrl: './post-detail.component.html',
  styleUrls: ['./post-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PostDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly postService = inject(PostDetailService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = signal(false);
  readonly posting = signal(false);
  readonly post = signal<PostDetail | null>(null);
  readonly comments = signal<PostComment[]>([]);

  readonly commentForm = this.fb.nonNullable.group({
    content: ['', [Validators.required, Validators.maxLength(1000)]],
  });

  ngOnInit(): void {
    const postId = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(postId)) {
      this.router.navigateByUrl('/feed');
      return;
    }
    this.loadPost(postId);
  }

  loadPost(postId: number): void {
    this.loading.set(true);
    this.postService
      .getPost(postId)
      .pipe(
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (post) => {
          this.post.set(post);
          this.comments.set(post.comments);
        },
        error: (error) => this.handleError(error),
      });
  }

  submitComment(): void {
    if (this.posting()) {
      return;
    }

    if (this.commentForm.invalid) {
      this.commentForm.markAllAsTouched();
      return;
    }

    const postId = this.post()?.id;
    if (!postId) {
      return;
    }

    this.posting.set(true);
    this.postService
      .addComment(postId, this.commentForm.getRawValue())
      .pipe(
        finalize(() => this.posting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.commentForm.reset();
          this.loadPost(postId);
        },
        error: (error) => this.handleError(error),
      });
  }

  private handleError(error: { error?: { message?: string } }): void {
    const message = error?.error?.message ?? 'Une erreur est survenue';
    this.snackBar.open(message, 'Fermer', { duration: 4000 });
  }
}
