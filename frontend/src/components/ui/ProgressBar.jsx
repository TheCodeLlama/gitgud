/**
 * Reusable Progress Bar Component
 * Animated progress indicator
 */

/**
 * ProgressBar component
 * @param {number} value - Current progress value
 * @param {number} max - Maximum value
 * @param {boolean} showPercentage - Show percentage text inside bar
 * @param {boolean} showLabel - Show label above bar
 * @param {string} label - Custom label text
 * @param {string} size - Bar height (sm, md, lg)
 * @param {string} color - Progress bar color variant
 * @param {string} className - Additional classes
 */
export default function ProgressBar({
  value = 0,
  max = 100,
  showPercentage = false,
  showLabel = false,
  label = '',
  size = 'md',
  color = 'accent',
  className = '',
  ...props
}) {
  const percentage = Math.min((value / max) * 100, 100);

  const sizeStyles = {
    sm: 'h-2',
    md: 'h-3',
    lg: 'h-4',
  };

  const colorStyles = {
    accent: 'bg-[var(--accent)]',
    success: 'bg-green-500',
    warning: 'bg-yellow-500',
    danger: 'bg-red-500',
  };

  const heightClass = sizeStyles[size] || sizeStyles.md;
  const colorClass = colorStyles[color] || colorStyles.accent;

  return (
    <div className={className}>
      {/* Label */}
      {showLabel && (
        <div className="flex items-center justify-between text-sm mb-2">
          <span className="text-[var(--text-muted)]">{label}</span>
          <span className="font-medium text-[var(--accent)]">
            {value} / {max}
          </span>
        </div>
      )}

      {/* Progress bar container */}
      <div
        className={`relative ${heightClass} bg-[var(--surface-muted)] rounded-full overflow-hidden border border-[var(--border)]`}
      >
        {/* Progress fill */}
        <div
          className={`absolute inset-y-0 left-0 ${colorClass} rounded-full`}
          style={{ width: `${percentage}%` }}
          role="progressbar"
          aria-valuenow={value}
          aria-valuemin="0"
          aria-valuemax={max}
          {...props}
        />


        {/* Percentage text */}
        {showPercentage && percentage > 15 && (
          <div className="absolute inset-0 flex items-center px-3">
            <span className="text-xs font-semibold text-white drop-shadow-md">
              {Math.round(percentage)}%
            </span>
          </div>
        )}
      </div>
    </div>
  );
}
