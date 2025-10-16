import { useState, useEffect } from 'react';

/**
 * Custom hook for password validation with real-time feedback
 *
 * @param {string} password - The password to validate
 * @param {string} confirmPassword - Optional confirm password for matching validation
 * @returns {Object} Validation state and utilities
 */
export function usePasswordValidation(password, confirmPassword = null) {
  const [validation, setValidation] = useState({
    hasMinLength: false,
    hasUpperCase: false,
    hasLowerCase: false,
    hasSpecialChar: false,
    passwordsMatch: null, // null when confirmPassword is not provided
  });
  const [error, setError] = useState('');
  const [isValid, setIsValid] = useState(false);

  // Validate password requirements
  const validatePassword = (value) => {
    if (!value) {
      return {
        hasMinLength: false,
        hasUpperCase: false,
        hasLowerCase: false,
        hasSpecialChar: false,
      };
    }

    return {
      hasMinLength: value.length >= 6,
      hasUpperCase: /[A-Z]/.test(value),
      hasLowerCase: /[a-z]/.test(value),
      hasSpecialChar: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(value),
    };
  };

  // Check if password matches confirm password
  const checkPasswordsMatch = (pass, confirmPass) => {
    if (confirmPass === null || confirmPass === undefined || confirmPass === '') {
      return null;
    }
    return pass === confirmPass;
  };

  // Update validation state when password or confirmPassword changes
  useEffect(() => {
    const requirements = validatePassword(password);
    const passwordsMatch = checkPasswordsMatch(password, confirmPassword);

    setValidation({
      ...requirements,
      passwordsMatch,
    });

    // Check if password meets all requirements
    const allRequirementsMet =
      requirements.hasMinLength &&
      requirements.hasUpperCase &&
      requirements.hasLowerCase &&
      requirements.hasSpecialChar;

    setIsValid(allRequirementsMet);

    // Set error message if password is provided but doesn't meet requirements
    if (password && !allRequirementsMet) {
      const missingRequirements = [];
      if (!requirements.hasMinLength) missingRequirements.push('at least 6 characters');
      if (!requirements.hasUpperCase) missingRequirements.push('one uppercase letter');
      if (!requirements.hasLowerCase) missingRequirements.push('one lowercase letter');
      if (!requirements.hasSpecialChar) missingRequirements.push('one special character');

      setError(`Password must contain ${missingRequirements.join(', ')}`);
    } else {
      setError('');
    }
  }, [password, confirmPassword]);

  return {
    validation,
    error,
    isValid,
  };
}
