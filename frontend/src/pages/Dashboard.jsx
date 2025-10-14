import { useAuth } from '../contexts/AuthContext';
import { Link } from 'react-router';

/**
 * Dashboard page
 * Main landing page for authenticated users
 */
export default function Dashboard() {
  const { user } = useAuth();

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-[var(--text)]">Dashboard</h1>
        <p className="text-[var(--text-muted)] mt-2">
          Welcome back! Here's your learning progress at a glance.
        </p>
      </div>

      {/* Welcome message */}
      <div className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6">
        <h2 className="text-xl font-semibold mb-2 text-[var(--text)]">
          Welcome back, {user?.firstName || user?.username || 'Learner'}!
        </h2>
        <p className="text-[var(--text-muted)]">
          Ready to continue your Java Spring learning journey?
        </p>
      </div>

      {/* Quick actions */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Link
          to="/modules"
          className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6
                   hover:border-[var(--accent)] hover:shadow-lg transition-all group"
        >
          <div className="text-3xl mb-3">📚</div>
          <h3 className="font-semibold mb-2 text-[var(--text)] group-hover:text-[var(--accent)] transition-colors">
            Browse Modules
          </h3>
          <p className="text-sm text-[var(--text-muted)]">
            Explore Java and Spring Boot learning paths
          </p>
        </Link>

        <Link
          to="/profile"
          className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6
                   hover:border-[var(--accent)] hover:shadow-lg transition-all group"
        >
          <div className="text-3xl mb-3">👤</div>
          <h3 className="font-semibold mb-2 text-[var(--text)] group-hover:text-[var(--accent)] transition-colors">
            Your Profile
          </h3>
          <p className="text-sm text-[var(--text-muted)]">
            View stats, achievements, and progress
          </p>
        </Link>

        <Link
          to="/achievements"
          className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6
                   hover:border-[var(--accent)] hover:shadow-lg transition-all group"
        >
          <div className="text-3xl mb-3">🏆</div>
          <h3 className="font-semibold mb-2 text-[var(--text)] group-hover:text-[var(--accent)] transition-colors">
            Achievements
          </h3>
          <p className="text-sm text-[var(--text-muted)]">
            Track your unlocked achievements
          </p>
        </Link>
      </div>

      {/* Coming soon placeholder */}
      <div className="bg-gradient-to-br from-[var(--accent)]/10 to-[var(--accent-secondary)]/10
                    border border-[var(--accent)]/20 rounded-lg p-8 text-center">
        <p className="text-[var(--text-muted)] mb-2">
          🚀 More dashboard features coming soon!
        </p>
        <p className="text-sm text-[var(--text-muted)]">
          Recent activity, progress charts, and personalized recommendations
        </p>
      </div>
    </div>
  );
}
