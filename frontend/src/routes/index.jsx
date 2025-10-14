/**
 * React Router routes configuration
 * Defines all application routes and navigation structure
 */

import { Routes, Route } from 'react-router';

/**
 * Home page - Theme showcase demonstrating the design system
 */
function Home() {
  return (
    <div className="bg-[var(--bg)] text-[var(--text)] p-8">
      <div className="max-w-7xl mx-auto space-y-12">
        {/* Header */}
        <div className="text-center space-y-4">
          <h1 className="text-5xl font-bold bg-gradient-to-r from-[var(--text)] to-[var(--text-muted)] bg-clip-text text-transparent">
            GitGud
          </h1>
          <p className="text-xl text-[var(--text-muted)]">
            Gamified Java Spring Learning Platform
          </p>
          <div className="flex items-center justify-center gap-3 text-sm">
            <span className="px-3 py-1 bg-[var(--surface)] border border-[var(--border)] rounded-full">
              Dark Mode
            </span>
            <span className="px-3 py-1 bg-[var(--surface)] border border-[var(--border)] rounded-full">
              Monochrome + Orange Accent
            </span>
            <span className="px-3 py-1 bg-[var(--accent)] text-[var(--bg)] rounded-full font-medium">
              MVP Phase 1A
            </span>
          </div>
        </div>

        {/* Color Palette */}
        <section className="space-y-4">
          <h2 className="text-2xl font-semibold">Color Palette</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <ColorSwatch name="Background" color="var(--bg)" />
            <ColorSwatch name="Surface" color="var(--surface)" />
            <ColorSwatch name="Surface Muted" color="var(--surface-muted)" />
            <ColorSwatch name="Border" color="var(--border)" />
            <ColorSwatch name="Text Primary" color="var(--text)" textColor="var(--bg)" />
            <ColorSwatch name="Text Muted" color="var(--text-muted)" textColor="var(--bg)" />
            <ColorSwatch name="Accent" color="var(--accent)" textColor="var(--bg)" />
            <ColorSwatch name="Accent Hover" color="var(--accent-hover)" textColor="var(--bg)" />
          </div>
        </section>

        {/* Typography */}
        <section className="space-y-4">
          <h2 className="text-2xl font-semibold">Typography</h2>
          <div className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6 space-y-4">
            <h1 className="text-4xl font-bold">Heading 1 - 4xl</h1>
            <h2 className="text-3xl font-semibold">Heading 2 - 3xl</h2>
            <h3 className="text-2xl font-semibold">Heading 3 - 2xl</h3>
            <h4 className="text-xl font-semibold">Heading 4 - xl</h4>
            <p className="text-base">
              Body text - Base. System font stack for optimal performance.
            </p>
            <p className="text-sm text-[var(--text-muted)]">
              Secondary text - Small with muted color.
            </p>
            <code className="text-sm">
              Code snippet - Monospace font with surface-muted background
            </code>
          </div>
        </section>

        {/* Buttons */}
        <section className="space-y-4">
          <h2 className="text-2xl font-semibold">Buttons</h2>
          <div className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6 space-y-4">
            <div className="flex flex-wrap gap-4">
              <button className="inline-flex items-center justify-center px-6 h-10 rounded-md bg-[var(--accent)] text-[var(--bg)] hover:bg-[var(--accent-hover)] font-medium transition-colors">
                Primary Action
              </button>
              <button className="inline-flex items-center justify-center px-6 h-10 rounded-md bg-[var(--surface-muted)] text-[var(--text)] hover:bg-[var(--border)] border border-[var(--border)] font-medium transition-colors">
                Secondary Action
              </button>
              <button className="inline-flex items-center justify-center px-6 h-10 rounded-md bg-transparent text-[var(--text)] hover:bg-[var(--surface-muted)] font-medium transition-colors">
                Ghost Button
              </button>
              <button
                className="inline-flex items-center justify-center px-6 h-10 rounded-md bg-[var(--surface-muted)] text-[var(--text-muted)] border border-[var(--border)] font-medium opacity-50 cursor-not-allowed"
                disabled
              >
                Disabled
              </button>
            </div>
          </div>
        </section>

        {/* Cards */}
        <section className="space-y-4">
          <h2 className="text-2xl font-semibold">Cards & Components</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6 space-y-3 hover:border-[var(--accent)] transition-colors">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold">Stat Card</h3>
                <span className="text-2xl">🏆</span>
              </div>
              <div className="space-y-1">
                <p className="text-3xl font-bold text-[var(--accent)]">1,240 XP</p>
                <p className="text-sm text-[var(--text-muted)]">Total experience</p>
              </div>
              <div className="w-full bg-[var(--surface-muted)] rounded-full h-2">
                <div className="bg-[var(--accent)] h-2 rounded-full" style={{ width: '65%' }} />
              </div>
            </div>

            <div className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6 space-y-3">
              <h3 className="text-lg font-semibold">Lesson Card</h3>
              <p className="text-sm text-[var(--text-muted)]">
                Learn the fundamentals of Java programming with interactive lessons.
              </p>
              <div className="flex items-center gap-2">
                <span className="px-2 py-1 bg-[var(--surface-muted)] border border-[var(--border)] rounded text-xs">
                  Beginner
                </span>
                <span className="px-2 py-1 bg-[var(--accent)] text-[var(--bg)] rounded text-xs font-medium">
                  +50 XP
                </span>
              </div>
            </div>

            <div className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6 space-y-3">
              <h3 className="text-lg font-semibold">Achievement</h3>
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 rounded-full bg-[var(--accent)] flex items-center justify-center text-2xl">
                  ⭐
                </div>
                <div>
                  <p className="font-medium">First Steps</p>
                  <p className="text-xs text-[var(--text-muted)]">Complete your first lesson</p>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Form Elements */}
        <section className="space-y-4">
          <h2 className="text-2xl font-semibold">Form Elements</h2>
          <div className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6 space-y-4 max-w-md">
            <div>
              <label className="block text-sm font-medium mb-2">Email Address</label>
              <input
                type="email"
                placeholder="you@example.com"
                className="w-full h-10 px-3 rounded-md bg-[var(--surface-muted)] text-[var(--text)] border border-[var(--border)] focus:outline-none focus:ring-2 focus:ring-[var(--accent)] focus:border-transparent transition-all"
              />
              <p className="mt-1 text-xs text-[var(--text-muted)]">We'll never share your email.</p>
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">Password</label>
              <input
                type="password"
                placeholder="••••••••"
                className="w-full h-10 px-3 rounded-md bg-[var(--surface-muted)] text-[var(--text)] border border-[var(--border)] focus:outline-none focus:ring-2 focus:ring-[var(--accent)] focus:border-transparent transition-all"
              />
            </div>
            <button className="w-full h-10 rounded-md bg-[var(--accent)] text-[var(--bg)] hover:bg-[var(--accent-hover)] font-medium transition-colors">
              Sign In
            </button>
          </div>
        </section>

        {/* Navigation Example */}
        <section className="space-y-4">
          <h2 className="text-2xl font-semibold">Navigation Example</h2>
          <div className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6">
            <p className="text-[var(--text-muted)] mb-4">
              Click the button below to navigate to the Dashboard page (separate route):
            </p>
            <a
              href="/dashboard"
              className="inline-flex items-center justify-center px-6 h-10 rounded-md
                         bg-[var(--accent)] text-[var(--bg)] hover:bg-[var(--accent-hover)]
                         font-medium transition-colors"
            >
              Go to Dashboard
            </a>
          </div>
        </section>

        {/* Footer */}
        <footer className="text-center text-sm text-[var(--text-muted)] py-8 border-t border-[var(--border)]">
          <p>
            GitGud MVP Phase 1A • Built with React, Vite, Tailwind CSS, React Query & React Router
          </p>
          <p className="mt-2">
            Step 6.1 Complete: Frontend infrastructure configured with sleek dark theme
          </p>
        </footer>
      </div>
    </div>
  );
}

/**
 * Color swatch component for palette showcase
 */
function ColorSwatch({ name, color, textColor = 'var(--text)' }) {
  return (
    <div
      className="rounded-lg p-4 border border-[var(--border)] space-y-2"
      style={{ backgroundColor: color }}
    >
      <div className="h-16 rounded-md" style={{ backgroundColor: color }} />
      <p className="text-sm font-medium" style={{ color: textColor }}>
        {name}
      </p>
      <code className="text-xs opacity-75" style={{ color: textColor }}>
        {color}
      </code>
    </div>
  );
}

/**
 * Dashboard page (placeholder)
 */
function Dashboard() {
  return (
    <div className="min-h-screen bg-[var(--bg)] text-[var(--text)] p-6">
      <h1 className="text-3xl font-bold mb-2">Dashboard</h1>
      <p className="text-[var(--text-muted)]">Welcome to GitGud!</p>
    </div>
  );
}

/**
 * 404 Not Found page
 */
function NotFound() {
  return (
    <div className="min-h-screen bg-[var(--bg)] text-[var(--text)] flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-6xl font-bold mb-4">404</h1>
        <p className="text-xl text-[var(--text-muted)] mb-6">Page not found</p>
        <a
          href="/"
          className="inline-flex items-center justify-center px-6 h-10 rounded-md
                     bg-[var(--surface)] text-[var(--text)] hover:bg-[var(--surface-muted)]
                     border border-[var(--border)] font-medium transition-colors"
        >
          Go Home
        </a>
      </div>
    </div>
  );
}

/**
 * Main routes configuration
 */
export function AppRoutes() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/" element={<Home />} />

      {/* Dashboard (will require authentication in 6.2) */}
      <Route path="/dashboard" element={<Dashboard />} />

      {/* Catch-all 404 */}
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

export default AppRoutes;
