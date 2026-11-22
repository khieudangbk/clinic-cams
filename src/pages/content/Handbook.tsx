import { articles } from '../../data';
import { Breadcrumb, Carousel } from '../../components/UI';
import { ArticleCard } from '../../components/Cards';

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
