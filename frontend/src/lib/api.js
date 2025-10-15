/**
 * Axios API client configuration
 * Handles authentication, request/response interceptors, and error handling
 */

import axios from 'axios';

// Base API URL from environment variables
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

/**
 * Create configured axios instance
 */
export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 seconds
});

/**
 * Request interceptor
 * - Logs request details in development
 * NOTE: Token is managed by AuthContext and set directly on api instance
 */
api.interceptors.request.use(
  (config) => {
    // Log request in development
    if (import.meta.env.DEV) {
      console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, config.data);
    }

    return config;
  },
  (error) => {
    console.error('[API Request Error]', error);
    return Promise.reject(error);
  }
);

/**
 * Response interceptor
 * - Handles successful responses
 * - Handles error responses (401, 403, 500, etc.)
 * - Automatic token refresh on 401
 */
api.interceptors.response.use(
  (response) => {
    // Log response in development
    if (import.meta.env.DEV) {
      console.log(`[API Response] ${response.config.method?.toUpperCase()} ${response.config.url}`, response.data);
    }

    return response;
  },
  async (error) => {
    // Log error in development
    if (import.meta.env.DEV) {
      console.error('[API Response Error]', error.response?.status, error.message);
    }

    // Handle 401 Unauthorized - token expired or invalid
    // Firebase token refresh is handled automatically by AuthContext
    if (error.response?.status === 401) {
      console.error('[API] Unauthorized - Firebase token may be expired');
      window.dispatchEvent(new CustomEvent('auth:logout'));
    }

    // Handle 403 Forbidden
    if (error.response?.status === 403) {
      console.error('[API] Access forbidden:', error.response.data?.message);
    }

    // Handle 404 Not Found
    if (error.response?.status === 404) {
      console.error('[API] Resource not found:', error.config.url);
    }

    // Handle 500 Internal Server Error
    if (error.response?.status === 500) {
      console.error('[API] Server error:', error.response.data?.message);
    }

    // Handle network errors
    if (!error.response) {
      console.error('[API] Network error - server may be unreachable');
    }

    return Promise.reject(error);
  }
);

/**
 * Helper function to set authentication token
 * @param {string} token - Firebase ID token
 */
export const setAuthToken = (token) => {
  if (token) {
    localStorage.setItem('firebase_token', token);
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }
};

/**
 * Helper function to clear authentication tokens
 */
export const clearAuthTokens = () => {
  localStorage.removeItem('firebase_token');
  delete api.defaults.headers.common['Authorization'];
};

/**
 * Helper function to get current authentication token
 * @returns {string|null} - Current Firebase token or null
 */
export const getAuthToken = () => {
  return localStorage.getItem('firebase_token');
};

/**
 * Check if user is authenticated
 * @returns {boolean} - True if Firebase token exists
 */
export const isAuthenticated = () => {
  return !!getAuthToken();
};

export default api;
