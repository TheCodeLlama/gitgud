/**
 * Recent Achievements Component
 * Displays 3-5 most recently earned achievements using AchievementCard components
 */

import { Trophy } from 'lucide-react';
import { Link } from 'react-router';
import { useUserAchievements } from '../../hooks/useUserAchievements';
import Card, { CardBody } from '../shared/Card';
import AchievementCard from '../shared/AchievementCard';
import Button from '../shared/Button';
import { ListItemSkeleton } from '../skeletons/Skeleton';
import { mapIconToComponent } from '../../utils/iconMapping';

export default function RecentAchievements({ limit = 5 }) {
  const { data: achievements, isLoading } = useUserAchievements();

  if (isLoading) {
    return (
      <Card>
        <CardBody>
          <h3 className="text-lg font-semibold text-[var(--text)] mb-4">Recent Achievements</h3>
          <div className="space-y-3">
            {[1, 2, 3].map((i) => (
              <ListItemSkeleton key={i} />
            ))}
          </div>
        </CardBody>
      </Card>
    );
  }

  // Sort by earned date (most recent first) and take the first few
  const recentAchievements = achievements
    ?.slice()
    .sort((a, b) => new Date(b.earnedAt) - new Date(a.earnedAt))
    .slice(0, limit) || [];

  if (recentAchievements.length === 0) {
    return (
      <Card>
        <CardBody>
          <h3 className="text-lg font-semibold text-[var(--text)] mb-4">Recent Achievements</h3>
          <div className="text-center py-8">
            <Trophy className="w-16 h-16 mx-auto mb-3 text-[var(--accent)]" />
            <p className="text-[var(--text-muted)] mb-4">No achievements yet!</p>
            <p className="text-sm text-[var(--text-muted)]">
              Complete lessons to earn your first achievement
            </p>
          </div>
        </CardBody>
      </Card>
    );
  }

  return (
    <Card>
      <CardBody>
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold text-[var(--text)]">Recent Achievements</h3>
          <Link
            to="/achievements"
            className="text-sm text-[var(--accent)] hover:underline font-medium"
          >
            View All →
          </Link>
        </div>

        <div className="space-y-3">
          {recentAchievements.map((achievement, index) => (
            <AchievementCard
              key={achievement.id}
              name={achievement.name}
              description={achievement.description}
              icon={mapIconToComponent(achievement.icon)}
              rarity={achievement.rarity}
              xpReward={achievement.xpReward}
              earnedAt={achievement.earnedAt}
              style={{
                animation: `fadeInUp 0.3s ease-out ${index * 0.1}s both`,
              }}
            />
          ))}
        </div>

        {/* View all button for mobile */}
        <Button
          variant="outline"
          fullWidth
          to="/achievements"
          className="mt-4 sm:hidden"
        >
          View All Achievements
        </Button>
      </CardBody>
    </Card>
  );
}
