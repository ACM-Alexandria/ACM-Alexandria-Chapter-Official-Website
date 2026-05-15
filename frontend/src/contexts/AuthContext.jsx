import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
} from "react";
import tokenService from "../services/tokenService";
import {
  login as apiLogin,
  logout as apiLogout,
  register as apiRegister,
  refreshAccessToken,
  getMe as apiGetMe,
} from "../services/authService";

const AuthContext = createContext({
  isAuthenticated: false,
  isLoading: true,
  user: null,
  login: async () => {},
  logout: async () => {},
  register: async () => {},
  updateUser: () => {},
});

/**
 * AuthProvider — single source of truth for authentication state.
 * Wraps the app in main.jsx.
 *
 * On mount, attempts to restore session from encrypted IndexedDB refresh token.
 * Listens for `auth:logout` events dispatched by the axios 401 interceptor.
 */
export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState(null);

  // Restore session on mount
  useEffect(() => {
    const restoreSession = async () => {
      const hydrateUser = async () => {
        try {
          const data = await apiGetMe();
          if (data?.email) {
            setUser({ email: data.email });
          }
        } catch {
          setUser(null);
        }
      };

      try {
        await tokenService.initializeTokenService();

        if (tokenService.hasAccessToken()) {
          setIsAuthenticated(true);
          await hydrateUser();
          setIsLoading(false);
          return;
        }

        if (tokenService.hasRefreshToken()) {
          try {
            await refreshAccessToken();
            setIsAuthenticated(true);
            await hydrateUser();
          } catch {
            await tokenService.clearAllTokens();
          }
        }
      } catch (error) {
        console.error("Failed to initialize auth:", error);
      } finally {
        setIsLoading(false);
      }
    };

    restoreSession();
  }, []);

  // Listen for forced-logout events (e.g. 401 interceptor)
  useEffect(() => {
    const handleForcedLogout = async () => {
      await tokenService.clearAllTokens();
      setIsAuthenticated(false);
      setUser(null);
    };

    window.addEventListener("auth:logout", handleForcedLogout);
    return () => window.removeEventListener("auth:logout", handleForcedLogout);
  }, []);

  const login = useCallback(async (email, password) => {
    const data = await apiLogin(email, password);
    setIsAuthenticated(true);

    if (data.id && data.email) {
      setUser({ id: data.id, email: data.email });
    }

    return data;
  }, []);

  const register = useCallback(async (userData) => {
    return await apiRegister(userData);
  }, []);

  const logout = useCallback(async () => {
    await apiLogout();
    setIsAuthenticated(false);
    setUser(null);
  }, []);

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

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

/**
 * Hook to access authentication state and methods.
 * Must be used within an AuthProvider.
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};

export default useAuth;
