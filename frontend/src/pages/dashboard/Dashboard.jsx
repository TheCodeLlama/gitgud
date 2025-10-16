import { useAuth } from '../../contexts/AuthContext.jsx';
import UserStatsCard from '../../components/dashboard/UserStatsCard.jsx';
import ContinueLearning from '../../components/dashboard/ContinueLearning.jsx';
import RecentAchievements from '../../components/dashboard/RecentAchievements.jsx';
import QuickStats from '../../components/dashboard/QuickStats.jsx';

/**
 * Dashboard page
 * Main landing page for authenticated users
 * Displays user stats, progress, recent achievements, and continue learning section
 */
export default function Dashboard() {
  const { user } = useAuth();

  return (
    <div className="space-y-8">
      {/* Header with welcome message */}
      <div>
        <h1 className="text-3xl font-bold text-[var(--text)]">
          Welcome back, {user?.displayName || user?.username || 'Learner'}! 👋
        </h1>
        <p className="text-[var(--text-muted)] mt-2">
          Here's your learning progress at a glance. Keep up the great work!
        </p>
      </div>

      {/* Quick Stats Overview */}
      <QuickStats />

      {/* Main content grid - Stats Card and Continue Learning */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* User Stats Card - Takes 1 column */}
        <div className="lg:col-span-1">
          <UserStatsCard />
        </div>

        {/* Continue Learning Section - Takes 2 columns */}
        <div className="lg:col-span-2">
          <ContinueLearning />
        </div>
      </div>

      {/* Recent Achievements */}
      <RecentAchievements limit={5} />
    </div>
  );
}
