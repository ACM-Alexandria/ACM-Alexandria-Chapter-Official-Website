import React from "react";

const HRFolderIcon = ({ className = "w-5 h-5", ...props }) => {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
      {...props}
    >
      {/* 1. Back folder tab */}
      <path d="M22 10V6a2 2 0 0 0-2-2H10l-2-2H4a2 2 0 0 0-2 2v5" />

      {/* 2. Front folder outline (Mathematically broken to leave a perfect gap for the ID Card) */}
      <path d="M12 22h8a2 2 0 0 0 2-2V10a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v1" />

      {/* 3. ID Card Outline */}
      <rect x="2" y="11" width="10" height="11" rx="2" />
      
      {/* 4. User Silhouette inside the ID Card */}
      <circle cx="7" cy="15" r="1.5" />
      <path d="M4.5 22v-1.5a2.5 2.5 0 0 1 5 0V22" />

      {/* 5. Paperclip sliding over the top of the ID card */}
      <path d="M5 11V8.5a1.5 1.5 0 0 1 3 0V12" />

      {/* 6. Decorative data lines on the right side of the folder */}
      <line x1="15" y1="14" x2="19" y2="14" />
      <line x1="15" y1="18" x2="19" y2="18" />
    </svg>
  );
};

export default HRFolderIcon;