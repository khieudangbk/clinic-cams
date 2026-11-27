import type { CreateStaffUserRequest } from './CreateStaffUserRequest';
import type { UpdateStaffUserRequest } from './UpdateStaffUserRequest';
import type { UpsertSpecialtyRequest } from './UpsertSpecialtyRequest';
import type { UpdateSettingsRequest } from './UpdateSettingsRequest';

export interface AdminRequests {
  '/api/users': CreateStaffUserRequest;
  [path: `/api/users/${number}`]: UpdateStaffUserRequest;
  '/api/specialties': UpsertSpecialtyRequest;
  '/api/settings': UpdateSettingsRequest;
}
