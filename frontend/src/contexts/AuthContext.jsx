import { createContext, useContext, useEffect, useState } from 'react';
import Keycloak from 'keycloak-js';
import keycloakConfig from '../config/keycloak';
import { api } from '../lib/api';
import axios from 'axios';

const AuthContext = createContext(null);

/**
 * AuthProvider - Manages authentication state with direct login/registration
 */
export function AuthProvider({ children }) {
  const [keycloak, setKeycloak] = useState(null);
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);

  useEffect(() => {
    // Initialize Keycloak for SSO check and OAuth flows
    const keycloakInstance = new Keycloak(keycloakConfig);

    keycloakInstance
      .init({
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
        pkceMethod: 'S256',
      })
      .then((authenticated) => {
        setKeycloak(keycloakInstance);

        if (authenticated) {
          // User authenticated via SSO or OAuth
          setAuthenticated(true);
          setToken(keycloakInstance.token);
          api.defaults.headers.common['Authorization'] = `Bearer ${keycloakInstance.token}`;
          loadUserProfile(keycloakInstance);

          // Set up token refresh
          setInterval(() => {
            keycloakInstance.updateToken(70).then((refreshed) => {
              if (refreshed) {
                setToken(keycloakInstance.token);
                api.defaults.headers.common['Authorization'] = `Bearer ${keycloakInstance.token}`;
              }
            }).catch(() => {
              console.error('Failed to refresh token');
            });
          }, 60000);
        } else {
          // Check if user has token in localStorage (direct login)
          const storedToken = localStorage.getItem('access_token');
          const storedUser = localStorage.getItem('user');

          if (storedToken && storedUser) {
            setAuthenticated(true);
            setToken(storedToken);
            setUser(JSON.parse(storedUser));
            api.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`;
          }
        }

        setLoading(false);
      })
      .catch((error) => {
        console.error('Keycloak initialization failed:', error);
        setLoading(false);
      });
  }, []);

  const loadUserProfile = async (keycloakInstance) => {
    try {
      const profile = await keycloakInstance.loadUserProfile();
      const userData = {
        id: profile.id,
        email: profile.email,
        username: profile.username,
        firstName: profile.firstName,
        lastName: profile.lastName,
      };
      setUser(userData);
      localStorage.setItem('user', JSON.stringify(userData));
    } catch (error) {
      console.error('Failed to load user profile:', error);
    }
  };

  /**
   * Direct username/password login using Keycloak's token endpoint
   */
  const login = async (username, password) => {
    try {
      const tokenUrl = `${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/token`;

      const params = new URLSearchParams();
      params.append('client_id', keycloakConfig.clientId);
      params.append('grant_type', 'password');
      params.append('username', username);
      params.append('password', password);
      params.append('scope', 'openid profile email');

      const response = await axios.post(tokenUrl, params, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      });

      const { access_token, refresh_token } = response.data;

      // Store tokens
      localStorage.setItem('access_token', access_token);
      localStorage.setItem('refresh_token', refresh_token);
      setToken(access_token);
      setAuthenticated(true);

      // Configure API client
      api.defaults.headers.common['Authorization'] = `Bearer ${access_token}`;

      // Fetch user info
      await fetchUserInfo(access_token);

      return true;
    } catch (error) {
      console.error('Login failed:', error);
      throw new Error(error.response?.data?.error_description || 'Login failed');
    }
  };

  /**
   * Register new user using backend API (which uses Keycloak Admin API)
   */
  const register = async (userData) => {
    try {
      const response = await api.post('/v1/auth/register', {
        email: userData.email,
        username: userData.username,
        firstName: userData.firstName,
        lastName: userData.lastName,
        password: userData.password,
      });

      // Auto-login after registration
      await login(userData.username, userData.password);

      return response.data;
    } catch (error) {
      console.error('Registration failed:', error);
      throw new Error(error.response?.data?.message || 'Registration failed');
    }
  };

  /**
   * Fetch user information from token
   */
  const fetchUserInfo = async (accessToken) => {
    try {
      const userInfoUrl = `${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/userinfo`;

      const response = await axios.get(userInfoUrl, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });

      const userData = {
        id: response.data.sub,
        email: response.data.email,
        username: response.data.preferred_username,
        firstName: response.data.given_name,
        lastName: response.data.family_name,
      };

      setUser(userData);
      localStorage.setItem('user', JSON.stringify(userData));
    } catch (error) {
      console.error('Failed to fetch user info:', error);
    }
  };

  /**
   * Logout - clear tokens and user data
   */
  const logout = () => {
    setUser(null);
    setToken(null);
    setAuthenticated(false);
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user');
    delete api.defaults.headers.common['Authorization'];

    // If Keycloak session exists, also logout from Keycloak
    if (keycloak?.authenticated) {
      keycloak.logout();
    }
  };

  /**
   * OAuth login with Google
   */
  const loginWithGoogle = () => {
    keycloak?.login({
      idpHint: 'google',
    });
  };

  /**
   * OAuth login with GitHub
   */
  const loginWithGithub = () => {
    keycloak?.login({
      idpHint: 'github',
    });
  };

  const value = {
    keycloak,
    authenticated,
    loading,
    user,
    token,
    login,
    logout,
    register,
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
