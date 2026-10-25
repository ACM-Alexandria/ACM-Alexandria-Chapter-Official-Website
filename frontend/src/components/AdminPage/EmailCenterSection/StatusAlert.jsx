import React, { useEffect, useState } from "react";

const STYLES = {
    error: {
        role: "alert",
        className: "border-red-200 bg-red-50 text-red-700 dark:border-red-900 dark:bg-red-950/40 dark:text-red-200",
    },
    success: {
        role: "status",
        className: "border-emerald-200 bg-emerald-50 text-emerald-800 dark:border-emerald-900 dark:bg-emerald-950/40 dark:text-emerald-200",
    },
};

// Shows a message, then hides it after `duration`; a new message shows again.
const StatusAlert = ({ type, message, duration = 3000 }) => {
    const [dismissed, setDismissed] = useState(null);

    useEffect(() => {
        if (!message) return undefined;
        const timer = setTimeout(() => setDismissed(message), duration);
        return () => clearTimeout(timer);
    }, [message, duration]);

    if (!message || dismissed === message) return null;
    const { role, className } = STYLES[type];
    return (
        <div className={`mb-4 rounded-lg border px-4 py-3 text-sm ${className}`} role={role}>
            {message}
        </div>
    );
};

export default StatusAlert;