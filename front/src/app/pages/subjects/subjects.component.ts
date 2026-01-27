import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { SubscribeError, SubjectItem } from './subjects.model';
import { SubjectsService } from './subjects.service';

@Component({
  selector: 'app-subjects',
  standalone: true,
  imports: [MatButtonModule, MatCardModule, MatSnackBarModule],
  templateUrl: './subjects.component.html',
  styleUrls: ['./subjects.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubjectsComponent implements OnInit {
  private readonly subjectsService = inject(SubjectsService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  readonly subjects = signal<SubjectItem[]>([]);
  readonly loading = signal(false);
  readonly subscribingIds = signal<Set<number>>(new Set());

  ngOnInit(): void {
    this.loadSubjects();
  }

  loadSubjects(): void {
    this.loading.set(true);
    this.subjectsService
      .listSubjects()
      .pipe(
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (subjects) => this.subjects.set(subjects),
        error: (error) => this.handleError(error),
      });
  }

  subscribe(subject: SubjectItem): void {
    if (subject.subscribed || this.subscribingIds().has(subject.id)) {
      return;
    }

    const nextSet = new Set(this.subscribingIds());
    nextSet.add(subject.id);
    this.subscribingIds.set(nextSet);

    this.subjectsService
      .subscribe(subject.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.subjects.update((items) =>
            items.map((item) => (item.id === subject.id ? { ...item, subscribed: true } : item)),
          );
          this.clearSubscribing(subject.id);
        },
        error: (error: SubscribeError) => {
          this.clearSubscribing(subject.id);
          this.handleSubscribeError(error);
        },
      });
  }

  isSubscribing(subjectId: number): boolean {
    return this.subscribingIds().has(subjectId);
  }

  private clearSubscribing(subjectId: number): void {
    const nextSet = new Set(this.subscribingIds());
    nextSet.delete(subjectId);
    this.subscribingIds.set(nextSet);
  }

  private handleSubscribeError(error: SubscribeError): void {
    if (error.message) {
      this.snackBar.open(error.message, 'Fermer', { duration: 4000 });
    }
  }

  private handleError(error: { error?: { message?: string } }): void {
    const message = error?.error?.message ?? 'Une erreur est survenue';
    this.snackBar.open(message, 'Fermer', { duration: 4000 });
  }
}
