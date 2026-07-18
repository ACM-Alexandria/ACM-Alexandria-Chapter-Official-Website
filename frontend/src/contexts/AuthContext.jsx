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
          console.log("[AuthContext] Hydrated CurrentUser Payload:", data);

          // Fallback to email-only if backend is returning legacy payloads
          if (data && (data.email || data.id)) {
            setUser({ 
              id: data.id || data.userId || null, 
              email: data.email || null,
              role: data.role || null
            });
            setIsAuthenticated(true);
            return true;
          }
        } catch (err) {
          console.warn("Session hydration failed:", err);
        }
        // If failed, ensure clean unauthenticated state
        setUser(null);
        setIsAuthenticated(false);
        return false;
      };

      try {
        await tokenService.initializeTokenService();

        if (tokenService.hasAccessToken()) {
          const hydrated = await hydrateUser();
          // If access token hydrated successfully, we are done!
          if (hydrated) {
            setIsLoading(false);
            return;
          }
          
          // If hydration failed (e.g. expired access token), attempt refresh
          if (tokenService.hasRefreshToken()) {
            try {
              await refreshAccessToken();
              await hydrateUser();
            } catch {
              await tokenService.clearAllTokens();
            }
          } else {
            await tokenService.clearAllTokens();
          }
        } else if (tokenService.hasRefreshToken()) {
          try {
            await refreshAccessToken();
            await hydrateUser();
          } catch {
            await tokenService.clearAllTokens();
            setIsAuthenticated(false);
            setUser(null);
          }
        } else {
          // No tokens available
          setIsAuthenticated(false);
          setUser(null);
        }
      } catch (error) {
        console.error("Failed to initialize auth:", error);
        setIsAuthenticated(false);
        setUser(null);
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
      setUser({ id: data.id, email: data.email, role: data.role || null });
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
