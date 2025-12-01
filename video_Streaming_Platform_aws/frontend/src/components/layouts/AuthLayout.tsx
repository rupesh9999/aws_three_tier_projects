import { Outlet, Link } from 'react-router-dom';

export default function AuthLayout() {
  return (
    <div className="min-h-screen bg-dark-500 relative">
      {/* Background Image */}
      <div
        className="absolute inset-0 bg-cover bg-center opacity-40"
        style={{
          backgroundImage: `url('https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=1920')`,
        }}
      />
      <div className="absolute inset-0 bg-gradient-to-t from-dark-500 via-dark-500/60 to-dark-500/40" />

      {/* Header */}
      <header className="relative z-10 px-4 md:px-12 py-6">
        <Link to="/">
          <h1 className="text-3xl md:text-4xl font-bold text-primary-500">
            STREAMFLIX
          </h1>
        </Link>
      </header>

      {/* Content */}
      <main className="relative z-10 flex items-center justify-center px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
}
