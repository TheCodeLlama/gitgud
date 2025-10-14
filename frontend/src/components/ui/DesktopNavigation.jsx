/**
 * Desktop Navigation Component
 * Container for desktop navigation items
 */

import DesktopNavigationItem from './DesktopNavigationItem';

/**
 * DesktopNavigation component
 * @param {Array} items - Array of navigation items with {name, path}
 * @param {function} isActivePath - Function to determine if a path is active
 */
export default function DesktopNavigation({ items, isActivePath }) {
  return (
    <div className="hidden sm:ml-6 sm:block">
      <div className="flex space-x-4">
        {items.map((item) => (
          <DesktopNavigationItem
            key={item.path}
            name={item.name}
            path={item.path}
            isActive={isActivePath(item.path)}
          />
        ))}
      </div>
    </div>
  );
}
