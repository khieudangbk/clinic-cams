import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Search } from 'lucide-react';
import { useApp } from '../context';
export default function Home() {
  const { catalog } = useApp();
  const [query, setQuery] = useState('');
  const nav = useNavigate();
  if (!catalog) return null;
  return (
    <>
      <section className="hero-band">
        <div className="container">
          <h1>
            Tìm kiếm bác sĩ, phòng khám, dịch vụ y tế
            <br className="desktop-break" /> và đặt lịch khám
          </h1>
          <form
            className="hero-search"
            role="search"
            onSubmit={(e) => {
              e.preventDefault();
              nav('/tim-kiem?q=' + encodeURIComponent(query));
            }}
          >
            <label className="sr-only" htmlFor="hero-query">
              Tìm bác sĩ, chuyên khoa
            </label>
            <input
              id="hero-query"
              placeholder="Tìm bác sĩ, chuyên khoa, phòng khám…"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              maxLength={100}
            />
            <button aria-label="Tìm kiếm">
              <Search />
            </button>
          </form>
          <h2 className="assistant-title">Tra cứu & hỗ trợ đặt khám</h2>
          <Link to="/tro-ly" className="ai-card">
            <img src="/media/assistant.png" alt="" />
            <div>
              <h3>Trợ lý Đi khám</h3>
              <p>Tìm kiếm thông tin bác sĩ, nơi khám và đặt lịch khám</p>
            </div>
          </Link>
        </div>
      </section>
    </>
  );
}
