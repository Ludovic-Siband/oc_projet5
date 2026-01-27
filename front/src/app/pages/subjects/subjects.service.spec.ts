import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SubjectsService } from './subjects.service';
import { environment } from '../../../environments/environment';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';
import { lastValueFrom } from 'rxjs';


describe('SubjectsService', () => {
  let service: SubjectsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SubjectsService],
    });

    service = TestBed.inject(SubjectsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('lists subjects', () => {
    service.listSubjects().subscribe();

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/subjects`);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('maps conflict errors on subscribe', async () => {
    const request$ = service.subscribe(1);
    const promise = lastValueFrom(request$).catch((error) => error);

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/subjects/1/subscribe`);
    request.flush({ message: 'Conflict' }, { status: 409, statusText: 'Conflict' });

    await expect(promise).resolves.toMatchObject({
      kind: 'conflict',
    });
  });

  it('maps not found errors on subscribe', async () => {
    const request$ = service.subscribe(2);
    const promise = lastValueFrom(request$).catch((error) => error);

    const request = httpMock.expectOne(`${environment.apiBaseUrl}/api/subjects/2/subscribe`);
    request.flush({ message: 'Not found' }, { status: 404, statusText: 'Not Found' });

    await expect(promise).resolves.toMatchObject({
      kind: 'not-found',
    });
  });
});
