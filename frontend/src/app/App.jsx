import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Layout from "./components/Layout";
import Navbar from "./components/navbar/Navbar";
import Hero from "./components/hero/Hero";

function App() {
  return (
    <Router>
      <Layout>
        <Navbar />
        <Hero />
      </Layout>
    </Router>
  );
}

export default App;
