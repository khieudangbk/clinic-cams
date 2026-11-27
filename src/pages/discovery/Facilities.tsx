import { Breadcrumb } from '../../components/UI';
import { FacilityCard } from '../../components/Cards';

export function Facilities() {
  return (
    <div className="container page">
      <Breadcrumb items={[{ label: 'Cơ sở y tế' }]} />
      <h1>Cơ sở y tế</h1>
      <p>Đặt khám trực tiếp tại cơ sở đang được hỗ trợ trong dự án.</p>
      <div className="listing-grid">
        <FacilityCard />
      </div>
    </div>
  );
}
