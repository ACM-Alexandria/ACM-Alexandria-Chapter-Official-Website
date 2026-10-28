// Tab buttons for the profile page. `tabs` is [[id, label], ...]; the panel it controls has id `profile-panel-<id>`.
const ProfileTabs = ({ tabs, active, onChange }) => (
  <div role="tablist" aria-label="Profile sections" className="flex gap-1 border-b border-ocean-106 dark:border-white/10">
    {tabs.map(([id, label]) => (
      <button
        key={id}
        type="button"
        role="tab"
        id={`profile-tab-${id}`}
        aria-selected={active === id}
        aria-controls={`profile-panel-${id}`}
        onClick={() => onChange(id)}
        className={`-mb-px h-12 border-b-2 px-4 text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ocean-461 ${active === id ? "border-ocean-676 text-ocean-676 dark:border-ocean-316 dark:text-ocean-316" : "border-transparent text-slate-600 hover:text-slate-900 dark:text-ocean-239 dark:hover:text-white"}`}
      >
        {label}
      </button>
    ))}
  </div>
);

export default ProfileTabs;
