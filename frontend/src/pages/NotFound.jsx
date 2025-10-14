/**
 * 404 Not Found page
 */
export default function NotFound() {
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
