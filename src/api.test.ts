import { afterEach, describe, it, expect, vi } from 'vitest';
import { api, setCsrf, ApiError } from './api';
afterEach(() => {
  vi.unstubAllGlobals();
  setCsrf('');
});
describe('API client', () => {
  it('sends same-origin cookies and CSRF for mutations', async () => {
    const fetch = vi.fn().mockResolvedValue(new Response('{"ok":true}'));
    vi.stubGlobal('fetch', fetch);
    setCsrf('test-token');
    await api('/api/appointments', 'POST', { slot_id: 3 });
    expect(fetch).toHaveBeenCalledWith(
      '/api/appointments',
      expect.objectContaining({
        credentials: 'same-origin',
        headers: expect.objectContaining({ 'X-CSRF-Token': 'test-token' }),
        body: '{"slot_id":3}',
      }),
    );
  });
  it('propagates business-rule error and status', async () => {
    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockResolvedValue(
          new Response('{"error":"Giờ đã được đặt","code":"conflict"}', { status: 409 }),
        ),
    );
    await expect(api('/api/appointments', 'POST', {})).rejects.toMatchObject({
      message: 'Giờ đã được đặt',
      status: 409,
      code: 'conflict',
    });
  });
  it('keeps unauthenticated errors distinguishable', async () => {
    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockResolvedValue(
          new Response('{"error":"Đăng nhập","code":"unauthorized"}', { status: 401 }),
        ),
    );
    await expect(api('/api/appointments')).rejects.toBeInstanceOf(ApiError);
  });
  it('supports cancelling outdated slot requests', async () => {
    const fetch = vi.fn().mockResolvedValue(new Response('{"slots":[]}'));
    vi.stubGlobal('fetch', fetch);
    const c = new AbortController();
    await api('/api/public/slots', 'GET', undefined, c.signal);
    expect(fetch.mock.calls[0][1].signal).toBe(c.signal);
    expect(fetch.mock.calls[0][1].body).toBeUndefined();
  });
});
