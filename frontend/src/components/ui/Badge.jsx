/**
 * Reusable Badge Component
 * Small labels for statuses, categories, etc.
 */

const badgeStyles = {
  variant: {
    default: 'bg-[var(--surface-muted)] text-[var(--text)]',
    primary: 'bg-[var(--accent)]/10 text-[var(--accent)] border border-[var(--accent)]/20',
    success: 'bg-green-500/10 text-green-600 border border-green-500/20',
    warning: 'bg-yellow-500/10 text-yellow-600 border border-yellow-500/20',
    danger: 'bg-red-500/10 text-red-600 border border-red-500/20',
    info: 'bg-blue-500/10 text-blue-600 border border-blue-500/20',
  },
  size: {
    sm: 'px-2 py-0.5 text-xs',
    md: 'px-2.5 py-1 text-sm',
    lg: 'px-3 py-1.5 text-base',
  },
};

/**
 * Badge component
 * @param {string} variant - Badge style (default, primary, success, warning, danger, info)
 * @param {string} size - Badge size (sm, md, lg)
 * @param {ReactNode} children - Badge content
 * @param {string} className - Additional classes
 */
export default function Badge({
  variant = 'default',
  size = 'md',
  children,
  className = '',
  ...props
}) {
  const baseStyles = 'inline-flex items-center rounded-full font-medium';
  const variantStyles = badgeStyles.variant[variant] || badgeStyles.variant.default;
  const sizeStyles = badgeStyles.size[size] || badgeStyles.size.md;

  return (
    <span
      className={`${baseStyles} ${variantStyles} ${sizeStyles} ${className}`.trim()}
      {...props}
    >
      {children}
    </span>
  );
}
