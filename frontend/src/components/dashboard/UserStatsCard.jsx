/**
 * User Stats Card Component
 * Displays current level, XP, progress to next level, and streak using modular components
 */

import { Flame, Sparkles } from 'lucide-react';
import { useUserStats } from '../../hooks/useUserStats';
import { useLevelProgress } from '../../hooks/useLevelProgress';
import Card, { CardBody } from '../ui/Card';
import ProgressBar from '../ui/ProgressBar';
import { StatsSkeleton } from '../skeletons/Skeleton';

export default function UserStatsCard() {
  const { data: stats, isLoading: statsLoading } = useUserStats();
  const { data: levelProgress, isLoading: progressLoading } = useLevelProgress();

  const isLoading = statsLoading || progressLoading;

  if (isLoading) {
    return (
      <Card>
        <CardBody>
          <StatsSkeleton />
        </CardBody>
      </Card>
    );
  }

  if (!stats) {
    return (
      <Card>
        <CardBody className="text-center">
          <p className="text-[var(--text-muted)]">No stats available</p>
        </CardBody>
      </Card>
    );
  }

  const progressPercent = levelProgress
    ? (levelProgress.currentXp / levelProgress.xpRequired) * 100
    : ((stats.currentLevelXp || 0) / stats.xpToNextLevel) * 100;

  return (
    <div className="bg-gradient-to-br from-[var(--surface)] to-[var(--surface-muted)] border border-[var(--border)] rounded-lg p-6 shadow-lg">
      <h2 className="text-xl font-semibold text-[var(--text)] mb-6">Your Stats</h2>

      {/* Level and XP Grid */}
      <div className="grid grid-cols-2 gap-4 mb-6">
        {/* Current Level */}
        <div className="bg-[var(--bg)] rounded-lg p-4 text-center border border-[var(--accent)]/20">
          <div className="text-sm text-[var(--text-muted)] mb-2">Level</div>
          <div className="text-4xl font-bold text-[var(--accent)]">
            {stats.currentLevel}
          </div>
          <div className="text-xs text-[var(--text-muted)] mt-2">
            {levelProgress?.levelTitle || 'Novice'}
          </div>
        </div>

        {/* Current Streak */}
        <div className="bg-[var(--bg)] rounded-lg p-4 text-center border border-orange-500/20">
          <div className="text-sm text-[var(--text-muted)] mb-2">Streak</div>
          <div className="text-4xl font-bold flex items-center justify-center gap-2">
            <Flame className="w-9 h-9 text-orange-500" />
            <span className="text-[var(--text)]">{stats.currentStreakDays}</span>
          </div>
          <div className="text-xs text-[var(--text-muted)] mt-2">
            {stats.currentStreakDays === 1 ? 'day' : 'days'}
          </div>
        </div>
      </div>

      {/* XP Progress using ProgressBar component */}
      <ProgressBar
        value={stats.currentLevelXp || 0}
        max={stats.xpToNextLevel}
        showPercentage={progressPercent > 15}
        showLabel
        label={`Progress to Level ${stats.currentLevel + 1}`}
        size="lg"
        color="accent"
        className="mb-3"
      />

      {/* XP Needed */}
      <div className="text-xs text-center text-[var(--text-muted)] mb-6">
        {stats.xpToNextLevel - (stats.currentLevelXp || 0)} XP needed to level up
      </div>

      {/* Total XP Badge */}
      <div className="pt-4 border-t border-[var(--border)] text-center">
        <div className="text-xs text-[var(--text-muted)] mb-1">Total Experience</div>
        <div className="text-2xl font-bold text-[var(--text)]">
          {stats.totalXp.toLocaleString()} <span className="text-base text-[var(--text-muted)]">XP</span>
        </div>
      </div>

      {/* Motivational message */}
      {stats.currentStreakDays >= 7 && (
        <div className="mt-4 p-3 bg-orange-500/10 border border-orange-500/20 rounded-lg text-center">
          <span className="text-sm text-orange-600 dark:text-orange-400 flex items-center justify-center gap-2">
            <Sparkles className="w-4 h-4" />
            Amazing! You're on fire with your {stats.currentStreakDays}-day streak!
          </span>
        </div>
      )}
    </div>
  );
}
