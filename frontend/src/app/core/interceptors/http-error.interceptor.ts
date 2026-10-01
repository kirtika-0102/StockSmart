import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const message = typeof error.error === 'object' && error.error?.message
        ? error.error.message
        : error.message;
      console.error('API error', error.status, message);
      return throwError(() => error);
    })
  );
};
