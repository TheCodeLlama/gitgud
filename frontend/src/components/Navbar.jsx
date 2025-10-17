import { useState } from 'react';
import { Flame } from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router';
import { useAuth } from '../contexts/AuthContext';
import { useUserStats } from '../hooks/useUserStats';
import DesktopNavigation from './shared/DesktopNavigation';
import NavbarStats from './shared/NavbarStats';
import { NavbarStatsSkeleton } from './skeletons/Skeleton';

/**
 * Main navigation bar component
 * - Responsive design with mobile menu
 * - Active route highlighting
 * - XP/Level and streak display for authenticated users
 * - User profile dropdown
 * - Theme-consistent styling
 */
export default function Navbar() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { authenticated, user, logout } = useAuth();
  const { data: stats, isLoading: statsLoading } = useUserStats();

  // Navigation items based on auth status
  const navigationItems = authenticated
    ? [
        { name: 'Dashboard', path: '/dashboard' },
        { name: 'Modules', path: '/modules' },
        { name: 'Achievements', path: '/achievements' },
        { name: 'Profile', path: '/profile' },
      ]
    : [];

  const isActivePath = (path) => location.pathname === path;

  const handleLogout = async () => {
    setProfileMenuOpen(false);
    await logout();
    navigate('/signin');
  };

  return (
    <nav className="bg-[var(--surface)] border-b border-[var(--border)]">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          {/* Left side - Mobile menu button, Logo and desktop navigation */}
          <div className="flex items-center gap-2">
            {/* Mobile menu button */}
            <button
              type="button"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="inline-flex items-center justify-center rounded-md p-2 text-[var(--text-muted)]
                       hover:bg-[var(--surface-muted)] hover:text-[var(--text)] transition-colors sm:hidden"
            >
              <span className="sr-only">Open main menu</span>
              {/* Hamburger icon */}
              {!mobileMenuOpen ? (
                <svg
                  className="h-6 w-6"
                  fill="none"
                  viewBox="0 0 24 24"
                  strokeWidth="1.5"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5"
                  />
                </svg>
              ) : (
                <svg
                  className="h-6 w-6"
                  fill="none"
                  viewBox="0 0 24 24"
                  strokeWidth="1.5"
                  stroke="currentColor"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              )}
            </button>

            {/* Logo */}
            <Link to={authenticated ? '/dashboard' : '/'} className="flex shrink-0 items-center">
              <div className="bg-[var(--accent)] rounded-md px-3 py-1.5 flex items-center justify-center w-14">
                <span className="text-lg font-bold text-[var(--bg)]">{'{GG}'}</span>
              </div>
            </Link>

            {/* Desktop navigation */}
            <DesktopNavigation items={navigationItems} isActivePath={isActivePath} />
          </div>

          {/* Right side - Stats, Auth buttons or user menu */}
          <div className="flex items-center gap-2">
            {authenticated ? (
              <>
                {/* XP/Level and Streak display - Hidden on mobile */}
                <div className="hidden md:flex mr-4">
                  {statsLoading ? (
                    <NavbarStatsSkeleton />
                  ) : (
                    <NavbarStats stats={stats} />
                  )}
                </div>

                {/* User menu */}
                <div className="relative ml-3">
                  <button
                    onClick={() => setProfileMenuOpen(!profileMenuOpen)}
                    className="flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium
                             text-[var(--text)] hover:bg-[var(--surface-muted)] transition-colors"
                  >
                    <span className="hidden sm:inline">
                      {user?.username}
                    </span>
                    <div className="h-8 w-8 rounded-full bg-[var(--accent)] flex items-center justify-center text-[var(--bg)] font-semibold">
                      {(user?.username?.[0] || 'U')}
                    </div>
                  </button>

                  {/* Profile dropdown */}
                  {profileMenuOpen && (
                    <>
                      {/* Backdrop for closing dropdown */}
                      <div
                        className="fixed inset-0 z-10"
                        onClick={() => setProfileMenuOpen(false)}
                      />
                      <div
                        className="absolute right-0 z-20 mt-2 w-56 origin-top-right rounded-md
                                 bg-[var(--surface)] border border-[var(--border)] shadow-lg"
                      >
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
                            to="/profile"
                            onClick={() => setProfileMenuOpen(false)}
                            className="block px-4 py-2 text-sm text-[var(--text-muted)]
                                     hover:bg-[var(--surface-muted)] hover:text-[var(--text)]"
                          >
                            Your Profile
                          </Link>
                          <Link
                            to="/settings"
                            onClick={() => setProfileMenuOpen(false)}
                            className="block px-4 py-2 text-sm text-[var(--text-muted)]
                                     hover:bg-[var(--surface-muted)] hover:text-[var(--text)]"
                          >
                            Settings
                          </Link>
                          <button
                            onClick={handleLogout}
                            className="block w-full text-left px-4 py-2 text-sm text-[var(--text-muted)]
                                     hover:bg-[var(--surface-muted)] hover:text-[var(--text)]"
                          >
                            Sign Out
                          </button>
                        </div>
                      </div>
                    </>
                  )}
                </div>
              </>
            ) : (
              <div className="hidden sm:flex sm:gap-2">
                <Link
                  to="/signin"
                  className="rounded-md px-4 py-2 text-sm font-medium text-[var(--text-muted)]
                           hover:text-[var(--text)] transition-colors"
                >
                  Sign In
                </Link>
                <Link
                  to="/signup"
                  className="rounded-md px-4 py-2 text-sm font-medium bg-transparent
                           text-[var(--accent)] border-2 border-[var(--accent)]
                           hover:bg-[var(--accent)] hover:text-[var(--bg)] transition-colors"
                >
                  Sign Up
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Mobile menu */}
      {mobileMenuOpen && (
        <div className="sm:hidden border-t border-[var(--border)]">
          <div className="space-y-1 px-2 pb-3 pt-2">
            {navigationItems.map((item) => (
              <Link
                key={item.path}
                to={item.path}
                onClick={() => setMobileMenuOpen(false)}
                className={`block rounded-md px-3 py-2 text-base font-medium transition-colors ${
                  isActivePath(item.path)
                    ? 'bg-[var(--surface-muted)] text-[var(--accent)]'
                    : 'text-[var(--text-muted)] hover:bg-[var(--surface-muted)] hover:text-[var(--text)]'
                }`}
              >
                {item.name}
              </Link>
            ))}

            {/* Mobile auth buttons */}
            {!authenticated && (
              <div className="pt-4 space-y-2">
                <Link
                  to="/signin"
                  onClick={() => setMobileMenuOpen(false)}
                  className="block rounded-md px-3 py-2 text-base font-medium text-[var(--text-muted)]
                           hover:bg-[var(--surface-muted)] hover:text-[var(--text)]"
                >
                  Sign In
                </Link>
                <Link
                  to="/signup"
                  onClick={() => setMobileMenuOpen(false)}
                  className="block rounded-md px-3 py-2 text-base font-medium text-center
                           bg-transparent text-[var(--accent)] border-2 border-[var(--accent)]
                           hover:bg-[var(--accent)] hover:text-[var(--bg)]"
                >
                  Sign Up
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </nav>
  );
}
