import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { SubjectsComponent } from './subjects.component';
import { SubjectsService } from './subjects.service';
import { describe, beforeEach, it, expect, vi } from 'vitest';


describe('SubjectsComponent', () => {
  let fixture: ComponentFixture<SubjectsComponent>;
  let component: SubjectsComponent;
  let subjectsService: { listSubjects: ReturnType<typeof vi.fn>; subscribe: ReturnType<typeof vi.fn> };
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    subjectsService = { listSubjects: vi.fn(), subscribe: vi.fn() };
    snackBar = { open: vi.fn() };

    TestBed.overrideComponent(SubjectsComponent, {
      set: { providers: [{ provide: MatSnackBar, useValue: snackBar }] },
    });

    await TestBed.configureTestingModule({
      imports: [SubjectsComponent],
      providers: [
        provideNoopAnimations(),
        { provide: SubjectsService, useValue: subjectsService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SubjectsComponent);
    component = fixture.componentInstance;
  });

  it('loads subjects on init', () => {
    subjectsService.listSubjects.mockReturnValue(of([{ id: 1, name: 'A', description: 'd', subscribed: false }]));

    fixture.detectChanges();

    expect(component.subjects().length).toBe(1);
    expect(subjectsService.listSubjects).toHaveBeenCalled();
  });

  it('subscribes to a subject', () => {
    const subject = { id: 1, name: 'A', description: 'd', subscribed: false };
    subjectsService.listSubjects.mockReturnValue(of([subject]));
    subjectsService.subscribe.mockReturnValue(of(undefined));

    fixture.detectChanges();

    component.subscribe(subject);

    expect(subjectsService.subscribe).toHaveBeenCalledWith(1);
    expect(component.isSubscribing(1)).toBe(false);
    expect(component.subjects()[0].subscribed).toBe(true);
  });

  it('shows snackbar on load errors', () => {
    subjectsService.listSubjects.mockReturnValue(throwError(() => ({ error: { message: 'Boom' } })));

    fixture.detectChanges();

    fixture.detectChanges();
    expect(snackBar.open).toHaveBeenCalledWith('Boom', 'Fermer', { duration: 4000 });
  });
});
