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
 * - Adds JWT token to Authorization header if available
 * - Logs request details in development
 */
api.interceptors.request.use(
  (config) => {
    // Get token from localStorage
    const token = localStorage.getItem('access_token');

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

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
    const originalRequest = error.config;

    // Log error in development
    if (import.meta.env.DEV) {
      console.error('[API Response Error]', error.response?.status, error.message);
    }

    // Handle 401 Unauthorized - token expired or invalid
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // Try to refresh the token
        const refreshToken = localStorage.getItem('refresh_token');

        if (refreshToken) {
          // Attempt token refresh (implement this endpoint when available)
          const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
            refresh_token: refreshToken,
          });

          const { access_token } = response.data;
          localStorage.setItem('access_token', access_token);

          // Retry the original request with new token
          originalRequest.headers.Authorization = `Bearer ${access_token}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        // Refresh failed, clear tokens and redirect to login
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');

        // Dispatch custom event for app to handle (e.g., redirect to login)
        window.dispatchEvent(new CustomEvent('auth:logout'));

        return Promise.reject(refreshError);
      }
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
 * @param {string} token - JWT access token
 * @param {string} refreshToken - JWT refresh token (optional)
 */
export const setAuthToken = (token, refreshToken = null) => {
  if (token) {
    localStorage.setItem('access_token', token);
    if (refreshToken) {
      localStorage.setItem('refresh_token', refreshToken);
    }
  }
};

/**
 * Helper function to clear authentication tokens
 */
export const clearAuthTokens = () => {
  localStorage.removeItem('access_token');
  localStorage.removeItem('refresh_token');
};

/**
 * Helper function to get current authentication token
 * @returns {string|null} - Current access token or null
 */
export const getAuthToken = () => {
  return localStorage.getItem('access_token');
};

/**
 * Check if user is authenticated
 * @returns {boolean} - True if access token exists
 */
export const isAuthenticated = () => {
  return !!getAuthToken();
};

export default api;
