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
      className={`rounded-md px-3 py-2 text-sm font-medium transition-colors ${
        isActive
          ? 'bg-[var(--surface-muted)] text-[var(--accent)]'
          : 'text-[var(--text-muted)] hover:bg-[var(--surface-muted)] hover:text-[var(--text)]'
      }`}
    >
      {name}
    </Link>
  );
}
