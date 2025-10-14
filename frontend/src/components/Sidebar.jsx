/**
 * Sidebar navigation component
 * - Navigation links for main sections
 * - Progress stats widget showing XP, level, and streak
 * - Collapsible on mobile
 * - Responsive design with smooth transitions
 */

import { LayoutDashboard, Library, User, Trophy, Flame, Lightbulb, Sparkles } from 'lucide-react';
import { Link, useLocation } from 'react-router';
import { useUserStats } from '../hooks/useUserStats';
import ProgressBar from './ui/ProgressBar';
import { StatsSkeleton } from './skeletons/Skeleton';

/**
 * Sidebar Component
 * @param {boolean} isOpen - Whether sidebar is open (mobile)
 * @param {function} onClose - Callback to close sidebar (mobile)
 */
export default function Sidebar({ isOpen = true, onClose = () => {} }) {
  const location = useLocation();
  const { data: stats, isLoading } = useUserStats();

  // Navigation items
  const navigationItems = [
    {
      name: 'Dashboard',
      path: '/dashboard',
      icon: LayoutDashboard,
      description: 'Overview and quick stats',
    },
    {
      name: 'Modules',
      path: '/modules',
      icon: Library,
      description: 'Browse learning modules',
    },
    {
      name: 'Profile',
      path: '/profile',
      icon: User,
      description: 'View your profile',
    },
    {
      name: 'Achievements',
      path: '/achievements',
      icon: Trophy,
      description: 'View your achievements',
    },
  ];

  const isActivePath = (path) => location.pathname === path;

  const handleLinkClick = () => {
    // Close sidebar on mobile when link is clicked
    if (window.innerWidth < 1024) {
      onClose();
    }
  };

  return (
    <>
      {/* Mobile backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-30 lg:hidden"
          onClick={onClose}
          aria-hidden="true"
        />
      )}

      {/* Sidebar */}
      <aside
        className={`
          fixed lg:sticky top-0 left-0 z-40
          h-screen lg:h-[calc(100vh-4rem)]
          w-64 lg:w-72
          bg-[var(--surface)] border-r border-[var(--border)]
          transform transition-transform duration-300 ease-in-out
          ${isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
          overflow-y-auto
        `}
      >
        <div className="p-4 space-y-6">
          {/* Close button (mobile only) */}
          <div className="lg:hidden flex justify-end">
            <button
              onClick={onClose}
              className="p-2 rounded-md hover:bg-[var(--surface-muted)] transition-colors"
              aria-label="Close sidebar"
            >
              <svg
                className="h-6 w-6 text-[var(--text-muted)]"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth="1.5"
                stroke="currentColor"
              >
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          {/* Navigation links */}
          <nav className="space-y-1">
            {navigationItems.map((item) => {
              const Icon = item.icon;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  onClick={handleLinkClick}
                  className={`
                    flex items-center gap-3 px-4 py-3 rounded-lg
                    transition-all duration-200
                    ${
                      isActivePath(item.path)
                        ? 'bg-[var(--accent)]/10 text-[var(--accent)] border-l-4 border-[var(--accent)]'
                        : 'text-[var(--text-muted)] hover:bg-[var(--surface-muted)] hover:text-[var(--text)] border-l-4 border-transparent'
                    }
                  `}
                  title={item.description}
                >
                  <Icon className="w-6 h-6" aria-hidden="true" />
                  <div className="flex-1">
                    <div className="font-medium">{item.name}</div>
                    <div className="text-xs text-[var(--text-muted)]">{item.description}</div>
                  </div>
                </Link>
              );
            })}
          </nav>

          {/* Progress Stats Widget */}
          <div className="p-4 bg-[var(--bg)] border border-[var(--border)] rounded-lg">
            <h3 className="text-sm font-semibold text-[var(--text)] mb-4">Your Progress</h3>

            {isLoading ? (
              <StatsSkeleton />
            ) : stats ? (
              <div className="space-y-4">
                {/* Level display */}
                <div className="flex items-center justify-between">
                  <span className="text-sm text-[var(--text-muted)]">Level</span>
                  <span className="text-lg font-bold text-[var(--accent)]">
                    {stats.currentLevel}
                  </span>
                </div>

                {/* XP progress bar */}
                <ProgressBar
                  value={stats.currentLevelXp || 0}
                  max={stats.xpToNextLevel}
                  showLabel
                  label="XP"
                  size="sm"
                  color="accent"
                />

                {/* Stats grid */}
                <div className="grid grid-cols-2 gap-3 pt-2 border-t border-[var(--border)]">
                  {/* Total XP */}
                  <div className="text-center">
                    <div className="text-xs text-[var(--text-muted)] mb-1">Total XP</div>
                    <div className="text-lg font-semibold text-[var(--text)]">
                      {stats.totalXp.toLocaleString()}
                    </div>
                  </div>

                  {/* Current Streak */}
                  <div className="text-center">
                    <div className="text-xs text-[var(--text-muted)] mb-1">Streak</div>
                    <div className="text-lg font-semibold text-[var(--text)] flex items-center justify-center gap-1">
                      <Flame className="w-5 h-5 text-orange-500" />
                      {stats.currentStreakDays}
                    </div>
                  </div>
                </div>

                {/* Motivational message */}
                {stats.currentStreakDays > 0 && (
                  <div className="text-xs text-center text-[var(--text-muted)] pt-2 flex items-center justify-center gap-1">
                    {stats.currentStreakDays >= 7 ? (
                      <>
                        <Sparkles className="w-3 h-3" />
                        Amazing streak! Keep it up!
                      </>
                    ) : (
                      'Keep learning every day!'
                    )}
                  </div>
                )}
              </div>
            ) : (
              <div className="text-sm text-center text-[var(--text-muted)] py-4">
                Start learning to see your progress!
              </div>
            )}
          </div>
        </div>
      </aside>
    </>
  );
}
