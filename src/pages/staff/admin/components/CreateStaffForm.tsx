import { useApp } from '../../../../context';
import type { CreateStaffFormProps } from '../../../../interfaces/pages/CreateStaffFormProps';

export function CreateStaffForm({ save }: CreateStaffFormProps) {
  const { catalog } = useApp();
  return (
    <section className="panel">
      <h2>Tạo tài khoản nhân sự</h2>
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          const f = e.currentTarget;
          const d = Object.fromEntries(new FormData(f));
          if (
            await save('/api/users', {
              name: String(d.name),
              email: String(d.email),
              password: String(d.password),
              role: d.role === 'receptionist' ? 'receptionist' : 'doctor',
              specialty_id: Number(d.specialty_id),
            })
          )
            f.reset();
        }}
      >
        <label>
          Họ tên
          <input name="name" required maxLength={100} />
        </label>
        <label>
          Email
          <input name="email" type="email" required />
        </label>
        <label>
          Mật khẩu ban đầu
          <input
            name="password"
            type="password"
            minLength={10}
            required
            autoComplete="new-password"
          />
        </label>
        <div className="form-grid">
          <label>
            Vai trò
            <select name="role">
              <option value="doctor">Bác sĩ</option>
              <option value="receptionist">Lễ tân</option>
            </select>
          </label>
          <label>
            Chuyên khoa
            <select name="specialty_id">
              {catalog!.specialties.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </label>
        </div>
        <button className="primary">Tạo tài khoản</button>
      </form>
    </section>
  );
}
