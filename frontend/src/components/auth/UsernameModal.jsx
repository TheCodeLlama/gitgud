import { useState } from 'react';
import { api } from '../../lib/api';
import Button from '../ui/Button';

/**
 * UsernameModal component
 * Modal that prompts new OAuth users to enter a username before completing registration
 */
export default function UsernameModal({ onSubmit, onCancel }) {
  const [username, setUsername] = useState('');
  const [checking, setChecking] = useState(false);
  const [available, setAvailable] = useState(null);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

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

  // Handle username input change
  const handleUsernameChange = (e) => {
    const value = e.target.value;
    setUsername(value);
    setAvailable(null);
    setError('');
  };

  // Handle username blur (check availability when user finishes typing)
  const handleUsernameBlur = () => {
    if (username.trim()) {
      checkAvailability(username.trim());
    }
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();

    const validationError = validateUsername(username);
    if (validationError) {
      setError(validationError);
      return;
    }

    // If we haven't checked availability yet, check now
    if (available === null) {
      await checkAvailability(username.trim());
      return;
    }

    // If username is not available, don't submit
    if (!available) {
      setError('Username is already taken');
      return;
    }

    setSubmitting(true);
    try {
      await onSubmit(username.trim());
    } catch (err) {
      setError(err.message || 'Failed to complete registration. Please try again.');
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-[var(--surface)] border border-[var(--border)] rounded-lg shadow-xl max-w-md w-full mx-4 p-6">
        {/* Header */}
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-[var(--text)] mb-2">Choose Your Username</h2>
          <p className="text-sm text-[var(--text-muted)]">
            Before we continue, please choose a unique username for your account.
          </p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit}>
          {/* Username Input */}
          <div className="mb-6">
            <label htmlFor="username" className="block text-sm font-medium mb-2 text-[var(--text)]">
              Username
            </label>
            <div className="relative">
              <input
                id="username"
                type="text"
                value={username}
                onChange={handleUsernameChange}
                onBlur={handleUsernameBlur}
                className="w-full px-4 py-2 bg-[var(--bg)] border border-[var(--border)] rounded-lg
                         text-[var(--text)] placeholder-[var(--text-muted)]
                         focus:outline-none focus:ring-2 focus:ring-[var(--primary)] focus:border-transparent"
                placeholder="Enter username"
                disabled={submitting}
                autoFocus
              />
              {checking && (
                <div className="absolute right-3 top-1/2 -translate-y-1/2">
                  <div className="w-5 h-5 border-2 border-[var(--primary)] border-t-transparent rounded-full animate-spin" />
                </div>
              )}
            </div>

            {/* Validation Messages */}
            {error && (
              <p className="mt-2 text-sm text-red-500">{error}</p>
            )}
            {available === true && !error && (
              <p className="mt-2 text-sm text-green-500">Username is available!</p>
            )}

            {/* Username Guidelines */}
            <p className="mt-2 text-xs text-[var(--text-muted)]">
              3-100 characters. Letters, numbers, underscores, and hyphens only.
            </p>
          </div>

          {/* Action Buttons */}
          <div className="flex gap-3">
            <Button
              type="button"
              variant="secondary"
              onClick={onCancel}
              disabled={submitting}
              className="flex-1"
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              disabled={submitting || checking || !username || available === false}
              loading={submitting}
              className="flex-1"
            >
              {submitting ? 'Creating Account...' : 'Continue'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
