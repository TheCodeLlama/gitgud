/**
 * Custom hook for fetching user's overall learning progress
 * Uses React Query for caching and automatic refetching
 */

import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { useAuth } from '../contexts/AuthContext';

/**
 * Fetch user's overall progress from the backend
 * @returns {Promise} User progress data
 */
const fetchUserProgress = async () => {
  const { data } = await api.get('/v1/learning/progress');
  return data.data; // Extract data from ApiResponse wrapper
};

/**
 * Hook to fetch and cache user's overall progress
 * @returns {Object} Query result with progress data, loading, and error states
 */
export function useUserProgress() {
  const { authenticated } = useAuth();

  return useQuery({
    queryKey: ['userProgress'],
    queryFn: fetchUserProgress,
    enabled: authenticated, // Only fetch if user is authenticated
    staleTime: 1000 * 60 * 5, // Consider data fresh for 5 minutes
    refetchOnWindowFocus: true, // Refetch when user returns to tab
    retry: 2, // Retry failed requests twice
  });
}
