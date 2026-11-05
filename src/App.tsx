import { useApp } from './context';
import { Loading, ErrorBox } from './components/UI';
export default function App() {
  const { ready, error, catalog } = useApp();
  if (!ready) return <Loading />;
  if (error || !catalog) return <ErrorBox message={error || 'Catalog unavailable'} />;
  return <main className="container page"><h1>CAMS</h1></main>;
}
