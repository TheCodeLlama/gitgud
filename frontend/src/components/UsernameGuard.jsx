import { useEffect, useState } from 'react';
import { useLocation } from 'react-router';
import { useAuth } from '../contexts/AuthContext';
import UsernameModal from './auth/UsernameModal';

/**
 * UsernameGuard - Global component that ensures authenticated users have a username
 * Shows a non-dismissible modal on any page (except auth pages) if user lacks a username
 */
export default function UsernameGuard({ children }) {
  const { user, authenticated, loading } = useAuth();
  const location = useLocation();
  const [showModal, setShowModal] = useState(false);

  // Auth pages where we should NOT show the username modal
  const authPages = ['/', '/signin', '/signup'];
  const isAuthPage = authPages.includes(location.pathname);

  useEffect(() => {
    // Show modal if:
    // 1. User is authenticated
    // 2. User doesn't have a username
    // 3. We're not on an auth page
    // 4. Not currently loading
    if (authenticated && user && !user.username && !isAuthPage && !loading) {
      setShowModal(true);
    } else {
      setShowModal(false);
    }
  }, [authenticated, user, isAuthPage, loading]);

  /**
   * Handle username submission
   * This will complete the user sync with the backend
   */
  const handleUsernameSubmit = async (username) => {
    try {
      // Import the api client
      const { api } = await import('../lib/api');

      // Sync user with backend, passing the username
      const syncResponse = await api.post('/v1/auth/sync', { username });

      // Update the user object in AuthContext
      // Force a page reload to refresh the auth state
      window.location.reload();
    } catch (error) {
      console.error('Failed to update username:', error);
      throw error;
    }
  };

  return (
    <>
      {children}
      {/* Non-dismissible username modal - no cancel button */}
      {showModal && <UsernameModal onSubmit={handleUsernameSubmit} />}
    </>
  );
}
