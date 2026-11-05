import { createContext, useContext, useEffect, useState, useCallback, type ReactNode } from 'react';
import { api } from './api';
import type { Catalog, User } from './types';
type Store = {
  catalog: Catalog | null;
  user: User | null;
  settings: Record<string, number>;
  ready: boolean;
  error: string;
  refresh: () => Promise<void>;
  logout: () => Promise<void>;
  notify: (text: string, error?: boolean) => void;
};
const Context = createContext<Store>(null!);
export const useApp = () => useContext(Context);
export function AppProvider({ children }: { children: ReactNode }) {
  const [catalog, setCatalog] = useState<Catalog | null>(null),
    [user] = useState<User | null>(null),
    [settings] = useState({}),
    [ready, setReady] = useState(false),
    [error, setError] = useState(''),
    [toast, setToast] = useState({ text: '', error: false });
  const notify = useCallback((text: string, error = false) => setToast({ text, error }), []);
  useEffect(() => {
    if (toast.text) {
      const id = setTimeout(() => setToast({ text: '', error: false }), 6000);
      return () => clearTimeout(id);
    }
  }, [toast]);
  const refresh = useCallback(async () => {
    const c = await api<Catalog>('/api/public/catalog');
    setCatalog(c);
  }, []);
  useEffect(() => {
    refresh()
      .catch((e) => setError(e.message))
      .finally(() => setReady(true));
  }, [refresh]);
  const logout = async () => {};
  return (
    <Context.Provider value={{ catalog, user, settings, ready, error, refresh, logout, notify }}>
      {children}
      {toast.text && (
        <div
          className={'toast ' + (toast.error ? 'error' : '')}
          role={toast.error ? 'alert' : 'status'}
        >
          {toast.text}
          <button aria-label="Đóng thông báo" onClick={() => setToast({ text: '', error: false })}>
            ×
          </button>
        </div>
      )}
    </Context.Provider>
  );
}
