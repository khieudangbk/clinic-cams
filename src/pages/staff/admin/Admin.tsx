import { CreateStaffForm } from './components/CreateStaffForm';
import { BookingSettingsForm } from './components/BookingSettingsForm';
import { SpecialtyManager } from './components/SpecialtyManager';
import { StaffAccounts } from './components/StaffAccounts';
import type { UserListResponse } from '../../../interfaces/admin/UserListResponse';
import type { AdminRequests } from '../../../interfaces/admin/AdminRequests';
import type { OkResponse } from '../../../interfaces/common/OkResponse';
import type { CreatedIdResponse } from '../../../interfaces/common/CreatedIdResponse';
import { useEffect, useState } from 'react';
import { api } from '../../../api';
import { useApp } from '../../../context';
import type { User } from '../../../interfaces/account/User';
import { Empty, ErrorBox } from '../../../components/UI';

export function Admin() {
  const { user, refresh, notify } = useApp();
  const [users, setUsers] = useState<User[]>([]),
    [error, setError] = useState('');
  const load = () => {
    api<UserListResponse>('/api/users')
      .then((r) => setUsers(r.users))
      .catch((e) => setError(e.message));
  };
  useEffect(load, []);
  if (user?.role !== 'manager') return <Empty title="Chức năng dành cho quản lý" />;
  const save = async <P extends keyof AdminRequests>(path: P, data: AdminRequests[P]) => {
    try {
      await api<OkResponse | CreatedIdResponse, AdminRequests[P]>(path, 'POST', data);
      notify('Đã lưu thay đổi.');
      load();
      await refresh();
      return true;
    } catch (e) {
      notify((e as Error).message, true);
      return false;
    }
  };
  return (
    <>
      <h1>Quản trị phòng khám</h1>
      {error && <ErrorBox message={error} retry={load} />}
      <div className="dashboard-grid">
        <CreateStaffForm save={save} />
        <BookingSettingsForm save={save} />
      </div>
      <SpecialtyManager save={save} />
      <StaffAccounts users={users} save={save} />
    </>
  );
}
