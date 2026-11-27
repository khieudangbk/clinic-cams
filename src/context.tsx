import type { Store } from './interfaces/app/Store';
import type { SessionResponse } from './interfaces/account/SessionResponse';
import type { OkResponse } from './interfaces/common/OkResponse';
import { createContext, useContext, useEffect, useState, useCallback, type ReactNode } from 'react';
import { api, setCsrf, ApiError } from './api';
import type { PublicCatalogResponse } from './interfaces/catalog/PublicCatalogResponse';
import type { User } from './interfaces/account/User';
const Context = createContext<Store>(null!);
export const useApp = () => useContext(Context);
export function AppProvider({ children }: { children: ReactNode }) {
  const [catalog, setCatalog] = useState<PublicCatalogResponse | null>(null),
    [user, setUser] = useState<User | null>(null),
    [settings, setSettings] = useState<Record<string, number>>({}),
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
    const c = await api<PublicCatalogResponse>('/api/public/catalog');
    setCatalog(c);
    try {
      const s = await api<SessionResponse>('/api/session');
      setUser(s.user);
      setCsrf(s.csrf);
      setSettings(s.settings);
    } catch (e) {
      if (e instanceof ApiError && e.status === 401) {
        setUser(null);
        setCsrf('');
      } else throw e;
    }
  }, []);
  useEffect(() => {
    refresh()
      .catch((e) => setError(e.message))
      .finally(() => setReady(true));
  }, [refresh]);
  const logout = async () => {
    await api<OkResponse>('/api/logout', 'POST', {});
    setUser(null);
    setCsrf('');
  };
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
