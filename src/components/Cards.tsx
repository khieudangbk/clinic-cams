import { Link } from 'react-router-dom';
import { Video } from 'lucide-react';
import type { Doctor } from '../interfaces/catalog/Doctor';
import type { Specialty } from '../interfaces/catalog/Specialty';
import { media, specialtyImage, doctorImage, articles } from '../data';
export function SpecialtyCard({ specialty }: { specialty: Specialty }) {
  return (
    <Link className="media-card" to={'/chuyen-khoa/' + specialty.id}>
      <img loading="lazy" src={media + specialtyImage(specialty)} alt="" />
      <h3>{specialty.name}</h3>
    </Link>
  );
}
export function DoctorCard({ doctor }: { doctor: Doctor }) {
  return (
    <Link className="doctor-card" to={'/bac-si/' + doctor.id}>
      <img loading="lazy" src={media + doctorImage(doctor)} alt={'Minh họa ' + doctor.name} />
      <h3>{doctor.name}</h3>
      <p>{doctor.specialty_name}</p>
    </Link>
  );
}
export function FacilityCard() {
  return (
    <Link className="media-card facility-card" to="/co-so-y-te/cams">
      <img loading="lazy" src="/media/clinic.svg" alt="Phòng khám CAMS" />
      <h3>Phòng khám đa khoa CAMS</h3>
    </Link>
  );
}
export function ArticleCard({
  article,
  indexStyle = false,
}: {
  article: (typeof articles)[number];
  indexStyle?: boolean;
}) {
  return (
    <Link
      className={'article-card ' + (indexStyle ? 'index-card' : '')}
      to={'/cam-nang/' + article.id}
    >
      <img loading="lazy" src={media + article.image} alt="" />
      <h3>{article.title}</h3>
    </Link>
  );
}
export function ServiceMediaCard({
  title,
  to,
  image,
  video = false,
}: {
  title: string;
  to: string;
  image: string;
  video?: boolean;
}) {
  return (
    <Link className="media-card" to={to}>
      <div className="media-image">
        <img loading="lazy" src={media + image} alt="" />
        {video && (
          <span className="video-badge">
            <Video />
          </span>
        )}
      </div>
      <h3>{title}</h3>
    </Link>
  );
}
