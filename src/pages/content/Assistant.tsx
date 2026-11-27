import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Search } from 'lucide-react';
import { useApp } from '../../context';
import { normalize } from '../../display';
import { Breadcrumb } from '../../components/UI';

export function Assistant() {
  const { catalog } = useApp();
  const [q, setQ] = useState('');
  const nav = useNavigate();
  const doctors = catalog!.doctors.filter(
    (d) => q && normalize(d.name + ' ' + d.specialty_name).includes(normalize(q)),
  );
  return (
    <div className="container page assistant-page">
      <Breadcrumb items={[{ label: 'Trợ lý Đi khám' }]} />
      <img src="/media/assistant.png" alt="" />
      <h1>Trợ lý Đi khám</h1>
      <p>Tìm bác sĩ, chuyên khoa và hướng dẫn đặt khám trên CAMS.</p>
      <form
        className="hero-search"
        onSubmit={(e) => {
          e.preventDefault();
          nav('/tim-kiem?q=' + encodeURIComponent(q));
        }}
      >
        <label className="sr-only" htmlFor="assistant-query">
          Bạn muốn tìm gì?
        </label>
        <input
          id="assistant-query"
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Nhập tên bác sĩ hoặc chuyên khoa…"
        />
        <button aria-label="Tìm">
          <Search />
        </button>
      </form>
      <div className="assistant-suggestions">
        {catalog!.specialties.map((s) => (
          <button className="outline-pill" key={s.id} onClick={() => setQ(s.name)}>
            {s.name}
          </button>
        ))}
      </div>
      {q && (
        <div className="assistant-results">
          <p>{doctors.length} kết quả tra cứu</p>
          {doctors.map((d) => (
            <Link key={d.id} to={'/bac-si/' + d.id}>
              <strong>{d.name}</strong>
              <span>{d.specialty_name} →</span>
            </Link>
          ))}
        </div>
      )}
      <p className="muted">
        Công cụ tìm kiếm theo từ khóa, không phải tư vấn y khoa hay chẩn đoán bằng AI.
      </p>
    </div>
  );
}
