import { useState, useEffect } from "react";
import { fetchUserProfile } from "../services/userService";

const profileCache = new Map();

export const useUserProfile = (userId) => {
  const [profile, setProfile] = useState(profileCache.get(userId) || null);
  const [loading, setLoading] = useState(!profileCache.has(userId));
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!userId) {
      setLoading(false);
      return;
    }

    if (profileCache.has(userId)) {
      setProfile(profileCache.get(userId));
      setLoading(false);
      return;
    }

    const loadProfile = async () => {
      setLoading(true);
      try {
        const data = await fetchUserProfile(userId);
        profileCache.set(userId, data);
        setProfile(data);
      } catch (err) {
        console.error(`Failed to load profile for ${userId}`, err);
        setError(err);
      } finally {
        setLoading(false);
      }
    };

    loadProfile();
  }, [userId]);

  return { profile, loading, error };
};
