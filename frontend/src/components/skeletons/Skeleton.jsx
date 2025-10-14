/**
 * Base skeleton component for loading states
 * Provides animated placeholder UI while content loads
 */

/**
 * Base Skeleton component
 * @param {string} className - Additional Tailwind classes
 */
export function Skeleton({ className = '' }) {
  return (
    <div
      className={`animate-pulse bg-[var(--surface-muted)] rounded ${className}`}
      aria-hidden="true"
    />
  );
}

/**
 * Card Skeleton - For card-like layouts
 */
export function CardSkeleton() {
  return (
    <div className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6">
      <Skeleton className="h-6 w-3/4 mb-4" />
      <Skeleton className="h-4 w-full mb-2" />
      <Skeleton className="h-4 w-5/6 mb-2" />
      <Skeleton className="h-4 w-4/6" />
    </div>
  );
}

/**
 * List Item Skeleton - For list layouts
 */
export function ListItemSkeleton() {
  return (
    <div className="flex items-center gap-4 p-4 bg-[var(--surface)] border border-[var(--border)] rounded-lg">
      <Skeleton className="h-12 w-12 rounded-full" />
      <div className="flex-1">
        <Skeleton className="h-5 w-3/4 mb-2" />
        <Skeleton className="h-4 w-1/2" />
      </div>
    </div>
  );
}

/**
 * Stats Skeleton - For stats widgets
 */
export function StatsSkeleton() {
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <Skeleton className="h-5 w-24" />
        <Skeleton className="h-5 w-16" />
      </div>
      <Skeleton className="h-3 w-full rounded-full" />
      <div className="flex items-center justify-between">
        <Skeleton className="h-4 w-20" />
        <Skeleton className="h-4 w-20" />
      </div>
    </div>
  );
}

/**
 * Sidebar Skeleton - For sidebar loading
 */
export function SidebarSkeleton() {
  return (
    <div className="space-y-6 p-4">
      <div className="space-y-2">
        {[1, 2, 3, 4].map((i) => (
          <Skeleton key={i} className="h-10 w-full" />
        ))}
      </div>
      <div className="p-4 bg-[var(--surface)] border border-[var(--border)] rounded-lg">
        <StatsSkeleton />
      </div>
    </div>
  );
}

/**
 * Navbar Stats Skeleton - For navbar gamification display
 */
export function NavbarStatsSkeleton() {
  return (
    <div className="flex items-center gap-4">
      <div className="flex items-center gap-2">
        <Skeleton className="h-6 w-6 rounded-full" />
        <Skeleton className="h-4 w-12" />
      </div>
      <div className="flex items-center gap-2">
        <Skeleton className="h-6 w-6 rounded" />
        <Skeleton className="h-4 w-16" />
      </div>
    </div>
  );
}
