import { type ReactNode } from 'react';
export function Loading() {
  return (
    <div className="loading" role="status">
      <span className="spinner" />
      Đang tải…
    </div>
  );
}
export function Empty({
  title = 'Chưa có dữ liệu',
  children,
}: {
  title?: string;
  children?: ReactNode;
}) {
  return (
    <div className="empty-state">
      <h2>{title}</h2>
      {children}
    </div>
  );
}
export function ErrorBox({ message, retry }: { message: string; retry?: () => void }) {
  return (
    <div className="error-box" role="alert">
      <p>{message}</p>
      {retry && <button onClick={retry}>Thử lại</button>}
    </div>
  );
}
