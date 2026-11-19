import { focusRing } from "../ui/uiStyles";

// The topic index: a sticky column beside the questions on large screens, a row of chips you can swipe on
// phones. Links are in-page anchors, which the site's smooth scrolling glides to below the navbar.
const FaqNav = ({ groups, activeId }) => (
  <nav aria-label="FAQ topics" className="-mx-5 overflow-x-auto px-5 pb-1 [scrollbar-width:none] sm:-mx-8 sm:px-8 [&::-webkit-scrollbar]:hidden lg:sticky lg:top-28 lg:mx-0 lg:self-start lg:overflow-visible lg:px-0 lg:pb-0">
    <ul className="flex w-max gap-2 lg:w-auto lg:flex-col lg:gap-1">
      {groups.map((group) => {
        const active = group.id === activeId;
        return (
          <li key={group.id}>
            <a
              href={`#${group.id}`}
              aria-current={active ? "true" : undefined}
              className={`flex items-center justify-between gap-4 whitespace-nowrap rounded-xl px-3.5 py-2 text-sm font-semibold transition-colors ${focusRing} ${active
                ? "bg-white text-ocean-676 ring-1 ring-ocean-106 dark:bg-white/10 dark:text-ocean-222 dark:ring-white/15"
                : "text-slate-600 ring-1 ring-transparent hover:bg-white hover:text-ocean-676 dark:text-ocean-239 dark:hover:bg-white/5 dark:hover:text-ocean-222"}`}
            >
              {group.label}
              <span className={`text-xs font-medium tabular-nums ${active ? "text-ocean-573 dark:text-ocean-316" : "text-slate-500 dark:text-ocean-239"}`}>{group.items.length}</span>
            </a>
          </li>
        );
      })}
    </ul>
  </nav>
);

export default FaqNav;
