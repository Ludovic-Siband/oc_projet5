import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { FeedComponent } from './feed.component';
import { FeedService } from './feed.service';
import { RouterTestingModule } from '@angular/router/testing';
import { describe, beforeEach, it, expect, vi } from 'vitest';


describe('FeedComponent', () => {
  let fixture: ComponentFixture<FeedComponent>;
  let component: FeedComponent;
  let feedService: { getFeed: ReturnType<typeof vi.fn> };
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    feedService = { getFeed: vi.fn() };
    snackBar = { open: vi.fn() };

    TestBed.overrideComponent(FeedComponent, {
      set: { providers: [{ provide: MatSnackBar, useValue: snackBar }] },
    });

    await TestBed.configureTestingModule({
      imports: [FeedComponent, RouterTestingModule],
      providers: [
        provideNoopAnimations(),
        { provide: FeedService, useValue: feedService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedComponent);
    component = fixture.componentInstance;
  });

  it('loads feed on init', () => {
    feedService.getFeed.mockReturnValue(
      of([{ id: 1, subjectId: 1, author: 'a', title: 't', content: 'c', createdAt: '2024-01-01T10:00:00Z' }]),
    );

    fixture.detectChanges();

    expect(component.posts().length).toBe(1);
    expect(feedService.getFeed).toHaveBeenCalledWith('desc');
  });

  it('shows snackbar on errors', () => {
    feedService.getFeed.mockReturnValue(throwError(() => ({ error: { message: 'Boom' } })));

    fixture.detectChanges();

    fixture.detectChanges();
    expect(snackBar.open).toHaveBeenCalledWith('Boom', 'Fermer', { duration: 4000 });
  });

  it('toggles sort and reloads', () => {
    feedService.getFeed.mockReturnValue(of([]));
    const loadSpy = vi.spyOn(component, 'loadFeed');

    component.toggleSort();

    expect(component.sort()).toBe('asc');
    expect(loadSpy).toHaveBeenCalled();
  });
});
