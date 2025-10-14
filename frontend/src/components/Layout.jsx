import Navbar from './Navbar';
import Footer from './Footer';

/**
 * Main layout wrapper component
 * - Includes navbar at the top
 * - Main content area
 * - Footer at the bottom
 * - Responsive design with mobile-first approach
 *
 * @param {ReactNode} children - Page content
 */
export default function Layout({ children }) {
  return (
    <div className="min-h-screen bg-[var(--bg)] flex flex-col">
      {/* Navbar */}
      <Navbar />

      {/* Main content */}
      <main className="flex-1 min-h-[calc(100vh-4rem)]">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
          {children}
        </div>
      </main>

      {/* Footer */}
      <Footer />
    </div>
  );
}
