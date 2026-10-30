import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }
  const roles = route.data['roles'] as string[] | undefined;
  if (!roles || roles.length === 0 || roles.some((role) => auth.hasRole(role))) {
    return true;
  }
  return router.createUrlTree(['/']);
};
