/**
 * GitGud App Root Component
 * Integrates React Query, React Router, Keycloak Auth, and provides the application shell
 */

import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { queryClient } from './lib/queryClient';
import { AppRoutes } from './routes';
import { AuthProvider } from './contexts/AuthContext';
import { ToastProvider } from './contexts/ToastContext';
import UsernameGuard from './components/UsernameGuard';

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>
          <UsernameGuard>
            <div className="min-h-screen bg-[var(--bg)]">
              <AppRoutes />
            </div>

            {/* React Query Devtools (only in development) */}
            {import.meta.env.DEV && <ReactQueryDevtools initialIsOpen={false} />}
          </UsernameGuard>
        </ToastProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
