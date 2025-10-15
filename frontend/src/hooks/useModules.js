/**
 * Custom hook for fetching learning modules
 * Uses React Query for caching and automatic refetching
 */

import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { queryKeys } from '../lib/queryClient';

/**
 * Fetch all modules from the backend
 * @returns {Promise} Modules data
 */
const fetchModules = async () => {
  const { data } = await api.get('/v1/learning/modules');
  return data.data; // Extract data from ApiResponse wrapper
};

/**
 * Hook to fetch and cache all modules
 * @returns {Object} Query result with modules data, loading, and error states
 */
export function useModules() {
  return useQuery({
    queryKey: queryKeys.learning.modules(),
    queryFn: fetchModules,
    staleTime: 1000 * 60 * 5, // Consider data fresh for 5 minutes (reduced for progress updates)
    refetchOnWindowFocus: false, // Modules don't change frequently
    refetchOnMount: 'always', // Always fetch fresh data when component mounts
    retry: 2, // Retry failed requests twice
  });
}
