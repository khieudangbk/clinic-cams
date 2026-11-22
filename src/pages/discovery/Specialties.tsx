import { useApp } from '../../context';
import { Breadcrumb } from '../../components/UI';
import { SpecialtyCard } from '../../components/Cards';

export function Specialties() {
  const { catalog } = useApp();
  return (
    <div className="container page">
      <Breadcrumb items={[{ label: 'Khám Chuyên khoa' }]} />
      <h1 className="listing-heading">Khám Chuyên khoa</h1>
      <div className="listing-grid">
        {catalog!.specialties.map((s) => (
          <SpecialtyCard key={s.id} specialty={s} />
        ))}
      </div>
    </div>
  );
}
