/**
 * Reusable Achievement Card Component
 * Displays an achievement with icon, details, and rarity
 */

import Badge from './Badge';

const rarityStyles = {
  LEGENDARY: {
    gradient: 'from-amber-500 to-orange-500',
    text: 'text-amber-500',
    variant: 'warning',
  },
  EPIC: {
    gradient: 'from-purple-500 to-pink-500',
    text: 'text-purple-500',
    variant: 'primary',
  },
  RARE: {
    gradient: 'from-blue-500 to-cyan-500',
    text: 'text-blue-500',
    variant: 'info',
  },
  UNCOMMON: {
    gradient: 'from-green-500 to-emerald-500',
    text: 'text-green-500',
    variant: 'success',
  },
  COMMON: {
    gradient: 'from-gray-400 to-gray-500',
    text: 'text-gray-500',
    variant: 'default',
  },
};

/**
 * AchievementCard component
 * @param {string} name - Achievement name
 * @param {string} description - Achievement description
 * @param {string} icon - Achievement icon emoji
 * @param {string} rarity - Rarity level (LEGENDARY, EPIC, RARE, UNCOMMON, COMMON)
 * @param {number} xpReward - XP reward amount
 * @param {string} earnedAt - Date earned (ISO string)
 * @param {boolean} locked - Whether achievement is locked
 * @param {string} className - Additional classes
 */
export default function AchievementCard({
  name,
  description,
  icon = '🏆',
  rarity = 'COMMON',
  xpReward = 0,
  earnedAt,
  locked = false,
  className = '',
  onClick,
  ...props
}) {
  const styles = rarityStyles[rarity] || rarityStyles.COMMON;

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;
    if (diffDays < 30) return `${Math.floor(diffDays / 7)} weeks ago`;
    return date.toLocaleDateString();
  };

  return (
    <div
      className={`flex items-start gap-3 p-3 bg-[var(--bg)] border border-[var(--border)]
                 rounded-lg hover:border-[var(--accent)]/50 transition-all group
                 ${locked ? 'opacity-50 grayscale' : ''} ${onClick ? 'cursor-pointer' : ''} ${className}`.trim()}
      onClick={onClick}
      {...props}
    >
      {/* Achievement icon with rarity gradient */}
      <div className={`flex-shrink-0 w-12 h-12 rounded-lg bg-gradient-to-br ${styles.gradient}
                    flex items-center justify-center text-2xl shadow-lg
                    ${locked ? 'grayscale' : ''}`}>
        {locked ? '🔒' : icon}
      </div>

      {/* Achievement details */}
      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between gap-2 mb-1">
          <h4 className="font-semibold text-[var(--text)] group-hover:text-[var(--accent)] transition-colors">
            {name}
          </h4>
          {!locked && xpReward > 0 && (
            <Badge variant="primary" size="sm">
              +{xpReward} XP
            </Badge>
          )}
        </div>

        <p className="text-sm text-[var(--text-muted)] line-clamp-2 mb-2">
          {description}
        </p>

        <div className="flex items-center gap-3 text-xs">
          <span className={`font-medium uppercase ${styles.text}`}>
            {rarity}
          </span>
          {earnedAt && (
            <>
              <span className="text-[var(--text-muted)]">•</span>
              <span className="text-[var(--text-muted)]">
                {formatDate(earnedAt)}
              </span>
            </>
          )}
          {locked && (
            <>
              <span className="text-[var(--text-muted)]">•</span>
              <span className="text-[var(--text-muted)]">Locked</span>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
