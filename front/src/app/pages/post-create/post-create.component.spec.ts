import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { PostCreateComponent } from './post-create.component';
import { PostCreateService } from './post-create.service';
import { describe, beforeEach, it, expect, vi } from 'vitest';


describe('PostCreateComponent', () => {
  let fixture: ComponentFixture<PostCreateComponent>;
  let component: PostCreateComponent;
  let postService: { getSubjects: ReturnType<typeof vi.fn>; createPost: ReturnType<typeof vi.fn> };
  let router: Router;
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    postService = { getSubjects: vi.fn(), createPost: vi.fn() };
    snackBar = { open: vi.fn() };

    TestBed.overrideComponent(PostCreateComponent, {
      set: { providers: [{ provide: MatSnackBar, useValue: snackBar }] },
    });

    await TestBed.configureTestingModule({
      imports: [PostCreateComponent, RouterTestingModule],
      providers: [
        provideNoopAnimations(),
        { provide: PostCreateService, useValue: postService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PostCreateComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
  });

  it('loads subjects on init', () => {
    postService.getSubjects.mockReturnValue(of([{ id: 1, name: 'A' }]));

    fixture.detectChanges();

    expect(component.subjects().length).toBe(1);
    expect(postService.getSubjects).toHaveBeenCalled();
  });

  it('submits a post and navigates to detail', () => {
    postService.getSubjects.mockReturnValue(of([]));
    postService.createPost.mockReturnValue(of({ id: 12 }));
    const navSpy = vi.spyOn(router, 'navigateByUrl');

    fixture.detectChanges();

    component.form.setValue({ subjectId: 1, title: 'Title', content: 'Body' });
    component.submit();

    expect(postService.createPost).toHaveBeenCalledWith({ subjectId: 1, title: 'Title', content: 'Body' });
    expect(navSpy).toHaveBeenCalledWith('/feed/posts/12');
  });

  it('shows snackbar on load errors', () => {
    postService.getSubjects.mockReturnValue(throwError(() => ({ error: { message: 'Boom' } })));

    fixture.detectChanges();

    fixture.detectChanges();
    expect(snackBar.open).toHaveBeenCalledWith('Boom', 'Fermer', { duration: 4000 });
  });

  it('applies server validation errors to fields', () => {
    postService.getSubjects.mockReturnValue(of([]));
    postService.createPost.mockReturnValue(
      throwError(() => ({ kind: 'validation', message: 'Invalid', fields: { title: 'required' } })),
    );

    fixture.detectChanges();

    component.form.setValue({ subjectId: 1, title: 'Title', content: 'Body' });
    component.submit();

    expect(component.form.get('title')?.hasError('server')).toBe(true);
  });
});
