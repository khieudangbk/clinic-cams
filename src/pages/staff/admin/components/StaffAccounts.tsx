import { useState } from 'react';
import { useApp } from '../../../../context';
import { roles } from '../../../../display';
import type { User } from '../../../../interfaces/account/User';
import type { StaffAccountsProps } from '../../../../interfaces/pages/StaffAccountsProps';

export function StaffAccounts({ users, save }: StaffAccountsProps) {
  const { catalog } = useApp();
  const [editing, setEditing] = useState<User | null>(null);
  return (
    <section className="panel section-gap">
      <h2>Tài khoản nhân viên</h2>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Họ tên</th>
              <th>Email</th>
              <th>Vai trò</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.name}</td>
                <td>{u.email}</td>
                <td>{roles[u.role]}</td>
                <td>{u.active ? 'Đang hoạt động' : 'Đã khóa'}</td>
                <td>
                  {u.role !== 'manager' && (
                    <div className="row-actions">
                      <button onClick={() => setEditing(u)}>Sửa</button>
                      <button onClick={() => save(`/api/users/${u.id}`, { active: !u.active })}>
                        {u.active ? 'Khóa' : 'Mở khóa'}
                      </button>
                    </div>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {editing && (
        <form
          className="edit-user"
          key={editing.id}
          onSubmit={async (e) => {
            e.preventDefault();
            const d = Object.fromEntries(new FormData(e.currentTarget));
            if (
              await save(`/api/users/${editing.id}`, {
                name: String(d.name),
                ...(editing.role === 'doctor' ? { specialty_id: Number(d.specialty_id) } : {}),
              })
            )
              setEditing(null);
          }}
        >
          <h3>Sửa tài khoản {editing.email}</h3>
          <label>
            Họ tên
            <input name="name" defaultValue={editing.name} required maxLength={100} />
          </label>
          {editing.role === 'doctor' && (
            <label>
              Chuyên khoa
              <select name="specialty_id" defaultValue={editing.specialty_id ?? ''}>
                {catalog!.specialties.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </label>
          )}
          <button className="primary">Lưu</button>
          <button type="button" className="outline" onClick={() => setEditing(null)}>
            Hủy
          </button>
        </form>
      )}
    </section>
  );
}
