import { useState } from 'react';
import { Link, useLocation } from 'react-router';
import { useAuth } from '../contexts/AuthContext';

/**
 * Main navigation bar component
 * - Responsive design with mobile menu
 * - Active route highlighting
 * - User profile dropdown
 * - Theme-consistent styling
 */
export default function Navbar() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);
  const location = useLocation();
  const { authenticated, user, logout } = useAuth();

  // Navigation items based on auth status
  const navigationItems = authenticated
    ? [
        { name: 'Dashboard', path: '/dashboard' },
        { name: 'Modules', path: '/modules' },
        { name: 'Profile', path: '/profile' },
      ]
    : [
        { name: 'Home', path: '/' },
        { name: 'Theme Guide', path: '/theme-guide' },
      ];

  const isActivePath = (path) => location.pathname === path;

  const handleLogout = () => {
    setProfileMenuOpen(false);
    logout();
  };

  return (
    <nav className="bg-[var(--surface)] border-b border-[var(--border)]">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="relative flex h-16 items-center justify-between">
          {/* Mobile menu button */}
          <div className="absolute inset-y-0 left-0 flex items-center sm:hidden">
            <button
              type="button"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="relative inline-flex items-center justify-center rounded-md p-2 text-[var(--text-muted)]
                       hover:bg-[var(--surface-muted)] hover:text-[var(--text)] transition-colors"
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
          </div>

          {/* Logo and desktop navigation */}
          <div className="flex flex-1 items-center justify-center sm:items-stretch sm:justify-start">
            {/* Logo */}
            <Link to="/" className="flex shrink-0 items-center">
              <div className="bg-[var(--accent)] rounded-md px-3 py-1.5 flex items-center justify-center">
                <span className="text-lg font-bold text-[var(--bg)]">{'{GG}'}</span>
              </div>
            </Link>

            {/* Desktop navigation */}
            <div className="hidden sm:ml-6 sm:block">
              <div className="flex space-x-4">
                {navigationItems.map((item) => (
                  <Link
                    key={item.path}
                    to={item.path}
                    className={`rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                      isActivePath(item.path)
                        ? 'bg-[var(--surface-muted)] text-[var(--accent)]'
                        : 'text-[var(--text-muted)] hover:bg-[var(--surface-muted)] hover:text-[var(--text)]'
                    }`}
                  >
                    {item.name}
                  </Link>
                ))}
              </div>
            </div>
          </div>

          {/* Right side - Auth buttons or user menu */}
          <div className="absolute inset-y-0 right-0 flex items-center pr-2 sm:static sm:inset-auto sm:ml-6 sm:pr-0">
            {authenticated ? (
              <div className="relative ml-3">
                <button
                  onClick={() => setProfileMenuOpen(!profileMenuOpen)}
                  className="flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium
                           text-[var(--text)] hover:bg-[var(--surface-muted)] transition-colors"
                >
                  <span className="hidden sm:inline">
                    {user?.firstName || user?.username || 'User'}
                  </span>
                  <div className="h-8 w-8 rounded-full bg-[var(--accent)] flex items-center justify-center text-[var(--bg)] font-semibold">
                    {(user?.firstName?.[0] || user?.username?.[0] || 'U').toUpperCase()}
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
                      className="absolute right-0 z-20 mt-2 w-48 origin-top-right rounded-md
                               bg-[var(--surface)] border border-[var(--border)] shadow-lg"
                    >
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
