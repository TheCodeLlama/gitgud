import Navbar from './Navbar';

/**
 * Main layout wrapper component
 * Includes the navbar and provides consistent page structure
 */
export default function Layout({ children }) {
  return (
    <div className="min-h-screen bg-[var(--bg)]">
      <Navbar />
      <main>{children}</main>
    </div>
  );
}
