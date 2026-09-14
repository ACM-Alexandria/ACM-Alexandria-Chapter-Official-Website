import { HiMoon, HiSun } from "react-icons/hi2";
import "./ThemeToggleButton.css";

export const ThemeToggleButton = ({ isDark, onToggle }) => {
  return (
    <button
      onClick={onToggle}
      aria-label="Toggle dark mode"
      className="
        relative flex items-center justify-center
        w-10 h-10 rounded-full overflow-hidden
        border border-slate-800 dark:border-slate-100
        opacity-60 hover:opacity-100 active:scale-95
        transition-all duration-200 ease-in-out
      "
    >
      <HiSun
        aria-hidden="true"
        className={`
          icon-swap text-slate-800 dark:text-slate-100
          ${!isDark ? "icon-swap--active" : "icon-swap--light-hidden"}
        `}
      />

      <HiMoon
        aria-hidden="true"
        className={`
          icon-swap text-slate-800 dark:text-slate-100
          ${isDark ? "icon-swap--active" : "icon-swap--dark-hidden"}
        `}
      />
    </button>
  );
}