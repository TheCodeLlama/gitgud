import { useState } from 'react';
import Button from '../shared/Button';
import { useUsernameValidation } from '../../hooks/useUsernameValidation';

/**
 * UsernameModal component
 * Modal that prompts new OAuth users to enter a username before completing registration
 */
export default function UsernameModal({ onSubmit, onCancel }) {
  const [username, setUsername] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Use custom hook for username validation
  const { checking, available, error, validateUsername } = useUsernameValidation(username, 200);

  // Handle username input change
  const handleUsernameChange = (e) => {
    setUsername(e.target.value);
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validate username format
    const validationError = validateUsername(username);
    if (validationError) {
      return;
    }

    // Ensure username is available
    if (available !== true) {
      return;
    }

    setSubmitting(true);
    try {
      await onSubmit(username.trim());
    } catch (err) {
      // Error is handled by the parent component
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
                className="w-full px-4 py-2 bg-[var(--bg)] border border-[var(--border)] rounded-lg
                         text-[var(--text)] placeholder-[var(--text-muted)]
                         focus:outline-none focus:ring-2 focus:ring-[var(--primary)] focus:border-transparent"
                placeholder="Enter username"
                disabled={submitting}
                autoFocus
                autoComplete="off"
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
            {/* Only show cancel button if onCancel is provided */}
            {onCancel && (
              <Button
                type="button"
                variant="secondary"
                onClick={onCancel}
                disabled={submitting}
                className="flex-1"
              >
                Cancel
              </Button>
            )}
            <Button
              type="submit"
              variant="primary"
              disabled={submitting || checking || !username || available === false}
              loading={submitting}
              className={onCancel ? "flex-1" : "w-full"}
            >
              {submitting ? 'Creating Account...' : 'Continue'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
