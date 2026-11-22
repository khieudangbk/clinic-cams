import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

export function Banner() {
  const [slide, setSlide] = useState(0),
    [pause, setPause] = useState(false);
  useEffect(() => {
    if (pause || matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    const timer = setInterval(() => setSlide((x) => (x + 1) % 3), 6500);
    return () => clearInterval(timer);
  }, [pause]);
  return (
    <div
      className="promotion-carousel"
      onMouseEnter={() => setPause(true)}
      onMouseLeave={() => setPause(false)}
      onFocusCapture={() => setPause(true)}
      onBlurCapture={() => setPause(false)}
    >
      <Link to="/bac-si" aria-label="Đặt khám tại CAMS">
        <img
          src={`/media/banner-${slide}.svg`}
          alt={
            [
              'Chủ động lịch khám — Đặt khám ngay',
              'Theo dõi lịch hẹn trong tài khoản',
              'CAMS — Khám theo chuyên khoa',
            ][slide]
          }
        />
      </Link>
      <div className="carousel-dots">
        {[0, 1, 2].map((n) => (
          <button
            key={n}
            aria-label={`Xem banner ${n + 1}`}
            aria-pressed={slide === n}
            className={slide === n ? 'active' : ''}
            onClick={() => setSlide(n)}
          />
        ))}
      </div>
    </div>
  );
}
