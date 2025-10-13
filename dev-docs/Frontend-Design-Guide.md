# Frontend Design Guide (MVP)

A minimalist, mostly monochrome design system (black, white, and gray) with a single orange accent for emphasis. Optimized for clarity, speed, and accessibility. Dark mode only. Built to align with the MVP stack: React + Vite (JavaScript), Tailwind CSS, React Router, React Query, Axios, Zustand/Redux, and Monaco Editor.

---

## 1. Design Principles
- Clarity over decoration: favor whitespace, clear hierarchy, and readable typography.
- Consistency: one scale for spacing, radii, and typography.
- Contrast-driven: grayscale UI with hierarchy via contrast, typography weight, and spacing; a single orange accent is used for primary actions and key highlights.
- Accessible by default: AA/AAA contrast targets, visible focus states, reduced motion preferences respected.
- Progressive disclosure: show essentials first, reveal details on demand.
- Mobile-first, responsive: design for small screens first; scale up.

---

## 2. Color System
Strictly monochromatic: black, white, and gray only. Ensure sufficient contrast in dark mode.

- Dark Mode Neutrals:
  - Background (App): `#0B0B0B` (near-black)
  - Surface (Cards/Containers): `#111317`
  - Muted Surface: `#161A1E`
  - Border: `#262B31`
  - Text Primary: `#F5F6F7` (near-white)
  - Text Secondary: `#9CA3AF` (muted gray)

- Notes:
  - Single accent color (Orange): `#F97316` (base), `#EA580C` (hover). Use sparingly for primary actions, links, progress, and key highlights. Avoid using accent for large surfaces.
  - Grayscale remains the foundation; rely on hierarchy, weight, spacing, and icons for most emphasis.
  - For rare emphasis outside actions, an inverted grayscale treatment (e.g., near-white chip with dark text) is acceptable but secondary to orange accent usage.

---

## 3. Typography
- Font: System UI stack or Inter (if webfont is acceptable later). Start with system font for MVP performance.
- Scale: 1.25 modular scale.
  - Display: 30–36px
  - H1: 24–28px
  - H2: 20–22px
  - H3: 18–20px
  - Body: 16px
  - Secondary: 14px
  - Code/Monospace: Monaco defaults for editor; UI code snippets use `ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace`.
- Line-height: 1.4–1.6 for body; headings tighter at 1.25–1.3.

---

## 4. Spacing, Radius, Elevation
- Spacing scale (Tailwind): 1, 2, 4, 6, 8, 12, 16, 24, 32, 48 (in px equivalents via Tailwind `space` tokens).
- Corner radius: 6px default; 10px for prominent surfaces; full for pills.
- Elevation: Minimal. Use subtle shadows for affordance only.
  - Shadow XS: `0 1px 2px rgba(0,0,0,0.04)`
  - Shadow SM: `0 2px 8px rgba(0,0,0,0.06)`

---

## 5. Dark Mode Theming
Use CSS variables scoped to `:root`. The application ships in dark mode only; no theme toggle or light palette is provided.

Example variables (add to a global CSS file, e.g., `src/styles/theme.css`):

```css
:root {
  --bg: #0B0B0B;
  --surface: #111317;
  --surface-muted: #161A1E;
  --border: #262B31;
  --text: #F5F6F7;
  --text-muted: #9CA3AF;

  /* Accent (orange) */
  --accent: #F97316;        /* primary accent */
  --accent-hover: #EA580C;  /* hover state */
  --accent-ring: rgba(249, 115, 22, 0.7);

  /* Monochrome action tokens */
  --interactive: #E5E7EB;        /* button bg, toggles */
  --interactive-hover: #D1D5DB;  /* hover state */
  --focus-ring: #FFFFFF;         /* focus outline */
}

/* Apply tokens */
body { background: var(--bg); color: var(--text); }
```

---

## 6. Tailwind Configuration (Guidance)
Edit `tailwind.config.ts` to expose tokens. Dark-only, so no `darkMode` toggle is required.

```ts
// tailwind.config.ts
import type { Config } from 'tailwindcss'

export default {
  content: ['./index.html', './src/**/*.{ts,tsx,js,jsx}'],
  theme: {
    extend: {
      colors: {
        bg: 'var(--bg)',
        surface: 'var(--surface)',
        surfaceMuted: 'var(--surface-muted)',
        border: 'var(--border)',
        text: 'var(--text)',
        textMuted: 'var(--text-muted)',
        accent: 'var(--accent)',
        accentHover: 'var(--accent-hover)',
        accentRing: 'var(--accent-ring)',
        interactive: 'var(--interactive)',
        interactiveHover: 'var(--interactive-hover)',
        focusRing: 'var(--focus-ring)'
      },
      borderRadius: {
        md: '6px',
        lg: '10px'
      },
      boxShadow: {
        xs: '0 1px 2px rgba(0,0,0,0.04)',
        sm: '0 2px 8px rgba(0,0,0,0.06)'
      }
    }
  },
  plugins: []
} satisfies Config
```

---

## 7. Layout Patterns
- App Shell (Desktop): Top navbar (56–64px), collapsible left sidebar (280px default, 72px collapsed), content container max-width 1280px.
- App Shell (Mobile): Top navbar with menu button; slide-over sidebar.
- Content spacing: outer padding 16–24px; vertical rhythm 16px.
- Grid: 12-column responsive grid; cards in 1–3 columns depending on breakpoints.

Key screens to consider (per MVP guide):
- Auth (Login/Register/Forgot Password)
- Dashboard (XP, level, streaks, recent activity)
- Modules and Lessons list
- Lesson Player (Monaco editor, instructions, test results)
- Profile/Settings

---

## 8. Components (Usage + Behavior)
Use Tailwind utilities; keep variants limited for MVP.

1) Buttons
- Sizes: sm (28–32px), md (36–40px), lg (44–48px)
- Variants:
  - Primary: accent border (`--accent`); hover changes border to `--accent-hover`.
  - Secondary: surface background, text primary, visible border; hover uses interactive-hover tint
  - Ghost: transparent bg, text primary, subtle hover on surface
- States: hover, active, disabled, loading; focus ring with 2px high-contrast outline

Example:
```tsx
<button
  className="inline-flex items-center justify-center rounded-md px-4 h-10
             bg-[var(--accent)] text-[var(--bg)] hover:bg-[var(--accent-hover)]
             focus:outline-none focus-visible:ring-2 focus-visible:ring-[var(--accent-ring)]
             disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
>
  Continue
</button>
```

2) Inputs & Forms
- Label always visible; helper text for validation.
- Hit area min-height 40px. Clear error messaging.
```tsx
<label className="block text-sm mb-1">Email</label>
<input
  className="w-full h-10 px-3 rounded-md bg-surface text-text border border-[var(--border)]
             focus:outline-none focus:ring-2 focus:ring-[var(--focus-ring)]"
  type="email" placeholder="you@example.com"/>
<p className="mt-1 text-xs text-[var(--text-muted)]">We'll never share your email.</p>
```

3) Cards
- Surface container with padding 16–24px; use for dashboard widgets and lesson info.
```tsx
<div className="bg-surface border border-[var(--border)] rounded-lg shadow-xs p-4">...</div>
```

4) Navigation
- Topbar: logo, product name, XP/level, streak indicator, avatar menu.
- Sidebar: sections for Dashboard, Modules, Achievements, Profile.
- Active item: left accent border (2px) or subtle surface tint; ensure sufficient contrast for text.

5) Tables/Lists
- Use dense rows; zebra striping optional; responsive: stack cells on small screens.

6) Modals & Drawers
- Use for confirmation and secondary flows. Dismiss via ESC and backdrop click. Trap focus.

7) Toaster/Alerts
- Non-blocking notifications at top-right. Use icons and grayscale emphasis; keep copy concise.

8) Skeletons & Loading
- Use skeletons for cards/lists; spinner only for small inline waits.

9) Monaco Editor Area
- Respect theme (use `vs-dark` when dark mode). Surround with a surface container; clear run/test actions in the header with a primary action button using the interactive tokens.

---

## 9. Iconography & Illustration
- Use a minimal icon set (e.g., Heroicons outline). Keep stroke width consistent.
- Avoid heavy illustrations in MVP; use small empty-state icons.

---

## 10. Accessibility
- Color contrast ≥ AA for text and UI; aim for AAA for body text.
- Keyboard-first: all interactive elements reachable and operable; visible focus styles.
- ARIA: meaningful labels for nav, dialogs, toasts. Announce state changes where needed.
- Motion: respect `prefers-reduced-motion`; avoid large parallax/animations.

---

## 11. Interaction & Feedback
- Hover and focus differ: hover = subtle brightness lift; focus = high-contrast ring.
- Click/Active state: slight darken and scale(0.98) optional for buttons.
- Disabled: lower opacity, cursor-not-allowed, no hover.
- Empty states: explain what to do next; provide a primary action.
- Error states: clear message + remediation; avoid blameful tone.

---

## 12. Responsive Breakpoints
Use Tailwind defaults: `sm` 640, `md` 768, `lg` 1024, `xl` 1280, `2xl` 1536. Design mobile-first and enhance at each breakpoint.

---

## 13. Example Page Shell (React)
```tsx
// AppShell.tsx
export function AppShell({ sidebar, children }: { sidebar: React.ReactNode; children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-[var(--bg)] text-[var(--text)]">
      <header className="h-14 border-b border-[var(--border)] bg-surface">
        <div className="mx-auto max-w-7xl h-full flex items-center justify-between px-4">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded bg-[var(--text)]" />
            <span className="font-semibold">GitGud</span>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-sm text-[var(--text-muted)]">Lvl 5 · 1,240 XP</span>
            <button className="rounded-full w-8 h-8 bg-surface border border-[var(--border)]"/>
          </div>
        </div>
      </header>
      <div className="mx-auto max-w-7xl px-4 grid grid-cols-12 gap-6 py-6">
        <aside className="hidden md:block col-span-3 lg:col-span-2">{sidebar}</aside>
        <main className="col-span-12 md:col-span-9 lg:col-span-10">{children}</main>
      </div>
    </div>
  )
}
```

---

## 14. Theme Toggle
Removed. The product is dark mode only; do not implement a theme toggle or light palette.

---

## 15. Content Style & UX Writing
- Use short, instructive labels: "Run Tests", "Submit", "Try Again".
- Avoid jargon; tooltips for advanced terms.
- Success messages celebrate lightly; errors are actionable.

---

## 16. Performance Considerations
- Prefer system fonts; defer non-critical webfonts.
- Lazy-load heavy routes (e.g., lesson player).
- Avoid excessive shadows/filters; minimize DOM depth.

---

## 17. Alignment with MVP Docs
- Matches Tailwind-first approach and React SPA routing.
- Supports gamification UI (XP, levels, streaks, achievements) with clear components.
- Monaco editor integration respects theme.

---

## 18. Next Steps (Post-MVP Enhancements)
- Refine grayscale ramps and contrast tokens.
- Expand component library (tabs, stepper, breadcrumbs).
- Motion primitives for micro-interactions.
- Theming presets for branding variants.
