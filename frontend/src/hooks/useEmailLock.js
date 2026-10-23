import { useEffect, useState } from "react";
import { fetchEmailLock } from "../services/adminService";

// Site-wide email lock: true while site emails are sent. Read-only; updating it is SUPER_ADMIN only.
const useEmailLock = () => {
  const [emailsEnabled, setEmailsEnabled] = useState(true);

  useEffect(() => {
    fetchEmailLock()
      .then((data) => setEmailsEnabled(data?.emailsEnabled !== false))
      .catch((err) => console.error("Failed to load email lock status", err));
  }, []);

  return [emailsEnabled, setEmailsEnabled];
};

export default useEmailLock;
