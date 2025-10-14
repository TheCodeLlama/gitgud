/**
 * Custom hook for fetching complete user profile with stats
 * Uses React Query for caching and automatic refetching
 */

import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { useAuth } from '../contexts/AuthContext';

/**
 * Fetch user profile with stats from the backend
 * @returns {Promise} User profile data including stats
 */
const fetchUserProfile = async () => {
  const { data } = await api.get('/v1/gamification/profile');
  return data.data; // Extract data from ApiResponse wrapper
};

/**
 * Hook to fetch and cache user profile with stats
 * @returns {Object} Query result with profile data, loading, and error states
 */
export function useUserProfile() {
  const { authenticated } = useAuth();

  return useQuery({
    queryKey: ['userProfile'],
    queryFn: fetchUserProfile,
    enabled: authenticated, // Only fetch if user is authenticated
    staleTime: 1000 * 60 * 10, // Consider data fresh for 10 minutes
    refetchOnWindowFocus: true, // Refetch when user returns to tab
    retry: 2, // Retry failed requests twice
  });
}
