import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import Navbar from './Navbar';
import Sidebar from './Sidebar';
import Footer from './Footer';

/**
 * Main layout wrapper component
 * - Includes navbar at the top
 * - Sidebar for authenticated users (collapsible on mobile)
 * - Main content area
 * - Footer at the bottom
 * - Responsive design with mobile-first approach
 *
 * @param {ReactNode} children - Page content
 * @param {boolean} showSidebar - Whether to show sidebar (default: true for authenticated users)
 */
export default function Layout({ children, showSidebar = true }) {
  const { authenticated } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // Determine if sidebar should be rendered
  const shouldShowSidebar = authenticated && showSidebar;

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  return (
    <div className="min-h-screen bg-[var(--bg)] flex flex-col">
      {/* Navbar */}
      <Navbar onMenuClick={toggleSidebar} />

      {/* Main content area with sidebar */}
      <div className="flex flex-1">
        {/* Sidebar - only for authenticated users */}
        {shouldShowSidebar && (
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
        )}

        {/* Main content */}
        <main
          className={`
            flex-1
            ${shouldShowSidebar ? 'lg:ml-0' : ''}
            min-h-[calc(100vh-4rem)]
          `}
        >
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
            {children}
          </div>
        </main>
      </div>

      {/* Footer */}
      <Footer />
    </div>
  );
}
