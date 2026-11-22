import type { PublicCatalogResponse } from '../catalog/PublicCatalogResponse';
import type { User } from '../account/User';

export interface Store {
  catalog: PublicCatalogResponse | null;
  user: User | null;
  settings: Record<string, number>;
  ready: boolean;
  error: string;
  refresh: () => Promise<void>;
  logout: () => Promise<void>;
  notify: (text: string, error?: boolean) => void;
}
