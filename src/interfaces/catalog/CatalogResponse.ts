import type { Specialty } from './Specialty';
import type { Doctor } from './Doctor';
import type { Schedule } from '../schedule/Schedule';

export interface CatalogResponse {
  specialties: Specialty[];
  doctors: Doctor[];
  schedules: Schedule[];
}
