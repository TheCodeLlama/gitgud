/**
 * Custom hook for fetching level progress data for progress bars
 * Uses React Query for caching and automatic refetching
 */

import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { useAuth } from '../contexts/AuthContext';

/**
 * Fetch level progress from the backend
 * @returns {Promise} Level progress data
 */
const fetchLevelProgress = async () => {
  const { data } = await api.get('/v1/gamification/levels/progress');
  return data.data; // Extract data from ApiResponse wrapper
};

/**
 * Hook to fetch and cache level progress data
 * @returns {Object} Query result with progress data, loading, and error states
 */
export function useLevelProgress() {
  const { authenticated } = useAuth();

  return useQuery({
    queryKey: ['levelProgress'],
    queryFn: fetchLevelProgress,
    enabled: authenticated, // Only fetch if user is authenticated
    staleTime: 1000 * 60 * 5, // Consider data fresh for 5 minutes
    refetchOnWindowFocus: true, // Refetch when user returns to tab
    retry: 2, // Retry failed requests twice
  });
}
