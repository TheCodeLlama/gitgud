/**
 * React Router routes configuration
 * Defines all application routes and navigation structure
 */

import { Routes, Route } from 'react-router';
import Home from '../pages/Home';
import ThemeGuide from '../pages/ThemeGuide';
import Dashboard from '../pages/Dashboard';
import NotFound from '../pages/NotFound';

/**
 * Main routes configuration
 */
export function AppRoutes() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/" element={<Home />} />
      <Route path="/theme-guide" element={<ThemeGuide />} />

      {/* Dashboard (will require authentication in 6.2) */}
      <Route path="/dashboard" element={<Dashboard />} />

      {/* Catch-all 404 */}
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

export default AppRoutes;
