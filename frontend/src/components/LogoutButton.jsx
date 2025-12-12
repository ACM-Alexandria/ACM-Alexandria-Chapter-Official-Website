import { useState } from 'react';

function LogoutButton() {
  const [isLoading, setIsLoading] = useState(false);

  const handleLogout = async () => {
    setIsLoading(true);

    try {
      // Call the logout API
      const response = await fetch('/api/v1/auth/logout', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          // Include auth token if stored in localStorage
          ...(localStorage.getItem('accessToken') && {
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
          })
        },
      });

      if (response.ok) {
        // Clear all authentication-related localStorage
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        localStorage.removeItem('userProfile');

        // Clear all items that might be auth-related
        const keysToRemove = [];
        for (let i = 0; i < localStorage.length; i++) {
          const key = localStorage.key(i);
          if (key && (key.includes('token') || key.includes('auth') || key.includes('user'))) {
            keysToRemove.push(key);
          }
        }
        keysToRemove.forEach(key => localStorage.removeItem(key));

        // Redirect to login page
        window.location.href = '/login';
      } else if (response.status >= 500) {
        // Show error message for 5xx errors
        alert('An error occurred. Please try again.');
        setIsLoading(false);
      } else {
        // For other errors, still show the message
        alert('An error occurred. Please try again.');
        setIsLoading(false);
      }
    } catch (error) {
      // Network error or other failure
      alert('An error occurred. Please try again.');
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
