/**
 * Reusable Button Component
 * Supports multiple variants and sizes for consistent styling across the app
 */

import { Link } from 'react-router';

const buttonStyles = {
  base: 'inline-flex items-center justify-center rounded-md font-medium transition-colors',
  variant: {
    primary: 'bg-transparent text-[var(--accent)] border-2 border-[var(--accent)] hover:bg-[var(--accent)] hover:text-[var(--bg)]',
    secondary: 'bg-[var(--surface-muted)] text-[var(--text)] hover:bg-[var(--border)] border border-[var(--border)]',
    outline: 'bg-transparent text-[var(--accent)] border-2 border-[var(--accent)] hover:bg-[var(--accent)] hover:text-[var(--bg)]',
    ghost: 'bg-transparent text-[var(--text)] hover:bg-[var(--surface-muted)]',
    danger: 'bg-red-500 text-white hover:bg-red-600 border-2 border-red-500',
  },
  size: {
    sm: 'px-4 h-8 text-sm',
    md: 'px-6 h-10 text-base',
    lg: 'px-8 h-12 text-lg',
  },
  disabled: 'bg-[var(--surface-muted)] text-[var(--text-muted)] border border-[var(--border)] opacity-50 cursor-not-allowed',
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
  const baseStyles = buttonStyles.base;
  const variantStyles = disabled ? buttonStyles.disabled : (buttonStyles.variant[variant] || buttonStyles.variant.primary);
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
