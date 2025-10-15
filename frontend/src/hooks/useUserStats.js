/**
 * Custom hook for fetching user stats (XP, level, streak)
 * Uses React Query for caching and automatic refetching
 */

import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { queryKeys } from '../lib/queryClient';
import { useAuth } from '../contexts/AuthContext';

/**
 * Fetch user stats from the backend
 * @returns {Promise} User stats data
 */
const fetchUserStats = async () => {
  const { data } = await api.get('/v1/gamification/stats');
  return data.data; // Extract data from ApiResponse wrapper
};

/**
 * Hook to fetch and cache user stats
 * @returns {Object} Query result with stats data, loading, and error states
 */
export function useUserStats() {
  const { authenticated, user } = useAuth();

  return useQuery({
    queryKey: user?.id ? queryKeys.gamification.stats(user.id) : ['userStats'],
    queryFn: fetchUserStats,
    enabled: authenticated, // Only fetch if user is authenticated
    staleTime: 1000 * 60 * 5, // Consider data fresh for 5 minutes
    refetchOnWindowFocus: true, // Refetch when user returns to tab
    retry: 2, // Retry failed requests twice
  });
}
