import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import ForgotPassword from './pages/ForgotPassword';

/**
 * Main App component with routing configuration
 * Note: /login route will be implemented in a separate task
 */
function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        {/* Login route placeholder - to be implemented later */}
      </Routes>
    </Router>
  );
}

export default App;
