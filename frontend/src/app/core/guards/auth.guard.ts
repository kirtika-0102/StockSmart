import { CanActivateFn } from '@angular/router';

/**
 * Placeholder guard. Phase 2 will redirect unauthenticated users to login.
 */
export const authGuard: CanActivateFn = () => true;
