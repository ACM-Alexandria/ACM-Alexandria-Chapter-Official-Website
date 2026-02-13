import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import tokenService from '../services/tokenService';
import {
  login as apiLogin,
  logout as apiLogout,
  register as apiRegister,
  refreshAccessToken
} from '../services/authApi';

// Create context with default values
const AuthContext = createContext({
  isAuthenticated: false,
  isLoading: true,
  user: null,
  login: async () => {},
  logout: async () => {},
  register: async () => {},
});

/**
 * Authentication Provider Component
 * Wrap your app with this to enable auth state management
 *
 * @param {Object} props
 * @param {React.ReactNode} props.children - Child components
 */
export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  /**
   * Handle forced logout from interceptor or other sources
   * This prevents page reload and provides smooth UX
   */
  const handleForcedLogout = useCallback(async (event) => {
    const reason = event?.detail?.reason || 'Session expired';
    
    console.log('Forced logout:', reason);

    // Clear auth state
    setIsAuthenticated(false);
    setUser(null);

    // Clear tokens (might already be cleared by interceptor)
    await tokenService.clearAllTokens();

    // Navigate to login without page reload
    navigate('/login', { 
      replace: true,
      state: { message: reason }
    });
  }, [navigate]);

  /**
   * Attempt to restore session on mount
   * If refresh token exists, try to get a new access token
   */
  useEffect(() => {
    const restoreSession = async () => {
      try {
        // Initialize token service (loads refresh token from encrypted storage)
        await tokenService.initializeTokenService();

        // Check if there's an existing access token in memory
        if (tokenService.hasAccessToken()) {
          setIsAuthenticated(true);
          setIsLoading(false);
          return;
        }

        // Try to restore session using refresh token
        if (tokenService.hasRefreshToken()) {
          try {
            await refreshAccessToken();
            setIsAuthenticated(true);
          } catch (error) {
            console.warn('Session restoration failed:', error);
            await tokenService.clearAllTokens();
          }
        }
      } catch (error) {
        console.error('Failed to initialize auth:', error);
      } finally {
        setIsLoading(false);
      }
    };

    restoreSession();
  }, []);

  useEffect(() => {
    window.addEventListener('auth:logout', handleForcedLogout);

    return () => {
      window.removeEventListener('auth:logout', handleForcedLogout);
    };
  }, [handleForcedLogout]);

  /**
   * Login with email and password
   * @param {string} email - User email
   * @param {string} password - User password
   * @returns {Promise<Object>} Login response data
   */
  const login = useCallback(async (email, password) => {
    const data = await apiLogin(email, password);
    setIsAuthenticated(true);

    // Store user data if returned
    if (data.user || data.email) {
      setUser(data.user || { email: data.email, id: data.id });
    }

    return data;
  }, []);

  /**
   * Register a new user
   * @param {Object} userData - User registration data
   * @returns {Promise<Object>} Registration response data
   */
  const register = useCallback(async (userData) => {
    return await apiRegister(userData);
  }, []);

  /**
   * Logout current user
   * Clears all tokens and resets auth state
   */
  const logout = useCallback(async () => {
    await apiLogout();
    setIsAuthenticated(false);
    setUser(null);
  }, []);

  /**
   * Update user data
   * @param {Object} userData - Updated user data
   */
  const updateUser = useCallback((userData) => {
    setUser(userData);
  }, []);

  const value = {
    isAuthenticated,
    isLoading,
    user,
    login,
    logout,
    register,
    updateUser,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * Hook to access authentication state and methods
 * Must be used within an AuthProvider
 *
 * @returns {Object} Auth context value
 * @throws {Error} If used outside AuthProvider
 */
export const useAuth = () => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }

  return context;
};

export default useAuth;