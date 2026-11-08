import { useEffect, useRef, useState, type ReactNode } from 'react';
import { ChevronLeft, ChevronRight, X, Home } from 'lucide-react';
import { Link } from 'react-router-dom';
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
export function Breadcrumb({ items }: { items: { label: string; to?: string }[] }) {
  return (
    <nav className="breadcrumb" aria-label="Đường dẫn">
      <Link to="/" aria-label="Trang chủ">
        <Home size={17} />
      </Link>
      {items.map((x, i) => (
        <span key={i}>
          {' '}
          / {x.to ? <Link to={x.to}>{x.label}</Link> : <span aria-current="page">{x.label}</span>}
        </span>
      ))}
    </nav>
  );
}
export function Section({
  title,
  to,
  children,
  className = '',
}: {
  title: string;
  to?: string;
  children: ReactNode;
  className?: string;
}) {
  return (
    <section className={'home-section ' + className}>
      <div className="container">
        <div className="section-heading">
          <h2>{title}</h2>
          {to && (
            <Link className="see-more" to={to}>
              Xem thêm
            </Link>
          )}
        </div>
        {children}
      </div>
    </section>
  );
}
export function Carousel({
  children,
  label,
  variant = 'media',
}: {
  children: ReactNode;
  label: string;
  variant?: string;
}) {
  const ref = useRef<HTMLDivElement>(null);
  const [pos, setPos] = useState({ left: false, right: false });
  useEffect(() => {
    const el = ref.current!;
    const update = () =>
      setPos({
        left: el.scrollLeft > 5,
        right: el.scrollLeft + el.clientWidth < el.scrollWidth - 5,
      });
    el.addEventListener('scroll', update);
    const observer = new ResizeObserver(update);
    observer.observe(el);
    update();
    return () => {
      el.removeEventListener('scroll', update);
      observer.disconnect();
    };
  }, [children]);
  return (
    <div className={'carousel-wrap ' + variant}>
      <div ref={ref} className="carousel" aria-label={label} tabIndex={0}>
        {children}
      </div>
      {pos.left && (
        <button
          aria-label={'Xem trước: ' + label}
          className="carousel-arrow prev"
          onClick={() =>
            ref.current?.scrollBy({ left: -ref.current.clientWidth, behavior: 'smooth' })
          }
        >
          <ChevronLeft size={20} />
        </button>
      )}
      {pos.right && (
        <button
          aria-label={'Xem tiếp: ' + label}
          className="carousel-arrow next"
          onClick={() =>
            ref.current?.scrollBy({ left: ref.current.clientWidth, behavior: 'smooth' })
          }
        >
          <ChevronRight size={20} />
        </button>
      )}
    </div>
  );
}
export function Modal({
  title,
  children,
  onClose,
}: {
  title: string;
  children: ReactNode;
  onClose: () => void;
}) {
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const d = ref.current!;
    d.showModal();
    return () => d.close();
  }, []);
  return (
    <dialog
      ref={ref}
      className="modal"
      aria-label={title}
      onCancel={(e) => {
        e.preventDefault();
        onClose();
      }}
      onClick={(e) => {
        if (e.target === ref.current) onClose();
      }}
    >
      <button className="modal-close" aria-label="Đóng" onClick={onClose}>
        <X size={20} />
      </button>
      <h2>{title}</h2>
      {children}
    </dialog>
  );
}
