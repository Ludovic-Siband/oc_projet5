import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { FeedService } from './feed.service';
import { environment } from '../../../environments/environment';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';


describe('FeedService', () => {
  let service: FeedService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [FeedService],
    });

    service = TestBed.inject(FeedService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('requests feed with sort parameter', () => {
    service.getFeed('desc').subscribe();

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/feed?sort=desc`);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });
});
