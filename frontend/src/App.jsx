import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Layout from "./components/Layout";
import Navbar from "./components/Navbar";
import Hero from "./components/Hero";

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
