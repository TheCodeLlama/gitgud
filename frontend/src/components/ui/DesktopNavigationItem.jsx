/**
 * Desktop Navigation Item Component
 * A single navigation link for desktop navigation
 */

import { Link } from 'react-router';

/**
 * DesktopNavigationItem component
 * @param {string} name - Display name of the navigation item
 * @param {string} path - Route path
 * @param {boolean} isActive - Whether this item is active
 */
export default function DesktopNavigationItem({ name, path, isActive }) {
  return (
    <Link
      to={path}
      className={`relative px-4 py-2 text-sm font-medium transition-all duration-200 ${
        isActive
          ? 'text-[var(--accent)]'
          : 'text-[var(--text-muted)] hover:text-[var(--text)]'
      }`}
    >
      {name}
      {/* Active indicator - bottom border */}
      {isActive && (
        <span className="absolute bottom-0 left-0 right-0 h-0.5 bg-[var(--accent)] rounded-t-full" />
      )}
    </Link>
  );
}
