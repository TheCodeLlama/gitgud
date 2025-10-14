/**
 * React Router routes configuration
 * Defines all application routes and navigation structure
 */

import { Routes, Route } from 'react-router';
import Home from '../pages/Home';
import ThemeGuide from '../pages/ThemeGuide';
import Dashboard from '../pages/dashboard/Dashboard.jsx';
import NotFound from '../pages/NotFound';
import SignIn from '../pages/auth/SignIn.jsx';
import SignUp from '../pages/auth/SignUp.jsx';
import ProtectedRoute from '../components/ProtectedRoute';
import Layout from '../components/Layout';

/**
 * Main routes configuration
 */
export function AppRoutes() {
  return (
    <Routes>
      {/* Public routes with navbar */}
      <Route
        path="/"
        element={
          <Layout>
            <Home />
          </Layout>
        }
      />
      <Route
        path="/theme-guide"
        element={
          <Layout>
            <ThemeGuide />
          </Layout>
        }
      />

      {/* Auth routes without navbar */}
      <Route path="/signin" element={<SignIn />} />
      <Route path="/signup" element={<SignUp />} />

      {/* Protected routes with navbar */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <Layout>
              <Dashboard />
            </Layout>
          </ProtectedRoute>
        }
      />

      {/* Catch-all 404 without navbar */}
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

export default AppRoutes;
