import React from "react";
import { Footer } from "./Footer.jsx";

const Layout = ({ children }) => {
  return (
    <div className="min-h-screen flex flex-col">
      <main className="flex-1">{children}</main>
      <Footer />
    </div>
  );
};

export default Layout;
