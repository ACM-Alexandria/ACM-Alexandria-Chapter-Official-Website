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

const StatusAlert = ({ type, message, duration = 3000 }) => {
    const [visible, setVisible] = useState(false);

    useEffect(() => {
        if (!message) {
            setVisible(false);
            return;
        }
        setVisible(true);
        const timer = setTimeout(() => setVisible(false), duration);
        return () => clearTimeout(timer);
    }, [message, duration]);

    if (!message || !visible) return null;
    const { role, className } = STYLES[type];
    return (
        <div className={`mb-4 rounded-md border px-4 py-3 text-sm ${className}`} role={role}>
            {message}
        </div>
    );
};

export default StatusAlert;