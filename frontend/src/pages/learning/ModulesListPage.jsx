/**
 * ModulesListPage Component
 * Displays all available learning modules with search and filter functionality
 */

import { useState, useMemo } from 'react';
import { useModules } from '../../hooks/useModules';
import { useUserProgress } from '../../hooks/useUserProgress';
import { useUserStats } from '../../hooks/useUserStats';
import ModuleCard from '../../components/learning/ModuleCard';
import { Search, Filter } from 'lucide-react';
import { Skeleton } from '../../components/skeletons/Skeleton.jsx';

export default function ModulesListPage() {
  const { data: modules, isLoading: modulesLoading, error: modulesError } = useModules();
  const { data: userProgress } = useUserProgress();
  const { data: userStats } = useUserStats();

  const [searchQuery, setSearchQuery] = useState('');
  const [difficultyFilter, setDifficultyFilter] = useState('ALL');

  // Filter and search modules
  const filteredModules = useMemo(() => {
    if (!modules) return [];

    return modules.filter((module) => {
      // Search filter
      const matchesSearch =
        searchQuery === '' ||
        module.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        module.description.toLowerCase().includes(searchQuery.toLowerCase());

      // Difficulty filter
      const matchesDifficulty =
        difficultyFilter === 'ALL' || module.difficulty === difficultyFilter;

      return matchesSearch && matchesDifficulty;
    });
  }, [modules, searchQuery, difficultyFilter]);

  // Check if module is locked
  const isModuleLocked = (module) => {
    if (!module.requiredLevel || module.requiredLevel <= 1) return false;
    if (!userStats) return false;
    return userStats.level < module.requiredLevel;
  };

  if (modulesError) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="bg-red-500/10 border border-red-500/20 rounded-lg p-6 text-center">
          <p className="text-red-600">Failed to load modules. Please try again later.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Page Header */}
      <div className="mb-8">
        <h1 className="text-4xl font-bold text-[var(--text)] mb-2">Learning Modules</h1>
        <p className="text-[var(--text-muted)]">
          Explore our collection of interactive Java and Spring Boot courses
        </p>
      </div>

      {/* Search and Filter Bar */}
      <div className="mb-8 flex flex-col md:flex-row gap-4">
        {/* Search Input */}
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-[var(--text-muted)]" />
          <input
            type="text"
            placeholder="Search modules..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-3 bg-[var(--surface)] border border-[var(--border)] rounded-lg text-[var(--text)] placeholder-[var(--text-muted)] focus:outline-none focus:border-[var(--accent)] transition-colors"
          />
        </div>

        {/* Difficulty Filter */}
        <div className="flex items-center gap-2">
          <Filter className="w-5 h-5 text-[var(--text-muted)]" />
          <select
            value={difficultyFilter}
            onChange={(e) => setDifficultyFilter(e.target.value)}
            className="px-4 py-3 bg-[var(--surface)] border border-[var(--border)] rounded-lg text-[var(--text)] focus:outline-none focus:border-[var(--accent)] transition-colors cursor-pointer"
          >
            <option value="ALL">All Difficulties</option>
            <option value="BEGINNER">Beginner</option>
            <option value="INTERMEDIATE">Intermediate</option>
            <option value="ADVANCED">Advanced</option>
            <option value="EXPERT">Expert</option>
          </select>
        </div>
      </div>

      {/* Loading State */}
      {modulesLoading && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6">
              <Skeleton className="h-6 w-3/4 mb-4" />
              <Skeleton className="h-4 w-full mb-2" />
              <Skeleton className="h-4 w-5/6 mb-4" />
              <Skeleton className="h-8 w-full" />
            </div>
          ))}
        </div>
      )}

      {/* No Results */}
      {!modulesLoading && filteredModules.length === 0 && (
        <div className="text-center py-12">
          <p className="text-[var(--text-muted)] text-lg">
            {searchQuery || difficultyFilter !== 'ALL'
              ? 'No modules match your filters. Try adjusting your search.'
              : 'No modules available yet. Check back soon!'}
          </p>
        </div>
      )}

      {/* Modules Grid */}
      {!modulesLoading && filteredModules.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredModules.map((module) => (
            <ModuleCard
              key={module.id}
              module={module}
              userProgress={userProgress}
              locked={isModuleLocked(module)}
            />
          ))}
        </div>
      )}

      {/* Stats Summary */}
      {!modulesLoading && modules && modules.length > 0 && (
        <div className="mt-12 pt-8 border-t border-[var(--border)]">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
            <div>
              <p className="text-3xl font-bold text-[var(--accent)]">{modules.length}</p>
              <p className="text-[var(--text-muted)] text-sm">Total Modules</p>
            </div>
            <div>
              <p className="text-3xl font-bold text-[var(--accent)]">
                {modules.reduce((acc, m) => acc + (m.lessons?.length || 0), 0)}
              </p>
              <p className="text-[var(--text-muted)] text-sm">Total Lessons</p>
            </div>
            <div>
              <p className="text-3xl font-bold text-[var(--accent)]">
                {modules.filter((m) => !isModuleLocked(m)).length}
              </p>
              <p className="text-[var(--text-muted)] text-sm">Unlocked</p>
            </div>
            <div>
              <p className="text-3xl font-bold text-[var(--accent)]">
                {userProgress?.completedLessons || 0}
              </p>
              <p className="text-[var(--text-muted)] text-sm">Completed</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
