import { useState, useEffect } from 'react';
import { api } from '../lib/api';

/**
 * Custom hook for username validation with debounced availability checking
 *
 * @param {string} username - The username to validate
 * @param {number} debounceMs - Debounce delay in milliseconds (default: 200)
 * @returns {Object} Validation state and utilities
 */
export function useUsernameValidation(username, debounceMs = 200) {
  const [checking, setChecking] = useState(false);
  const [available, setAvailable] = useState(null);
  const [error, setError] = useState('');

  // Validate username format
  const validateUsername = (value) => {
    if (!value || value.trim().length === 0) {
      return 'Username is required';
    }
    if (value.length < 3) {
      return 'Username must be at least 3 characters';
    }
    if (value.length > 100) {
      return 'Username must be less than 100 characters';
    }
    if (!/^[a-zA-Z0-9_-]+$/.test(value)) {
      return 'Username can only contain letters, numbers, underscores, and hyphens';
    }
    return null;
  };

  // Check username availability with backend
  const checkAvailability = async (value) => {
    const validationError = validateUsername(value);
    if (validationError) {
      setError(validationError);
      setAvailable(null);
      return;
    }

    setChecking(true);
    setError('');

    try {
      const response = await api.get(`/v1/auth/username/check`, {
        params: { username: value },
      });

      const isAvailable = response.data.data;
      setAvailable(isAvailable);

      if (!isAvailable) {
        setError('Username is already taken');
      }
    } catch (err) {
      console.error('Failed to check username availability:', err);
      setError('Failed to check username availability. Please try again.');
      setAvailable(null);
    } finally {
      setChecking(false);
    }
  };

  // Debounced username validation - checks after user stops typing
  useEffect(() => {
    // Don't check if username is empty
    if (!username || username.trim().length === 0) {
      setError('');
      setAvailable(null);
      return;
    }

    // First do instant format validation
    const validationError = validateUsername(username);
    if (validationError) {
      setError(validationError);
      setAvailable(null);
      return;
    }

    // Clear any previous error from format validation
    setError('');

    // Debounce the API call
    const timeoutId = setTimeout(() => {
      checkAvailability(username.trim());
    }, debounceMs);

    // Cleanup: cancel the timeout if username changes again
    return () => clearTimeout(timeoutId);
  }, [username, debounceMs]);

  return {
    checking,
    available,
    error,
    validateUsername,
  };
}
