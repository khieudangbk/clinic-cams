import { useApp } from '../../../../context';
import type { SpecialtyManagerProps } from '../../../../interfaces/pages/SpecialtyManagerProps';

export function SpecialtyManager({ save }: SpecialtyManagerProps) {
  const { catalog } = useApp();
  return (
    <section className="panel section-gap">
      <h2>Danh mục chuyên khoa</h2>
      <form
        className="inline-form"
        onSubmit={async (e) => {
          e.preventDefault();
          const f = e.currentTarget;
          if (await save('/api/specialties', { name: String(new FormData(f).get('name') ?? '') }))
            f.reset();
        }}
      >
        <label>
          <span className="sr-only">Chuyên khoa mới</span>
          <input name="name" required placeholder="Tên chuyên khoa mới" maxLength={100} />
        </label>
        <button className="primary">Thêm chuyên khoa</button>
      </form>
      <div className="specialty-edit-grid">
        {catalog!.specialties.map((s) => (
          <form
            key={s.id}
            onSubmit={(e) => {
              e.preventDefault();
              save('/api/specialties', {
                id: s.id,
                name: String(new FormData(e.currentTarget).get('name') ?? ''),
              });
            }}
          >
            <label>
              <span className="sr-only">Sửa {s.name}</span>
              <input name="name" defaultValue={s.name} required maxLength={100} />
            </label>
            <button className="outline">Lưu</button>
          </form>
        ))}
      </div>
    </section>
  );
}
