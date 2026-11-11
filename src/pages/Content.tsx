import { Link, useParams } from 'react-router-dom';
import { articles, media } from '../data';
import { Breadcrumb, Carousel, Empty } from '../components/UI';
import { ArticleCard } from '../components/Cards';
export function Handbook() {
  return (
    <div className="container page handbook">
      <Breadcrumb items={[{ label: 'Cẩm nang' }]} />
      <h1>Bài viết mới nhất</h1>
      <Carousel label="Bài viết mới nhất" variant="articles">
        {articles.map((a) => (
          <ArticleCard article={a} key={a.id} indexStyle />
        ))}
      </Carousel>
      {['Hướng dẫn đi khám', 'Sử dụng CAMS'].map((category) => (
        <section key={category}>
          <h2>{category}</h2>
          <div className="handbook-grid">
            {articles
              .filter((a) => a.category === category)
              .map((a) => (
                <ArticleCard article={a} key={a.id} indexStyle />
              ))}
          </div>
        </section>
      ))}
    </div>
  );
}
export function Article() {
  const { id } = useParams();
  const article = articles.find((x) => x.id === id);
  if (!article)
    return (
      <Empty title="Không tìm thấy bài viết">
        <Link to="/cam-nang">Về cẩm nang</Link>
      </Empty>
    );
  return (
    <div className="article-page">
      <div className="article-cover">
        <img src={media + article.image} alt="" />
        <div className="container">
          <Breadcrumb items={[{ label: 'Cẩm nang', to: '/cam-nang' }, { label: article.title }]} />
        </div>
      </div>
      <div className="container article-layout">
        <article className="article-body">
          <figure>
            <img src={media + article.image} alt="Minh họa nội dung bài viết" />
            <figcaption>Minh họa hướng dẫn sử dụng CAMS</figcaption>
          </figure>
          <h1>{article.title}</h1>
          <p className="article-meta">
            Biên soạn: <Link to="/huong-dan">Nhóm CAMS</Link> · Nội dung hướng dẫn demo
          </p>
          <p>{article.intro}</p>
          <blockquote>
            Chủ động đặt lịch, kiểm tra xác nhận và chuẩn bị trước khi đến khám.
          </blockquote>
          {article.sections.map(([title, body], i) => (
            <section id={'muc-' + i} key={title}>
              <h2>{title}</h2>
              <p>{body}</p>
            </section>
          ))}
          <p className="article-note">
            Bài viết hướng dẫn thao tác và chuẩn bị chung, không thay thế chỉ định hay tư vấn của
            bác sĩ.
          </p>
          <Link to="/bac-si" className="outline-pill">
            Tìm bác sĩ và đặt lịch
          </Link>
        </article>
        <aside className="article-sidebar">
          <div>
            <h2>Nội dung chính</h2>
            {article.sections.map(([title], i) => (
              <a key={title} href={'#muc-' + i}>
                {title}
              </a>
            ))}
          </div>
          <Link className="article-chat" to="/tro-ly">
            <img src="/media/assistant.png" alt="" />
            <strong>Trò chuyện với Trợ lý Đi khám</strong>
          </Link>
        </aside>
      </div>
    </div>
  );
}
