import { useAuth } from '../contexts/AuthContext';
import UserStatsCard from '../components/dashboard/UserStatsCard';
import ContinueLearning from '../components/dashboard/ContinueLearning';
import RecentAchievements from '../components/dashboard/RecentAchievements';
import QuickStats from '../components/dashboard/QuickStats';

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
          Welcome back, {user?.firstName || user?.username || 'Learner'}! 👋
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

      {/* Motivational tip */}
      <div className="bg-gradient-to-r from-[var(--accent)]/10 via-[var(--accent-secondary)]/10 to-[var(--accent)]/10
                    border border-[var(--accent)]/20 rounded-lg p-6 text-center">
        <div className="text-2xl mb-3">💡</div>
        <h3 className="text-lg font-semibold text-[var(--text)] mb-2">
          Tip of the Day
        </h3>
        <p className="text-[var(--text-muted)] max-w-2xl mx-auto">
          Consistency is key! Try to complete at least one lesson per day to maintain your streak
          and earn bonus XP. Small, regular practice leads to big improvements over time.
        </p>
      </div>
    </div>
  );
}
