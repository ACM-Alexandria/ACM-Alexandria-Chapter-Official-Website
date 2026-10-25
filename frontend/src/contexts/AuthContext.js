import { createContext } from "react";

// The signed-in user and the auth actions, provided by AuthProvider (AuthProvider.jsx) and read with useAuth.
export const AuthContext = createContext({
  isAuthenticated: false,
  isLoading: true,
  user: null,
  login: async () => {},
  loginWithGoogle: async () => {},
  logout: async () => {},
  register: async () => {},
  updateUser: () => {},
});
