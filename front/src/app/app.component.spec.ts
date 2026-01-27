import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Title } from '@angular/platform-browser';
import { AppComponent } from './app.component';
import { describe, beforeEach, it, expect, vi } from 'vitest';


describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;
  let title: Title;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    title = TestBed.inject(Title);
  });

  it('sets the document title on init', () => {
    const spy = vi.spyOn(title, 'setTitle');

    fixture.detectChanges();

    expect(spy).toHaveBeenCalledWith('MDD');
  });
});
