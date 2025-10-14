/**
 * Navbar Stats Component
 * Displays level, XP progress bar, total XP, and streak in navbar
 */

import { Flame } from 'lucide-react';
import { Link } from 'react-router';

/**
 * NavbarStats component
 * @param {object} stats - User stats object with currentLevel, currentLevelXp, xpToNextLevel, totalXp, currentStreakDays
 */
export default function NavbarStats({ stats }) {
  if (!stats) return null;

  const progressPercent = ((stats.currentLevelXp || 0) / stats.xpToNextLevel) * 100;

  return (
    <div className="flex items-center gap-4">
      {/* Level, Progress Bar, and XP Display */}
      <Link
        to="/profile"
        className="flex items-center gap-3 px-3 py-2 bg-[var(--accent)]/10 rounded-lg
                 hover:bg-[var(--accent)]/20 transition-colors group"
        title="View your profile"
      >
        {/* Level */}
        <div className="flex items-center gap-1">
          <span className="text-xs font-medium text-[var(--text-muted)]">LVL</span>
          <span className="text-lg font-bold text-[var(--accent)]">
            {stats.currentLevel}
          </span>
        </div>

        {/* Progress bar */}
        <div className="flex flex-col justify-center">
          <div className="w-20 h-1.5 bg-[var(--surface-muted)] rounded-full overflow-hidden">
            <div
              className="h-full bg-gradient-to-r from-[var(--accent)] to-[var(--accent-hover)] rounded-full transition-all duration-300"
              style={{ width: `${progressPercent}%` }}
              role="progressbar"
              aria-valuenow={stats.currentLevelXp || 0}
              aria-valuemin="0"
              aria-valuemax={stats.xpToNextLevel}
              aria-label={`Level progress: ${Math.round(progressPercent)}%`}
            />
          </div>
        </div>

        {/* Total XP */}
        <div className="text-xs text-[var(--text-muted)] group-hover:text-[var(--text)]">
          {stats.totalXp.toLocaleString()} XP
        </div>
      </Link>

      {/* Streak indicator */}
      {stats.currentStreakDays > 0 && (
        <div
          className="flex items-center gap-1.5 px-3 py-1.5 bg-orange-500/10 rounded-lg"
          title={`${stats.currentStreakDays} day streak!`}
        >
          <Flame className="w-5 h-5 text-orange-500" />
          <span className="text-sm font-semibold text-orange-500">
            {stats.currentStreakDays}
          </span>
        </div>
      )}
    </div>
  );
}
