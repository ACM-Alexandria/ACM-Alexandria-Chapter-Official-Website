import React, { useState } from "react";
import { FiMoreHorizontal } from "react-icons/fi";
import ActionSheet from "./ActionSheet";
import { iconButtonClass, iconButtonDangerClass } from "./adminStyles";

// Actions for one table row: icon buttons when the table is wide enough, otherwise an
// "Actions" button that opens a bottom sheet with every action. The table must sit in an
// `@container` element.
// actions: [{ key, label, icon, onClick, danger?, disabled?, hidden?, sheetOnly?, keepSpace? }]
// keepSpace: when hidden, leave an empty slot on desktop so icons line up between rows.
// sheetOnly actions (e.g. ones that already have a control in the row on desktop) only appear in the sheet.
// Icons appear once the table's own width allows (container query), the sheet button otherwise.
// Class names are written out in full so Tailwind can see them.
const BREAKPOINTS = {
  compact: { icons: "hidden @min-[560px]:flex", sheet: "@min-[560px]:hidden" },
  wide: { icons: "hidden @min-[1000px]:flex", sheet: "@min-[1000px]:hidden" },
};

const RowActions = ({ actions, name, subtitle, from = "wide" }) => {
  const bp = BREAKPOINTS[from] || BREAKPOINTS.wide;
  const [sheetOpen, setSheetOpen] = useState(false);
  const visible = actions.filter((a) => !a.hidden);
  if (visible.length === 0) return null;

  return (
    <>
      <div className={`${bp.icons} items-center justify-end gap-1.5`}>
        {actions.filter((a) => !a.sheetOnly && (!a.hidden || a.keepSpace)).map((action) => {
          if (action.hidden) return <span key={action.key} className="w-9 h-9" aria-hidden="true" />;
          const Icon = action.icon;
          return (
            <button
              key={action.key}
              type="button"
              onClick={(e) => { e.stopPropagation(); action.onClick(); }}
              disabled={action.disabled}
              className={action.danger ? iconButtonDangerClass : iconButtonClass}
              title={action.label}
              aria-label={name ? `${action.label}: ${name}` : action.label}
            >
              {Icon && <Icon className="w-4 h-4" aria-hidden="true" />}
            </button>
          );
        })}
      </div>

      <button
        type="button"
        onClick={(e) => { e.stopPropagation(); setSheetOpen(true); }}
        className={`${iconButtonClass} ${bp.sheet}`}
        aria-haspopup="dialog"
        aria-label={name ? `Actions for ${name}` : "Actions"}
      >
        <FiMoreHorizontal className="w-5 h-5" aria-hidden="true" />
      </button>

      <ActionSheet
        open={sheetOpen}
        onClose={() => setSheetOpen(false)}
        title={name}
        subtitle={subtitle}
        actions={visible}
      />
    </>
  );
};

export default RowActions;
