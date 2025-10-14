/**
 * Custom hook for fetching user's earned achievements
 * Uses React Query for caching and automatic refetching
 */

import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { useAuth } from '../contexts/AuthContext';

/**
 * Fetch user's achievements from the backend
 * @returns {Promise} User achievements data
 */
const fetchUserAchievements = async () => {
  const { data } = await api.get('/v1/gamification/achievements/user');
  return data.data; // Extract data from ApiResponse wrapper
};

/**
 * Hook to fetch and cache user's earned achievements
 * @returns {Object} Query result with achievements data, loading, and error states
 */
export function useUserAchievements() {
  const { authenticated } = useAuth();

  return useQuery({
    queryKey: ['userAchievements'],
    queryFn: fetchUserAchievements,
    enabled: authenticated, // Only fetch if user is authenticated
    staleTime: 1000 * 60 * 10, // Consider data fresh for 10 minutes
    refetchOnWindowFocus: true, // Refetch when user returns to tab
    retry: 2, // Retry failed requests twice
  });
}
