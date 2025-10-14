/**
 * Reusable Stat Card Component
 * Displays a single statistic with icon matching the original QuickStats design
 */

/**
 * StatCard component
 * @param {string} label - Stat label
 * @param {string|number} value - Stat value
 * @param {React.ReactNode} icon - Icon component
 * @param {string} gradient - Gradient color classes (e.g., 'from-yellow-500 to-amber-500')
 * @param {string} bgColor - Background color class (e.g., 'bg-yellow-500/10')
 * @param {string} borderColor - Border color class (e.g., 'border-yellow-500/20')
 * @param {string} className - Additional classes
 */
export default function StatCard({
  label,
  value,
  icon,
  gradient,
  bgColor,
  borderColor,
  className = '',
  ...props
}) {
  return (
    <div
      className={`${bgColor} ${borderColor} border rounded-lg p-4
                 hover:scale-105 transition-transform duration-200 ${className}`.trim()}
      {...props}
    >
      <div className="flex items-center justify-between mb-2">
        <div className="rounded-lg shadow-sm">
          {icon}
        </div>
      </div>
      <div className="text-2xl font-bold text-[var(--text)] mb-1">
        {value}
      </div>
      <div className="text-xs text-[var(--text-muted)] font-medium">
        {label}
      </div>
    </div>
  );
}
