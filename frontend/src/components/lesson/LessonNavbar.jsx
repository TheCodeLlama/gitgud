import { useState } from 'react';
import { Link, useNavigate } from 'react-router';
import { useAuth } from '../../contexts/AuthContext';
import { useUserStats } from '../../hooks/useUserStats';
import { Flame, ChevronLeft, ChevronRight } from 'lucide-react';

/**
 * Slim navbar for lesson pages
 * - Minimal design to maximize coding space
 * - Only essential elements: logo, navigation, XP/streak, user menu
 * - Compact height (48px vs 64px)
 */
export default function LessonNavbar({
  lesson,
  currentLessonIndex,
  totalLessons,
  onBack,
  previousLesson,
  nextLesson,
  onNavigateToLesson,
}) {
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { data: stats } = useUserStats();

  const handleLogout = async () => {
    setProfileMenuOpen(false);
    await logout();
    navigate('/signin');
  };

  // Calculate XP progress percentage
  const progressPercent = stats && stats.xpForCurrentLevel > 0
    ? ((stats.currentLevelXp || 0) / stats.xpForCurrentLevel) * 100
    : 0;

  return (
    <nav className="bg-[var(--surface)] border-b border-[var(--border)] h-12 flex items-center px-4 justify-between gap-4">
      {/* Left side - Logo and navigation */}
      <div className="flex items-center gap-2 lg:gap-3">
        {/* Logo - clickable to dashboard */}
        <Link to="/dashboard" className="flex shrink-0 items-center">
          <div className="bg-[var(--accent)] rounded px-2 py-1 flex items-center justify-center">
            <span className="text-sm font-bold text-[var(--bg)]">{'{GG}'}</span>
          </div>
        </Link>

        {/* Divider */}
        <div className="h-6 w-px bg-[var(--border)]" />

        {/* Navigation buttons */}
        <div className="flex items-center gap-1">
          {/* Back button */}
          <button
            onClick={onBack}
            className="flex items-center gap-1 px-2 py-1 text-xs lg:text-sm text-[var(--text-muted)] hover:text-[var(--text)] hover:bg-[var(--surface-muted)] rounded transition-colors"
          >
            <ChevronLeft className="w-4 h-4" />
            <span className="hidden sm:inline">Modules</span>
          </button>

          {/* Previous lesson button */}
          {previousLesson && (
            <button
              onClick={() => onNavigateToLesson(previousLesson.id)}
              className="flex items-center gap-1 px-2 py-1 text-xs lg:text-sm text-[var(--text-muted)] hover:text-[var(--text)] hover:bg-[var(--surface-muted)] rounded transition-colors"
              title={`Previous: ${previousLesson.title}`}
            >
              <ChevronLeft className="w-4 h-4" />
              <span className="hidden lg:inline">Previous Lesson</span>
            </button>
          )}

          {/* Next lesson button */}
          {nextLesson && (
            <button
              onClick={() => onNavigateToLesson(nextLesson.id)}
              className="flex items-center gap-1 px-2 py-1 text-xs lg:text-sm text-[var(--text-muted)] hover:text-[var(--text)] hover:bg-[var(--surface-muted)] rounded transition-colors"
              title={`Next: ${nextLesson.title}`}
            >
              <span className="hidden lg:inline">Next Lesson</span>
              <ChevronRight className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>

      {/* Center - XP Progress and Streak */}
      {stats && (
        <div className="hidden md:flex items-center gap-3">
          {/* XP Progress */}
          <Link
            to="/profile"
            className="flex items-center gap-2 px-2 py-1 bg-[var(--accent)]/10 rounded hover:bg-[var(--accent)]/20 transition-colors"
            title="View your profile"
          >
            {/* Level */}
            <div className="flex items-center gap-1">
              <span className="text-xs font-medium text-[var(--text-muted)]">LVL</span>
              <span className="text-sm font-bold text-[var(--accent)]">
                {stats.currentLevel}
              </span>
            </div>

            {/* Progress bar */}
            <div className="w-16 lg:w-20 h-1.5 bg-[var(--surface-muted)] rounded-full overflow-hidden">
              <div
                className="h-full bg-gradient-to-r from-[var(--accent)] to-[var(--accent-hover)] rounded-full transition-all duration-300"
                style={{ width: `${progressPercent}%` }}
              />
            </div>

            {/* Total XP */}
            <span className="text-xs text-[var(--text-muted)]">
              {stats.totalXp.toLocaleString()} XP
            </span>
          </Link>

          {/* Streak */}
          {stats.currentStreakDays > 0 && (
            <div
              className="flex items-center gap-1.5 px-2 py-1 bg-orange-500/10 rounded"
              title={`${stats.currentStreakDays} day streak!`}
            >
              <Flame className="w-4 h-4 text-orange-500" />
              <span className="text-sm font-semibold text-orange-500">
                {stats.currentStreakDays}
              </span>
            </div>
          )}
        </div>
      )}

      {/* Right side - User menu */}
      <div className="relative">
        <button
          onClick={() => setProfileMenuOpen(!profileMenuOpen)}
          className="flex items-center gap-2 rounded-md px-2 py-1 text-sm hover:bg-[var(--surface-muted)] transition-colors"
        >
          <span className="hidden sm:inline text-[var(--text)] text-sm">
            {user?.username}
          </span>
          <div className="h-7 w-7 rounded-full bg-[var(--accent)] flex items-center justify-center text-[var(--bg)] font-semibold text-sm">
            {(user?.username?.[0] || 'U').toUpperCase()}
          </div>
        </button>

        {/* Profile dropdown */}
        {profileMenuOpen && (
          <>
            {/* Backdrop */}
            <div
              className="fixed inset-0 z-10"
              onClick={() => setProfileMenuOpen(false)}
            />
            <div className="absolute right-0 z-20 mt-2 w-56 origin-top-right rounded-md bg-[var(--surface)] border border-[var(--border)] shadow-lg">
              {/* User info header */}
              {stats && (
                <div className="px-4 py-3 border-b border-[var(--border)]">
                  <div className="text-sm font-medium text-[var(--text)]">
                    {user?.username || 'User'}
                  </div>
                  <div className="flex items-center gap-3 mt-2 text-xs text-[var(--text-muted)]">
                    <span>Level {stats.currentLevel}</span>
                    <span>•</span>
                    <span>{stats.totalXp.toLocaleString()} XP</span>
                    {stats.currentStreakDays > 0 && (
                      <>
                        <span>•</span>
                        <span className="flex items-center gap-1">
                          <Flame className="w-3 h-3 text-orange-500" />
                          {stats.currentStreakDays}
                        </span>
                      </>
                    )}
                  </div>
                </div>
              )}

              <div className="py-1">
                <Link
                  to="/dashboard"
                  onClick={() => setProfileMenuOpen(false)}
                  className="block px-4 py-2 text-sm text-[var(--text-muted)] hover:bg-[var(--surface-muted)] hover:text-[var(--text)]"
                >
                  Dashboard
                </Link>
                <Link
                  to="/profile"
                  onClick={() => setProfileMenuOpen(false)}
                  className="block px-4 py-2 text-sm text-[var(--text-muted)] hover:bg-[var(--surface-muted)] hover:text-[var(--text)]"
                >
                  Your Profile
                </Link>
                <Link
                  to="/settings"
                  onClick={() => setProfileMenuOpen(false)}
                  className="block px-4 py-2 text-sm text-[var(--text-muted)] hover:bg-[var(--surface-muted)] hover:text-[var(--text)]"
                >
                  Settings
                </Link>
                <button
                  onClick={handleLogout}
                  className="block w-full text-left px-4 py-2 text-sm text-[var(--text-muted)] hover:bg-[var(--surface-muted)] hover:text-[var(--text)]"
                >
                  Sign Out
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </nav>
  );
}
