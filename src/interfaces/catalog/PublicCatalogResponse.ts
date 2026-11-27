import type { Specialty } from './Specialty';
import type { Doctor } from './Doctor';

export interface PublicCatalogResponse {
  specialties: Specialty[];
  doctors: Doctor[];
  now: string;
  clock_mode: 'demo' | 'real';
}
