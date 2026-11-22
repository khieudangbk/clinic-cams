import type { ErrorResponse } from './interfaces/common/ErrorResponse';

let csrf = '';
export const setCsrf = (value: string) => {
  csrf = value;
};
export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public code: string,
  ) {
    super(message);
  }
}
export async function api<T = any, TRequest = unknown>(
  path: string,
  method = 'GET',
  data?: TRequest,
  signal?: AbortSignal,
): Promise<T> {
  const response = await fetch(path, {
    method,
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', 'X-CSRF-Token': csrf },
    body: data === undefined ? undefined : JSON.stringify(data),
    signal,
  });
  const json: unknown = await response.json();
  if (!response.ok) {
    const error: Partial<ErrorResponse> = json && typeof json === 'object' ? json : {};
    throw new ApiError(
      typeof error.error === 'string' && error.error
        ? error.error
        : 'Không thực hiện được thao tác.',
      response.status,
      typeof error.code === 'string' ? error.code : '',
    );
  }
  return json as T;
}
