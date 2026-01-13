import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, filter, Observable, switchMap, take, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

const LOGIN_ROUTE = '/login';
const refreshSubject = new BehaviorSubject<string | null>(null);
let isRefreshing = false;

export const authInterceptor: HttpInterceptorFn = (
  request: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const path = extractPath(request.url);

  if (!isApiPath(path)) {
    return next(request);
  }

  const isAuthEndpoint = isAuthPath(path);
  let cloned = request.clone({ withCredentials: true });

  if (!isAuthEndpoint) {
    const token = authService.getAccessToken();
    if (token) {
      cloned = cloned.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    }
  }

  return next(cloned).pipe(
    catchError((error: HttpErrorResponse) => {
      if (!shouldAttemptRefresh(error, path)) {
        return throwError(() => error);
      }

      if (!isRefreshing) {
        isRefreshing = true;
        refreshSubject.next(null);
        return authService.refreshAccessToken().pipe(
          switchMap((token) => {
            isRefreshing = false;
            refreshSubject.next(token);
            const retry = addBearerToken(request, token);
            return next(retry);
          }),
          catchError((refreshError) => {
            isRefreshing = false;
            authService.clearAccessToken();
            refreshSubject.next(null);
            router.navigateByUrl(LOGIN_ROUTE);
            return throwError(() => refreshError);
          }),
        );
      }

      return refreshSubject.pipe(
        filter((token): token is string => token !== null),
        take(1),
        switchMap((token) => next(addBearerToken(request, token))),
      );
    }),
  );
};

function addBearerToken(request: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return request.clone({
    withCredentials: true,
    setHeaders: { Authorization: `Bearer ${token}` },
  });
}

function shouldAttemptRefresh(error: HttpErrorResponse, path: string): boolean {
  return error.status === 401 && isApiPath(path) && !isAuthPath(path);
}

function isApiPath(path: string): boolean {
  return path.startsWith('/api/');
}

function isAuthPath(path: string): boolean {
  return path.startsWith('/api/auth/');
}

function extractPath(url: string): string {
  try {
    const origin = globalThis?.location?.origin ?? 'http://localhost';
    return new URL(url, origin).pathname;
  } catch {
    return url;
  }
}
