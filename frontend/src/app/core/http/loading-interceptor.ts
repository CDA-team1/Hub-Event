import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';
import { Loading } from './loading';

export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  const loading = inject(Loading);

  loading.start();

  return next(req).pipe(finalize(() => loading.stop()));
};
