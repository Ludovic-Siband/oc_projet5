import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Router, ActivatedRoute, convertToParamMap } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { PostDetailComponent } from './post-detail.component';
import { PostDetailService } from './post-detail.service';
import { describe, beforeEach, it, expect, vi } from 'vitest';


describe('PostDetailComponent', () => {
  let postService: { getPost: ReturnType<typeof vi.fn>; addComment: ReturnType<typeof vi.fn> };
  let snackBar: { open: ReturnType<typeof vi.fn> };

  const postResponse = {
    id: 5,
    subject: { id: 1, name: 'Subject' },
    title: 'Title',
    content: 'Body',
    author: 'Author',
    createdAt: '2024-01-01',
    comments: [{ id: 1, content: 'Hi', author: 'A', createdAt: '2024-01-02' }],
  };

  const setup = async (id: string) => {
    TestBed.resetTestingModule();
    postService = { getPost: vi.fn(), addComment: vi.fn() };
    snackBar = { open: vi.fn() };

    TestBed.overrideComponent(PostDetailComponent, {
      set: { providers: [{ provide: MatSnackBar, useValue: snackBar }] },
    });

    await TestBed.configureTestingModule({
      imports: [PostDetailComponent, RouterTestingModule],
      providers: [
        provideNoopAnimations(),
        { provide: PostDetailService, useValue: postService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: convertToParamMap({ id }) },
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(PostDetailComponent);
    const component = fixture.componentInstance;
    const router = TestBed.inject(Router);

    return { fixture, component, router };
  };

  it('redirects when id is invalid', async () => {
    const { fixture, router } = await setup('bad');
    const navSpy = vi.spyOn(router, 'navigateByUrl');

    fixture.detectChanges();

    expect(navSpy).toHaveBeenCalledWith('/feed');
  });

  it('loads post details on init', async () => {
    const { fixture, component } = await setup('5');
    postService.getPost.mockReturnValue(of(postResponse));

    fixture.detectChanges();

    expect(component.post()?.id).toBe(5);
    expect(component.comments().length).toBe(1);
    expect(postService.getPost).toHaveBeenCalledWith(5);
  });

  it('submits a comment and reloads', async () => {
    const { fixture, component } = await setup('5');
    postService.getPost.mockReturnValue(of(postResponse));
    postService.addComment.mockReturnValue(of(undefined));

    fixture.detectChanges();

    const loadSpy = vi.spyOn(component, 'loadPost');
    component.commentForm.setValue({ content: 'Hello' });
    component.submitComment();

    expect(postService.addComment).toHaveBeenCalledWith(5, { content: 'Hello' });
    expect(loadSpy).toHaveBeenCalledWith(5);
  });

  it('shows snackbar on load errors', async () => {
    const { fixture } = await setup('5');
    postService.getPost.mockReturnValue(throwError(() => ({ error: { message: 'Boom' } })));

    fixture.detectChanges();

    fixture.detectChanges();
    expect(snackBar.open).toHaveBeenCalledWith('Boom', 'Fermer', { duration: 4000 });
  });
});
