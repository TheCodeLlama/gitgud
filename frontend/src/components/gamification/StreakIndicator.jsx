/**
 * Streak Indicator Component
 * Displays current learning streak with visual feedback
 */

import { Flame, AlertTriangle } from 'lucide-react';

/**
 * StreakIndicator component
 * @param {number} currentStreak - Current streak in days
 * @param {number} longestStreak - Longest streak ever achieved
 * @param {boolean} isAtRisk - Whether streak is at risk of breaking
 * @param {string} lastActivityDate - ISO date string of last activity
 * @param {string} size - Size variant (sm, md, lg)
 * @param {boolean} showLabel - Show "day streak" label
 * @param {boolean} showLongest - Show longest streak
 * @param {string} variant - Display variant (compact, full)
 * @param {string} className - Additional CSS classes
 */
export default function StreakIndicator({
  currentStreak = 0,
  longestStreak = 0,
  isAtRisk = false,
  lastActivityDate = null,
  size = 'md',
  showLabel = true,
  showLongest = false,
  variant = 'compact',
  className = '',
}) {
  // Check if streak is at risk (no activity today)
  const checkStreakRisk = () => {
    if (!lastActivityDate) return true;
    const lastActivity = new Date(lastActivityDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    lastActivity.setHours(0, 0, 0, 0);
    const daysDiff = Math.floor((today - lastActivity) / (1000 * 60 * 60 * 24));
    return daysDiff >= 1;
  };

  const atRisk = isAtRisk || checkStreakRisk();

  // Size styles
  const sizeStyles = {
    sm: {
      icon: 'w-4 h-4',
      text: 'text-sm',
      number: 'text-lg',
      container: 'p-2',
    },
    md: {
      icon: 'w-5 h-5',
      text: 'text-base',
      number: 'text-2xl',
      container: 'p-3',
    },
    lg: {
      icon: 'w-6 h-6',
      text: 'text-lg',
      number: 'text-3xl',
      container: 'p-4',
    },
  };

  const styles = sizeStyles[size] || sizeStyles.md;

  // Streak color based on length
  const getStreakColor = () => {
    if (currentStreak === 0) return 'text-gray-400';
    if (atRisk) return 'text-orange-500';
    if (currentStreak >= 30) return 'text-purple-500';
    if (currentStreak >= 14) return 'text-blue-500';
    if (currentStreak >= 7) return 'text-green-500';
    return 'text-orange-500';
  };

  const getStreakBgColor = () => {
    if (currentStreak === 0) return 'bg-gray-500/10';
    if (atRisk) return 'bg-orange-500/10';
    if (currentStreak >= 30) return 'bg-purple-500/10';
    if (currentStreak >= 14) return 'bg-blue-500/10';
    if (currentStreak >= 7) return 'bg-green-500/10';
    return 'bg-orange-500/10';
  };

  const streakColor = getStreakColor();
  const streakBgColor = getStreakBgColor();

  // Compact variant (icon + number)
  if (variant === 'compact') {
    return (
      <div className={`inline-flex items-center gap-2 ${className}`}>
        <div className={`relative ${atRisk ? 'animate-pulse' : ''}`}>
          <Flame
            className={`${styles.icon} ${streakColor} ${
              currentStreak > 0 && !atRisk ? 'animate-flicker' : ''
            }`}
          />
          {atRisk && currentStreak > 0 && (
            <AlertTriangle className="absolute -top-1 -right-1 w-3 h-3 text-orange-500" />
          )}
        </div>
        <div className="flex flex-col">
          <span className={`${styles.number} font-bold ${streakColor} leading-none`}>
            {currentStreak}
          </span>
          {showLabel && (
            <span className="text-xs text-[var(--text-muted)] leading-none">
              {currentStreak === 1 ? 'day' : 'days'}
            </span>
          )}
        </div>
      </div>
    );
  }

  // Full variant (card with details)
  return (
    <div className={`${streakBgColor} border border-[var(--border)] rounded-lg ${styles.container} ${className}`}>
      {/* Header */}
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <Flame className={`${styles.icon} ${streakColor} ${currentStreak > 0 && !atRisk ? 'animate-flicker' : ''}`} />
          <h3 className="font-bold text-[var(--text)]">Streak</h3>
        </div>
        {atRisk && currentStreak > 0 && (
          <div className="flex items-center gap-1 px-2 py-1 bg-orange-500/20 border border-orange-500/50 rounded text-xs text-orange-500 font-medium">
            <AlertTriangle className="w-3 h-3" />
            At Risk
          </div>
        )}
      </div>

      {/* Current Streak */}
      <div className="flex items-baseline gap-2 mb-2">
        <span className={`${styles.number} font-bold ${streakColor}`}>{currentStreak}</span>
        <span className={`${styles.text} text-[var(--text-muted)]`}>
          {currentStreak === 1 ? 'day' : 'days'}
        </span>
      </div>

      {/* Message */}
      {atRisk && currentStreak > 0 ? (
        <p className="text-xs text-orange-500 mb-2">
          Complete a lesson today to maintain your streak!
        </p>
      ) : currentStreak > 0 ? (
        <p className="text-xs text-[var(--success)] mb-2">
          Great work! Keep it going!
        </p>
      ) : (
        <p className="text-xs text-[var(--text-muted)] mb-2">
          Start your learning streak today!
        </p>
      )}

      {/* Longest Streak */}
      {showLongest && longestStreak > 0 && (
        <div className="pt-2 border-t border-[var(--border)]">
          <div className="flex items-center justify-between">
            <span className="text-xs text-[var(--text-muted)]">Longest Streak</span>
            <span className="text-sm font-bold text-[var(--text)]">
              {longestStreak} {longestStreak === 1 ? 'day' : 'days'}
            </span>
          </div>
        </div>
      )}

      {/* Streak Milestones */}
      {currentStreak > 0 && (
        <div className="pt-2 border-t border-[var(--border)] mt-2">
          <div className="flex items-center justify-between text-xs">
            <span className="text-[var(--text-muted)]">Next Milestone</span>
            <span className="font-bold text-[var(--primary)]">
              {currentStreak < 7
                ? `${7 - currentStreak} days to 🔥 Week Warrior`
                : currentStreak < 14
                ? `${14 - currentStreak} days to 🔥 Two Weeks`
                : currentStreak < 30
                ? `${30 - currentStreak} days to 🔥 Month Master`
                : '🎉 Legendary!'}
            </span>
          </div>
        </div>
      )}
    </div>
  );
}
