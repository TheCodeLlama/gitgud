/**
 * Reusable Button Component
 * Supports multiple variants and sizes for consistent styling across the app
 */

import { Link } from 'react-router';

const buttonStyles = {
  variant: {
    primary: 'bg-[var(--accent)] text-[var(--bg)] hover:opacity-90',
    secondary: 'bg-[var(--surface)] text-[var(--text)] border border-[var(--border)] hover:bg-[var(--surface-muted)]',
    outline: 'bg-transparent text-[var(--accent)] border-2 border-[var(--accent)] hover:bg-[var(--accent)] hover:text-[var(--bg)]',
    ghost: 'bg-transparent text-[var(--text)] hover:bg-[var(--surface-muted)]',
    danger: 'bg-red-500 text-white hover:bg-red-600',
  },
  size: {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-base',
    lg: 'px-6 py-3 text-lg',
  },
  fullWidth: 'w-full',
};

/**
 * Button component
 * @param {string} variant - Button style variant (primary, secondary, outline, ghost, danger)
 * @param {string} size - Button size (sm, md, lg)
 * @param {boolean} fullWidth - Make button full width
 * @param {string} to - If provided, renders as Link
 * @param {function} onClick - Click handler
 * @param {boolean} disabled - Disabled state
 * @param {ReactNode} children - Button content
 * @param {string} className - Additional classes
 */
export default function Button({
  variant = 'primary',
  size = 'md',
  fullWidth = false,
  to,
  onClick,
  disabled = false,
  children,
  className = '',
  type = 'button',
  ...props
}) {
  const baseStyles = 'rounded-lg font-medium transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed';
  const variantStyles = buttonStyles.variant[variant] || buttonStyles.variant.primary;
  const sizeStyles = buttonStyles.size[size] || buttonStyles.size.md;
  const widthStyles = fullWidth ? buttonStyles.fullWidth : '';

  const combinedClassName = `${baseStyles} ${variantStyles} ${sizeStyles} ${widthStyles} ${className}`.trim();

  if (to && !disabled) {
    return (
      <Link to={to} className={combinedClassName} {...props}>
        {children}
      </Link>
    );
  }

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={combinedClassName}
      {...props}
    >
      {children}
    </button>
  );
}
