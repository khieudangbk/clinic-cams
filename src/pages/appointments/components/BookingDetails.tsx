export function BookingDetails({ value }: { value: string }) {
  try {
    const d: unknown = JSON.parse(value);
    if (!d || typeof d !== 'object' || Array.isArray(d)) return null;
    const details = d as Record<string, unknown>;
    return (
      <dl className="details-list">
        {[
          ['birth_year', 'Năm sinh'],
          ['gender', 'Giới tính'],
          ['province', 'Tỉnh/thành phố'],
          ['ward', 'Phường/xã'],
          ['address', 'Địa chỉ'],
          ['extra_note', 'Ghi chú'],
        ].map(
          ([key, label]) =>
            typeof details[key] === 'string' &&
            details[key] && (
              <div key={key}>
                <dt>{label}</dt>
                <dd>
                  {key === 'gender'
                    ? ({ male: 'Nam', female: 'Nữ', other: 'Khác' } as Record<string, string>)[
                        String(details[key])
                      ] || String(details[key])
                    : String(details[key])}
                </dd>
              </div>
            ),
        )}
      </dl>
    );
  } catch {
    return null;
  }
}
