/**
 * Profile Page
 * Displays user profile information, stats, and achievements
 */

import { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { api } from '../../lib/api';
import { Trophy, Star, Flame, TrendingUp, Edit, Lock } from 'lucide-react';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import ProgressBar from '../../components/ui/ProgressBar';
import AchievementCard from '../../components/ui/AchievementCard';
import { Skeleton } from '../../components/skeletons/Skeleton';
import { mapIconToComponent } from '../../utils/iconMapping';
import ProfileEditModal from '../../components/profile/ProfileEditModal';

export default function ProfilePage() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [userAchievements, setUserAchievements] = useState([]);
  const [allAchievements, setAllAchievements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [sortBy, setSortBy] = useState('recent'); // recent, rarity, name

  // Fetch profile data
  useEffect(() => {
    const fetchProfileData = async () => {
      try {
        setLoading(true);
        setError(null);

        // Fetch profile with stats
        const profileRes = await api.get('/v1/gamification/profile');
        setProfile(profileRes.data.data);

        // Fetch user's achievements
        const userAchievementsRes = await api.get('/v1/gamification/achievements/user');
        setUserAchievements(userAchievementsRes.data.data);

        // Fetch all achievements to show locked ones
        const allAchievementsRes = await api.get('/v1/gamification/achievements');
        setAllAchievements(allAchievementsRes.data.data);
      } catch (err) {
        console.error('Failed to fetch profile data:', err);
        setError('Failed to load profile data. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    if (user) {
      fetchProfileData();
    }
  }, [user]);

  // Create a merged achievements list (earned + locked)
  const getMergedAchievements = () => {
    if (!allAchievements.length) return [];

    // Create a map of earned achievements by achievement ID
    const earnedMap = new Map();
    userAchievements.forEach((ua) => {
      earnedMap.set(ua.achievement.id, ua);
    });

    // Merge all achievements with earned status
    const merged = allAchievements.map((achievement) => {
      const userAchievement = earnedMap.get(achievement.id);
      return {
        ...achievement,
        locked: !userAchievement,
        earnedAt: userAchievement?.earnedAt || null,
      };
    });

    // Sort based on selected sort option
    if (sortBy === 'recent') {
      return merged.sort((a, b) => {
        // Unlocked first, then by earned date
        if (a.locked && !b.locked) return 1;
        if (!a.locked && b.locked) return -1;
        if (!a.locked && !b.locked) {
          return new Date(b.earnedAt) - new Date(a.earnedAt);
        }
        return 0;
      });
    } else if (sortBy === 'rarity') {
      const rarityOrder = { LEGENDARY: 0, EPIC: 1, RARE: 2, UNCOMMON: 3, COMMON: 4 };
      return merged.sort((a, b) => {
        const rarityDiff = rarityOrder[a.rarity] - rarityOrder[b.rarity];
        if (rarityDiff !== 0) return rarityDiff;
        // If same rarity, unlocked first
        if (a.locked && !b.locked) return 1;
        if (!a.locked && b.locked) return -1;
        return 0;
      });
    } else if (sortBy === 'name') {
      return merged.sort((a, b) => a.name.localeCompare(b.name));
    }

    return merged;
  };

  const mergedAchievements = getMergedAchievements();
  const unlockedCount = userAchievements.length;
  const totalCount = allAchievements.length;

  // Calculate XP progress to next level
  const getXpProgress = () => {
    if (!profile) return 0;
    const currentLevelXp = profile.totalXp;
    const nextLevelXp = currentLevelXp + profile.xpToNextLevel;
    const progress = (currentLevelXp / nextLevelXp) * 100;
    return Math.min(progress, 100);
  };

  const handleProfileUpdate = (updatedProfile) => {
    setProfile({ ...profile, ...updatedProfile });
    setIsEditModalOpen(false);
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8 max-w-6xl">
        <Skeleton className="h-8 w-48 mb-6" />
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <Skeleton className="h-64 col-span-1" />
          <Skeleton className="h-64 col-span-2" />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-8 max-w-6xl">
        <Card className="p-6 text-center">
          <p className="text-[var(--danger)] mb-4">{error}</p>
          <Button onClick={() => window.location.reload()}>Retry</Button>
        </Card>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="container mx-auto px-4 py-8 max-w-6xl">
        <Card className="p-6 text-center">
          <p className="text-[var(--text-muted)]">No profile data available.</p>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-6xl">
      {/* Page Header */}
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-3xl font-bold text-[var(--text)]">Profile</h1>
        <Button onClick={() => setIsEditModalOpen(true)} icon={<Edit className="w-4 h-4" />}>
          Edit Profile
        </Button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left column - Profile Card */}
        <div className="lg:col-span-1">
          <Card className="p-6">
            {/* Avatar */}
            <div className="flex flex-col items-center text-center mb-6">
              <div className="w-24 h-24 rounded-full bg-gradient-to-br from-[var(--primary)] to-[var(--accent)] flex items-center justify-center mb-4">
                {profile.avatarUrl ? (
                  <img
                    src={profile.avatarUrl}
                    alt={profile.displayName}
                    className="w-24 h-24 rounded-full object-cover"
                  />
                ) : (
                  <span className="text-3xl font-bold text-white">
                    {profile.displayName?.charAt(0) || profile.username?.charAt(0) || 'U'}
                  </span>
                )}
              </div>

              {/* Display Name */}
              <h2 className="text-xl font-bold text-[var(--text)] mb-1">
                {profile.displayName || profile.username}
              </h2>
              <p className="text-sm text-[var(--text-muted)] mb-4">@{profile.username}</p>

              {/* Bio */}
              {profile.bio && (
                <p className="text-sm text-[var(--text-muted)] italic border-t border-[var(--border)] pt-4 w-full">
                  "{profile.bio}"
                </p>
              )}
            </div>

            {/* Level and XP */}
            <div className="mb-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-[var(--text)]">
                  Level {profile.currentLevel}
                </span>
                <span className="text-xs text-[var(--text-muted)]">
                  {profile.xpToNextLevel} XP to Level {profile.currentLevel + 1}
                </span>
              </div>
              <ProgressBar value={getXpProgress()} variant="primary" size="md" />
              <p className="text-xs text-[var(--text-muted)] mt-1 text-center">
                {profile.totalXp.toLocaleString()} Total XP
              </p>
            </div>
          </Card>
        </div>

        {/* Right column - Stats and Achievements */}
        <div className="lg:col-span-2 space-y-6">
          {/* Stats Cards */}
          <div className="grid grid-cols-2 gap-4">
            <Card className="p-4">
              <div className="flex items-center gap-3">
                <div className="p-3 bg-[var(--primary)]/10 rounded-lg">
                  <Trophy className="w-6 h-6 text-[var(--primary)]" />
                </div>
                <div>
                  <p className="text-2xl font-bold text-[var(--text)]">{unlockedCount}</p>
                  <p className="text-sm text-[var(--text-muted)]">Achievements</p>
                </div>
              </div>
            </Card>

            <Card className="p-4">
              <div className="flex items-center gap-3">
                <div className="p-3 bg-[var(--warning)]/10 rounded-lg">
                  <Star className="w-6 h-6 text-[var(--warning)]" />
                </div>
                <div>
                  <p className="text-2xl font-bold text-[var(--text)]">Level {profile.currentLevel}</p>
                  <p className="text-sm text-[var(--text-muted)]">Current Level</p>
                </div>
              </div>
            </Card>

            <Card className="p-4">
              <div className="flex items-center gap-3">
                <div className="p-3 bg-[var(--danger)]/10 rounded-lg">
                  <Flame className="w-6 h-6 text-[var(--danger)]" />
                </div>
                <div>
                  <p className="text-2xl font-bold text-[var(--text)]">{profile.currentStreakDays}</p>
                  <p className="text-sm text-[var(--text-muted)]">Day Streak</p>
                </div>
              </div>
            </Card>

            <Card className="p-4">
              <div className="flex items-center gap-3">
                <div className="p-3 bg-[var(--success)]/10 rounded-lg">
                  <TrendingUp className="w-6 h-6 text-[var(--success)]" />
                </div>
                <div>
                  <p className="text-2xl font-bold text-[var(--text)]">{profile.longestStreakDays}</p>
                  <p className="text-sm text-[var(--text-muted)]">Longest Streak</p>
                </div>
              </div>
            </Card>
          </div>

          {/* Achievements Section */}
          <Card className="p-6">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="text-xl font-bold text-[var(--text)] mb-1">Achievements</h3>
                <p className="text-sm text-[var(--text-muted)]">
                  {unlockedCount} of {totalCount} unlocked ({((unlockedCount / totalCount) * 100).toFixed(0)}%)
                </p>
              </div>

              {/* Sort dropdown */}
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="px-3 py-2 bg-[var(--bg)] border border-[var(--border)] rounded-lg text-sm text-[var(--text)] focus:outline-none focus:ring-2 focus:ring-[var(--primary)]"
              >
                <option value="recent">Recently Earned</option>
                <option value="rarity">By Rarity</option>
                <option value="name">By Name</option>
              </select>
            </div>

            {/* Achievements Grid */}
            {mergedAchievements.length > 0 ? (
              <div className="space-y-3 max-h-[600px] overflow-y-auto">
                {mergedAchievements.map((achievement) => (
                  <AchievementCard
                    key={achievement.id}
                    name={achievement.name}
                    description={achievement.description}
                    icon={mapIconToComponent(achievement.iconUrl)}
                    rarity={achievement.rarity}
                    xpReward={achievement.xpReward}
                    earnedAt={achievement.earnedAt}
                    locked={achievement.locked}
                  />
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <Lock className="w-12 h-12 text-[var(--text-muted)] mx-auto mb-3" />
                <p className="text-[var(--text-muted)]">No achievements yet. Start learning to unlock them!</p>
              </div>
            )}
          </Card>
        </div>
      </div>

      {/* Profile Edit Modal */}
      {isEditModalOpen && (
        <ProfileEditModal
          profile={profile}
          onClose={() => setIsEditModalOpen(false)}
          onSave={handleProfileUpdate}
        />
      )}
    </div>
  );
}
