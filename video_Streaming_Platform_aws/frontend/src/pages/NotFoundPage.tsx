import { Link } from 'react-router-dom';
import { Button } from '@components/common';

export default function NotFoundPage() {
  return (
    <div className="min-h-screen bg-dark-500 flex flex-col items-center justify-center px-4 text-center">
      <h1 className="text-6xl md:text-8xl font-bold text-primary-500 mb-4">404</h1>
      <h2 className="text-2xl md:text-3xl font-semibold text-white mb-4">
        Lost your way?
      </h2>
      <p className="text-gray-400 max-w-md mb-8">
        Sorry, we can't find that page. You'll find lots to explore on the home page.
      </p>
      <Link to="/browse">
        <Button size="lg">StreamFlix Home</Button>
      </Link>
      <p className="text-gray-600 text-sm mt-8">
        Error Code: <span className="text-white">NSES-404</span>
      </p>
    </div>
  );
}
