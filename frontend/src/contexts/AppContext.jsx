import { createContext } from "react";

const AppContext = createContext();

export const AppProvider = ({ children }) => {
  return <AppContext.Provider>{children}</AppContext.Provider>;
};

export default AppContext;
