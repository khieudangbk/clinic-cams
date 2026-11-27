import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Search } from 'lucide-react';
import { useApp } from '../../context';
import { normalize } from '../../display';
import { Breadcrumb, Empty } from '../../components/UI';
import { DoctorCard } from '../../components/Cards';

export function DoctorList() {
  const { catalog } = useApp();
  const [params, setParams] = useSearchParams();
  const [query, setQuery] = useState(params.get('q') || '');
  const specialty = params.get('specialty') || '';
  const doctors = catalog!.doctors.filter(
    (d) =>
      (!specialty || String(d.specialty_id) === specialty) &&
      normalize(d.name + ' ' + d.specialty_name).includes(normalize(params.get('q') || '')),
  );
  return (
    <div className="container page">
      <Breadcrumb items={[{ label: 'Tìm bác sĩ' }]} />
      <h1>Tìm kiếm bác sĩ, chuyên khoa</h1>
      <form
        className="search-filters"
        onSubmit={(e) => {
          e.preventDefault();
          setParams({ q: query, ...(specialty ? { specialty } : {}) });
        }}
      >
        <label>
          <span className="sr-only">Tìm kiếm</span>
          <input
            placeholder="Tên bác sĩ hoặc chuyên khoa…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </label>
        <label>
          <span className="sr-only">Chuyên khoa</span>
          <select
            value={specialty}
            onChange={(e) =>
              setParams({ q: query, ...(e.target.value ? { specialty: e.target.value } : {}) })
            }
          >
            <option value="">Tất cả chuyên khoa</option>
            {catalog!.specialties.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </label>
        <button className="primary">
          <Search size={18} />
          Tìm kiếm
        </button>
        <button
          type="button"
          className="outline"
          onClick={() => {
            setQuery('');
            setParams({});
          }}
        >
          Xóa bộ lọc
        </button>
      </form>
      <p className="result-count" role="status">
        Tìm thấy {doctors.length} bác sĩ
      </p>
      {doctors.length ? (
        <div className="listing-grid doctor-list">
          {doctors.map((d) => (
            <DoctorCard key={d.id} doctor={d} />
          ))}
        </div>
      ) : (
        <Empty title="Không tìm thấy bác sĩ">
          <p>Thử tên hoặc chuyên khoa khác.</p>
        </Empty>
      )}
    </div>
  );
}
