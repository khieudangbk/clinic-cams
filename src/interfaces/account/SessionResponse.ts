import type { User } from './User';

export interface SessionResponse {
  user: User;
  csrf: string;
  settings: Record<string, number>;
  now: string;
}
