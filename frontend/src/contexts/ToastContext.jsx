import { createContext, useContext, useState, useCallback } from 'react';
import Toast from '../components/ui/Toast';

const ToastContext = createContext(null);

/**
 * Toast Provider component
 * Manages global toast notifications state and rendering
 */
export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  // Add a new toast
  const addToast = useCallback((toast) => {
    const id = Date.now() + Math.random();
    setToasts((prev) => [...prev, { id, ...toast }]);
    return id;
  }, []);

  // Show achievement toast
  const showAchievement = useCallback(
    (achievement) => {
      return addToast({
        type: 'achievement',
        achievement,
        duration: 6000, // Show achievements longer
      });
    },
    [addToast]
  );

  // Show message toast
  const showMessage = useCallback(
    (message, duration = 4000) => {
      return addToast({
        type: 'message',
        message,
        duration,
      });
    },
    [addToast]
  );

  // Remove a toast
  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id));
  }, []);

  // Clear all toasts
  const clearToasts = useCallback(() => {
    setToasts([]);
  }, []);

  const value = {
    addToast,
    showAchievement,
    showMessage,
    removeToast,
    clearToasts,
  };

  return (
    <ToastContext.Provider value={value}>
      {children}

      {/* Toast container - fixed position in top-right corner */}
      <div className="fixed top-20 right-4 z-50 flex flex-col gap-3 pointer-events-none">
        {toasts.map((toast) => (
          <div key={toast.id} className="pointer-events-auto">
            <Toast {...toast} onClose={removeToast} />
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

/**
 * Custom hook to access toast functions
 */
export function useToast() {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
}
