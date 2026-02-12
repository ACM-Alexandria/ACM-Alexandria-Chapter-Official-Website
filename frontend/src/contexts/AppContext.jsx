import { createContext, useState } from "react";

const AppContext = createContext();

export const AppProvider = ({ children }) => {
  
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  const value = {
    user,
    setUser,
    isAuthenticated,
    setIsAuthenticated,
  };

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
};

export default AppContext;