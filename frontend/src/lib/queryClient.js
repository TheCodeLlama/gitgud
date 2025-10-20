/**
 * React Query client configuration
 * Handles caching, refetching, and error handling for all API queries
 */

import { QueryClient } from '@tanstack/react-query';

/**
 * Default query options for all queries
 */
const defaultQueryOptions = {
  queries: {
    // Refetch on window focus only in production
    refetchOnWindowFocus: import.meta.env.PROD,

    // Retry failed requests (except 4xx errors)
    retry: (failureCount, error) => {
      // Don't retry on client errors (4xx)
      if (error?.response?.status >= 400 && error?.response?.status < 500) {
        return false;
      }
      // Retry up to 2 times for server errors
      return failureCount < 2;
    },

    // Stale time: how long data is considered fresh (5 minutes)
    staleTime: 5 * 60 * 1000,

    // Cache time: how long unused data stays in cache (10 minutes)
    gcTime: 10 * 60 * 1000,

    // Refetch on mount if data is stale
    refetchOnMount: true,

    // Don't refetch on reconnect in development
    refetchOnReconnect: import.meta.env.PROD,
  },
  mutations: {
    // Retry mutations once on failure
    retry: 1,
  },
};

/**
 * Global error handler for queries
 */
const onError = (error) => {
  // Log errors in development
  if (import.meta.env.DEV) {
    console.error('[React Query Error]', error);
  }

  // Handle specific error codes
  if (error?.response?.status === 401) {
    console.error('[React Query] Authentication error - redirecting to login');
    // Auth context will handle redirect via event listener
  }

  if (error?.response?.status === 403) {
    console.error('[React Query] Authorization error - insufficient permissions');
  }

  if (error?.response?.status >= 500) {
    console.error('[React Query] Server error - please try again later');
  }

  if (!error?.response) {
    console.error('[React Query] Network error - check your connection');
  }
};

/**
 * Create and configure QueryClient instance
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    ...defaultQueryOptions,
    queries: {
      ...defaultQueryOptions.queries,
      onError,
    },
    mutations: {
      ...defaultQueryOptions.mutations,
      onError,
    },
  },
});

/**
 * Query keys factory for consistent cache key management
 * Provides centralized query key definitions
 */
export const queryKeys = {
  // Authentication
  auth: {
    me: ['auth', 'me'],
  },

  // Learning
  learning: {
    all: ['learning'],
    modules: () => [...queryKeys.learning.all, 'modules'],
    module: (id) => [...queryKeys.learning.modules(), id],
    lessons: (moduleId) => [...queryKeys.learning.all, 'lessons', moduleId],
    lesson: (id) => [...queryKeys.learning.all, 'lesson', id],
    progress: () => [...queryKeys.learning.all, 'progress'],
    lessonProgress: (lessonId) => [...queryKeys.learning.progress(), lessonId],
    submissions: () => [...queryKeys.learning.all, 'submissions'],
    latestSubmission: (userId, lessonId) => [...queryKeys.learning.submissions(), 'latest', userId, lessonId],
    submissionHistory: (userId, lessonId) => [...queryKeys.learning.submissions(), 'history', userId, lessonId],
    projectFiles: (lessonId) => [...queryKeys.learning.all, 'lesson', lessonId, 'files'],
    projectFile: (lessonId, fileId) => [...queryKeys.learning.projectFiles(lessonId), fileId],
  },

  // Gamification
  gamification: {
    all: ['gamification'],
    stats: (userId) => [...queryKeys.gamification.all, 'stats', userId],
    achievements: () => [...queryKeys.gamification.all, 'achievements'],
    userAchievements: (userId) => [...queryKeys.gamification.achievements(), 'user', userId],
    levels: () => [...queryKeys.gamification.all, 'levels'],
    profile: (userId) => [...queryKeys.gamification.all, 'profile', userId],
  },

  // Code Execution
  execution: {
    all: ['execution'],
    result: (jobId) => [...queryKeys.execution.all, 'result', jobId],
  },
};

export default queryClient;
