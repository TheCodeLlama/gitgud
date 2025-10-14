import { useAuth } from '../contexts/AuthContext';
import { Link } from 'react-router';

/**
 * Dashboard page (placeholder)
 */
export default function Dashboard() {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-[var(--bg)] text-[var(--text)] p-6">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Header with logout */}
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold">Dashboard</h1>
          <button
            onClick={logout}
            className="px-6 h-10 rounded-md bg-[var(--surface)] text-[var(--text)]
                     hover:bg-[var(--surface-muted)] border border-[var(--border)]
                     font-medium transition-colors"
          >
            Sign Out
          </button>
        </div>

        {/* Welcome message */}
        <div className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-8">
          <h2 className="text-2xl font-semibold mb-4">
            Welcome back, {user?.firstName || user?.username || 'Learner'}!
          </h2>
          <p className="text-[var(--text-muted)] mb-6">
            Ready to continue your Java Spring learning journey?
          </p>

          {/* User info */}
          {user && (
            <div className="space-y-2 text-sm">
              <p className="text-[var(--text-muted)]">
                <span className="font-medium text-[var(--text)]">Email:</span> {user.email}
              </p>
              <p className="text-[var(--text-muted)]">
                <span className="font-medium text-[var(--text)]">Username:</span> {user.username}
              </p>
            </div>
          )}
        </div>

        {/* Quick actions */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Link
            to="/modules"
            className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6
                     hover:border-[var(--accent)] transition-colors"
          >
            <div className="text-3xl mb-3">📚</div>
            <h3 className="font-semibold mb-2">Browse Modules</h3>
            <p className="text-sm text-[var(--text-muted)]">
              Explore Java and Spring Boot learning paths
            </p>
          </Link>

          <Link
            to="/profile"
            className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6
                     hover:border-[var(--accent)] transition-colors"
          >
            <div className="text-3xl mb-3">👤</div>
            <h3 className="font-semibold mb-2">Your Profile</h3>
            <p className="text-sm text-[var(--text-muted)]">
              View stats, achievements, and progress
            </p>
          </Link>

          <Link
            to="/theme-guide"
            className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6
                     hover:border-[var(--accent)] transition-colors"
          >
            <div className="text-3xl mb-3">🎨</div>
            <h3 className="font-semibold mb-2">Theme Guide</h3>
            <p className="text-sm text-[var(--text-muted)]">
              View design system and components
            </p>
          </Link>
        </div>

        {/* Coming soon placeholder */}
        <div className="bg-[var(--surface-muted)] border border-[var(--border)] rounded-lg p-8 text-center">
          <p className="text-[var(--text-muted)]">
            More features coming soon! XP system, lessons, and achievements are in development.
          </p>
        </div>
      </div>
    </div>
  );
}
