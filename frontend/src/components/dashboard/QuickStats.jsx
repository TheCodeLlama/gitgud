/**
 * Quick Stats Component
 * Displays quick overview stats using modular StatCard components
 */

import { Star, CheckCircle2, Target, Flame } from 'lucide-react';
import { useUserStats } from '../../hooks/useUserStats';
import { useUserProgress } from '../../hooks/useUserProgress';
import StatCard from '../ui/StatCard';
import { CardSkeleton } from '../skeletons/Skeleton';

export default function QuickStats() {
  const { data: stats, isLoading: statsLoading } = useUserStats();
  const { data: progress, isLoading: progressLoading } = useUserProgress();

  const isLoading = statsLoading || progressLoading;

  if (isLoading) {
    return (
      <div>
        <h3 className="text-lg font-semibold text-[var(--text)] mb-4">Quick Stats</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[1, 2, 3, 4].map((i) => (
            <CardSkeleton key={i} />
          ))}
        </div>
      </div>
    );
  }

  const statsData = [
    {
      id: 'total-xp',
      label: 'Total XP',
      value: stats?.totalXp?.toLocaleString() || '0',
      icon: <Star className="w-8 h-8 text-yellow-500" />,
      gradient: 'from-yellow-500 to-amber-500',
      bgColor: 'bg-yellow-500/10',
      borderColor: 'border-yellow-500/20',
    },
    {
      id: 'lessons-completed',
      label: 'Lessons Completed',
      value: progress?.completedLessons || 0,
      icon: <CheckCircle2 className="w-8 h-8 text-green-500" />,
      gradient: 'from-green-500 to-emerald-500',
      bgColor: 'bg-green-500/10',
      borderColor: 'border-green-500/20',
    },
    {
      id: 'current-level',
      label: 'Current Level',
      value: stats?.currentLevel || 1,
      icon: <Target className="w-8 h-8 text-blue-500" />,
      gradient: 'from-blue-500 to-cyan-500',
      bgColor: 'bg-blue-500/10',
      borderColor: 'border-blue-500/20',
    },
    {
      id: 'longest-streak',
      label: 'Longest Streak',
      value: `${stats?.longestStreakDays || 0} days`,
      icon: <Flame className="w-8 h-8 text-orange-500" />,
      gradient: 'from-orange-500 to-red-500',
      bgColor: 'bg-orange-500/10',
      borderColor: 'border-orange-500/20',
    },
  ];

  return (
    <div>
      <h3 className="text-lg font-semibold text-[var(--text)] mb-4">Quick Stats</h3>
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {statsData.map((stat, index) => (
          <StatCard
            key={stat.id}
            label={stat.label}
            value={stat.value}
            icon={stat.icon}
            gradient={stat.gradient}
            bgColor={stat.bgColor}
            borderColor={stat.borderColor}
            style={{
              animation: `fadeInUp 0.3s ease-out ${index * 0.1}s both`,
            }}
          />
        ))}
      </div>
    </div>
  );
}
