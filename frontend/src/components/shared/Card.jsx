/**
 * Reusable Card Component
 * Base card with consistent styling
 */

/**
 * Card component
 * @param {ReactNode} children - Card content
 * @param {string} className - Additional classes
 * @param {boolean} hover - Enable hover effect
 * @param {function} onClick - Click handler (makes card interactive)
 */
export default function Card({
  children,
  className = '',
  hover = false,
  onClick,
  ...props
}) {
  const baseStyles = 'bg-[var(--surface)] border border-[var(--border)] rounded-lg';
  const hoverStyles = hover ? 'hover:border-[var(--accent)] hover:shadow-lg transition-all cursor-pointer' : '';
  const clickStyles = onClick ? 'cursor-pointer' : '';

  return (
    <div
      className={`${baseStyles} ${hoverStyles} ${clickStyles} ${className}`.trim()}
      onClick={onClick}
      {...props}
    >
      {children}
    </div>
  );
}

/**
 * Card Header
 */
export function CardHeader({ children, className = '' }) {
  return (
    <div className={`p-6 border-b border-[var(--border)] ${className}`.trim()}>
      {children}
    </div>
  );
}

/**
 * Card Body
 */
export function CardBody({ children, className = '' }) {
  return (
    <div className={`p-6 ${className}`.trim()}>
      {children}
    </div>
  );
}

/**
 * Card Footer
 */
export function CardFooter({ children, className = '' }) {
  return (
    <div className={`p-6 border-t border-[var(--border)] ${className}`.trim()}>
      {children}
    </div>
  );
}
