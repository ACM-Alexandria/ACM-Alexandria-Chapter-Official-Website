// Tab buttons for the profile page. `tabs` is [[id, label], ...]; the panel it controls has id `profile-panel-<id>`.
const ProfileTabs = ({ tabs, active, onChange }) => (
  <div role="tablist" aria-label="Profile sections" className="flex gap-1 border-b border-[#D6E6F2] dark:border-white/10">
    {tabs.map(([id, label]) => (
      <button
        key={id}
        type="button"
        role="tab"
        id={`profile-tab-${id}`}
        aria-selected={active === id}
        aria-controls={`profile-panel-${id}`}
        onClick={() => onChange(id)}
        className={`-mb-px h-12 border-b-2 px-4 text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8] ${active === id ? "border-[#205E85] text-[#205E85] dark:border-[#7DBBE0] dark:text-[#7DBBE0]" : "border-transparent text-slate-600 hover:text-slate-900 dark:text-[#A9C7DB] dark:hover:text-white"}`}
      >
        {label}
      </button>
    ))}
  </div>
);

export default ProfileTabs;
