import { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from '@store/authStore';
import MainLayout from '@components/layouts/MainLayout';
import AuthLayout from '@components/layouts/AuthLayout';
import LoadingSpinner from '@components/common/LoadingSpinner';
import ProtectedRoute from '@components/auth/ProtectedRoute';

// Lazy-loaded pages for code splitting
const HomePage = lazy(() => import('@pages/HomePage'));
const LoginPage = lazy(() => import('@pages/auth/LoginPage'));
const RegisterPage = lazy(() => import('@pages/auth/RegisterPage'));
const ProfileSelectPage = lazy(() => import('@pages/ProfileSelectPage'));
const BrowsePage = lazy(() => import('@pages/BrowsePage'));
const SearchPage = lazy(() => import('@pages/SearchPage'));
const WatchPage = lazy(() => import('@pages/WatchPage'));
const MovieDetailPage = lazy(() => import('@pages/MovieDetailPage'));
const SeriesDetailPage = lazy(() => import('@pages/SeriesDetailPage'));
const GenrePage = lazy(() => import('@pages/GenrePage'));
const MyListPage = lazy(() => import('@pages/MyListPage'));
const SettingsPage = lazy(() => import('@pages/SettingsPage'));
const ProfileManagePage = lazy(() => import('@pages/ProfileManagePage'));
const HelpCenterPage = lazy(() => import('@pages/HelpCenterPage'));
const NotFoundPage = lazy(() => import('@pages/NotFoundPage'));

function App() {
  const { isAuthenticated, activeProfile } = useAuthStore();

  return (
    <Suspense
      fallback={
        <div className="min-h-screen bg-dark-500 flex items-center justify-center">
          <LoadingSpinner size="lg" />
        </div>
      }
    >
      <Routes>
        {/* Public routes */}
        <Route element={<AuthLayout />}>
          <Route
            path="/login"
            element={
              isAuthenticated ? <Navigate to="/browse" replace /> : <LoginPage />
            }
          />
          <Route
            path="/register"
            element={
              isAuthenticated ? <Navigate to="/browse" replace /> : <RegisterPage />
            }
          />
        </Route>

        {/* Landing page */}
        <Route
          path="/"
          element={
            isAuthenticated ? <Navigate to="/browse" replace /> : <HomePage />
          }
        />

        {/* Protected routes */}
        <Route element={<ProtectedRoute />}>
          {/* Profile selection (no active profile required) */}
          <Route path="/profiles" element={<ProfileSelectPage />} />
          <Route path="/profiles/manage" element={<ProfileManagePage />} />

          {/* Main app routes (require active profile) */}
          <Route element={<MainLayout />}>
            <Route
              path="/browse"
              element={
                activeProfile ? <BrowsePage /> : <Navigate to="/profiles" replace />
              }
            />
            <Route
              path="/search"
              element={
                activeProfile ? <SearchPage /> : <Navigate to="/profiles" replace />
              }
            />
            <Route
              path="/movie/:id"
              element={
                activeProfile ? <MovieDetailPage /> : <Navigate to="/profiles" replace />
              }
            />
            <Route
              path="/series/:id"
              element={
                activeProfile ? <SeriesDetailPage /> : <Navigate to="/profiles" replace />
              }
            />
            <Route
              path="/genre/:genreId"
              element={
                activeProfile ? <GenrePage /> : <Navigate to="/profiles" replace />
              }
            />
            <Route
              path="/my-list"
              element={
                activeProfile ? <MyListPage /> : <Navigate to="/profiles" replace />
              }
            />
            <Route
              path="/settings"
              element={
                activeProfile ? <SettingsPage /> : <Navigate to="/profiles" replace />
              }
            />
            <Route
              path="/help"
              element={
                activeProfile ? <HelpCenterPage /> : <Navigate to="/profiles" replace />
              }
            />
          </Route>

          {/* Watch page (fullscreen, no layout) */}
          <Route
            path="/watch/:type/:id"
            element={
              activeProfile ? <WatchPage /> : <Navigate to="/profiles" replace />
            }
          />
        </Route>

        {/* 404 */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Suspense>
  );
}

export default App;
