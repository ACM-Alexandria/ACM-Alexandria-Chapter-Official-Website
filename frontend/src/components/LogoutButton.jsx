import { useState } from 'react';
import { useAuth } from '../hooks/useAuth';

function LogoutButton() {
  const [isLoading, setIsLoading] = useState(false);
    const { logout } = useAuth();

  const handleLogout = async () => {
    setIsLoading(true);

    try {
      // Use secure logout from auth service
      // This clears tokens from secure storage (memory + sessionStorage)
      await logout();

      // Redirect to login page
      window.location.href = '/login';
    } catch (error) {
      // Network error or other failure
      console.error('Logout failed:', error);
      alert('An error occurred during logout. Please try again.');
      setIsLoading(false);
    }
  };

  return (
    <button
      onClick={handleLogout}
      disabled={isLoading}
      className="logout-button"
    >
      {isLoading ? 'Logging out...' : 'Logout'}
    </button>
  );
}

export default LogoutButton;
