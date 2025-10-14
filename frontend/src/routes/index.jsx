/**
 * React Router routes configuration
 * Defines all application routes and navigation structure
 */

import { Routes, Route } from 'react-router';
import Home from '../pages/Home';
import ThemeGuide from '../pages/ThemeGuide';
import Dashboard from '../pages/Dashboard';
import NotFound from '../pages/NotFound';
import SignIn from '../pages/SignIn';
import SignUp from '../pages/SignUp';
import ProtectedRoute from '../components/ProtectedRoute';

/**
 * Main routes configuration
 */
export function AppRoutes() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/" element={<Home />} />
      <Route path="/theme-guide" element={<ThemeGuide />} />
      <Route path="/signin" element={<SignIn />} />
      <Route path="/signup" element={<SignUp />} />

      {/* Protected routes */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        }
      />

      {/* Catch-all 404 */}
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

export default AppRoutes;
