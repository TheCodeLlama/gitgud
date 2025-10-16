import { createContext, useContext, useEffect, useState } from 'react';
import {
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  signOut,
  onAuthStateChanged,
  GoogleAuthProvider,
  GithubAuthProvider,
  signInWithPopup,
  updateProfile,
} from 'firebase/auth';
import { auth } from '../config/firebase';
import { api } from '../lib/api';
import { queryClient } from '../lib/queryClient';

const AuthContext = createContext(null);

/**
 * AuthProvider - Manages authentication state with Firebase
 */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let refreshInterval = null;

    // Listen for auth state changes
    const unsubscribe = onAuthStateChanged(auth, async (firebaseUser) => {
      // Clear any existing refresh interval
      if (refreshInterval) {
        clearInterval(refreshInterval);
        refreshInterval = null;
      }

      if (firebaseUser) {
        // User is signed in
        const idToken = await firebaseUser.getIdToken();

        // Set token in API client first (needed for sync call)
        api.defaults.headers.common['Authorization'] = `Bearer ${idToken}`;
        setToken(idToken);

        // Store token in localStorage (for page refreshes)
        localStorage.setItem('firebase_token', idToken);

        // Sync user with backend (send displayName as username for new users)
        try {
          const syncData = firebaseUser.displayName
            ? { username: firebaseUser.displayName }
            : {};
          const syncResponse = await api.post('/v1/auth/sync', syncData);

          // Use backend user data from sync response
          const backendUser = syncResponse.data.data;
          setUser({
            // Firebase data
            uid: firebaseUser.uid,
            photoURL: firebaseUser.photoURL,
            // Backend data (includes username, displayName, email, etc.)
            ...backendUser,
          });
        } catch (error) {
          console.error('Failed to sync user with backend:', error);
          // Fallback to Firebase data only
          setUser({
            uid: firebaseUser.uid,
            email: firebaseUser.email,
            displayName: firebaseUser.displayName || firebaseUser.email,
            photoURL: firebaseUser.photoURL,
          });
        }

        setAuthenticated(true);

        // Set up token refresh (Firebase tokens expire after 1 hour)
        // Refresh token every 50 minutes to stay ahead of expiration
        refreshInterval = setInterval(async () => {
          try {
            const newToken = await firebaseUser.getIdToken(true); // Force refresh
            setToken(newToken);
            api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
            localStorage.setItem('firebase_token', newToken);
          } catch (error) {
            console.error('Failed to refresh token:', error);
          }
        }, 50 * 60 * 1000); // 50 minutes
      } else {
        // User is signed out
        setUser(null);
        setToken(null);
        setAuthenticated(false);
        delete api.defaults.headers.common['Authorization'];
        localStorage.removeItem('firebase_token');

        // Clear React Query cache to prevent data leaking between users
        queryClient.clear();
      }

      setLoading(false);
    });

    // Cleanup subscription and interval on unmount
    return () => {
      unsubscribe();
      if (refreshInterval) {
        clearInterval(refreshInterval);
      }
    };
  }, []);

  /**
   * Register new user with email and password
   */
  const register = async (email, password, username) => {
    try {
      const userCredential = await createUserWithEmailAndPassword(auth, email, password);

      // Set the username as the display name on Firebase profile
      if (username) {
        await updateProfile(userCredential.user, {
          displayName: username,
        });

        // Force refresh the ID token to include the updated displayName
        await userCredential.user.getIdToken(true);
      }

      return userCredential.user;
    } catch (error) {
      console.error('Registration failed:', error);
      throw new Error(error.message || 'Registration failed');
    }
  };

  /**
   * Sign in with email and password
   */
  const login = async (email, password) => {
    try {
      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      return userCredential.user;
    } catch (error) {
      console.error('Login failed:', error);
      throw new Error(error.message || 'Login failed');
    }
  };

  /**
   * Sign in with Google
   */
  const loginWithGoogle = async () => {
    try {
      const provider = new GoogleAuthProvider();
      const userCredential = await signInWithPopup(auth, provider);
      return userCredential.user;
    } catch (error) {
      console.error('Google login failed:', error);
      throw new Error(error.message || 'Google login failed');
    }
  };

  /**
   * Sign in with GitHub
   */
  const loginWithGithub = async () => {
    try {
      const provider = new GithubAuthProvider();
      const userCredential = await signInWithPopup(auth, provider);
      return userCredential.user;
    } catch (error) {
      console.error('GitHub login failed:', error);
      throw new Error(error.message || 'GitHub login failed');
    }
  };

  /**
   * Sign out
   */
  const logout = async () => {
    try {
      await signOut(auth);
      setUser(null);
      setToken(null);
      setAuthenticated(false);
      delete api.defaults.headers.common['Authorization'];
      localStorage.removeItem('firebase_token');

      // Clear React Query cache to prevent data leaking between users
      queryClient.clear();
    } catch (error) {
      console.error('Logout failed:', error);
      throw new Error(error.message || 'Logout failed');
    }
  };

  const value = {
    user,
    token,
    authenticated,
    loading,
    register,
    login,
    logout,
    loginWithGoogle,
    loginWithGithub,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

/**
 * useAuth hook - Access authentication context
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}

export default AuthContext;
