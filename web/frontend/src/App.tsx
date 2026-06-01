/**
 * Root application component.
 *
 * Declares the route hierarchy and the ProtectedRoute wrapper that redirects
 * unauthenticated users to /login. The component itself does not own a
 * BrowserRouter — that is provided by main.tsx so tests can supply a
 * MemoryRouter instead.
 *
 * Route page components are loaded on demand (React.lazy) so each becomes its
 * own bundle chunk (NFR-P-2 / SRS §10.1). A top-level ErrorBoundary + Suspense
 * covers initial load and the public routes; the protected routes get a second
 * ErrorBoundary + Suspense around the AppLayout <Outlet /> (see AppLayout) so
 * the shell stays mounted while a page chunk loads and a page failure is
 * contained to the content area (SRS §10.2).
 */

import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { Suspense, type ReactNode } from 'react';

import { AppLayout } from './components/AppLayout';
import { ErrorBoundary } from './components/ErrorBoundary';
import { LoadingSplash } from './components/LoadingSplash';
import { useAuth } from './contexts/AuthContext';
import { lazyNamed } from './lib/lazy-named';

// Page modules use named exports, so lazyNamed maps the named export onto the
// default export React.lazy requires (see ./lib/lazy-named).
const DashboardPage = lazyNamed(
  () => import('./features/dashboard/pages/DashboardPage'),
  'DashboardPage',
);
const LoginPage = lazyNamed(() => import('./features/auth/pages/LoginPage'), 'LoginPage');
const RegisterPage = lazyNamed(() => import('./features/auth/pages/RegisterPage'), 'RegisterPage');
const AchievementsPage = lazyNamed(
  () => import('./features/achievements/pages/AchievementsPage'),
  'AchievementsPage',
);
const SettingsPage = lazyNamed(
  () => import('./features/settings/pages/SettingsPage'),
  'SettingsPage',
);
const GoalsPage = lazyNamed(() => import('./features/goals/pages/GoalsPage'), 'GoalsPage');
const WeightHistoryPage = lazyNamed(
  () => import('./features/weight/pages/WeightHistoryPage'),
  'WeightHistoryPage',
);
const WeightEntryFormPage = lazyNamed(
  () => import('./features/weight/pages/WeightEntryFormPage'),
  'WeightEntryFormPage',
);

/**
 * Wraps a route element so that unauthenticated users are redirected to
 * /login?from=<original-path> before the protected page renders.
 */
function ProtectedRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) return <LoadingSplash />;

  if (!isAuthenticated) {
    const params = new URLSearchParams({ from: location.pathname });
    return <Navigate to={`/login?${params.toString()}`} replace />;
  }

  return <>{children}</>;
}

/**
 * Declares the full route hierarchy.
 *
 * Public routes (login, register) use AuthLayout via the page components
 * themselves. Protected routes are nested inside AppLayout via a wrapping
 * Route + ProtectedRoute. All route page components are lazy-loaded; the
 * Suspense fallbacks and error boundaries are described in the file header.
 */
export function App() {
  return (
    <ErrorBoundary>
      <Suspense fallback={<LoadingSplash />}>
        <Routes>
          {/* ── Public routes (no navigation) ─────────────────────────── */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* ── Protected routes (inside AppLayout) ───────────────────── */}
          <Route
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<DashboardPage />} />
            <Route path="/weight" element={<WeightHistoryPage />} />
            <Route path="/weight/new" element={<WeightEntryFormPage />} />
            <Route path="/weight/:entryId/edit" element={<WeightEntryFormPage />} />
            <Route path="/goals" element={<GoalsPage />} />
            <Route path="/achievements" element={<AchievementsPage />} />
            <Route path="/settings" element={<SettingsPage />} />
          </Route>

          {/* ── Fallback ─────────────────────────────────────────────── */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Suspense>
    </ErrorBoundary>
  );
}
