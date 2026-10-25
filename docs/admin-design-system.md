# Admin Tools Design System

This document records the design rules used across the admin tools (Admin page and HR dashboard) and the
member profile pages. Any new admin screen, tab, dialog or table must follow it so the whole app stays
consistent. Source of truth for classes: `frontend/src/components/AdminPage/SharedComponents/adminStyles.js`.

---

## 1. Principles

- **One scale.** Sizes, radii, colors and spacing are the same everywhere. Do not invent per-screen values.
- **Calm, not decorated.** Brand blue (`#4B98C8`) is used for primary actions, focus and the active state.
  No gradients, no glass/blur effects, no uppercase tracked labels, no tiny 9–10px text, no hover lift.
- **Readable first.** Body text is `text-sm` (14px) minimum; helper text is `text-xs` (12px) and must
  still reach WCAG AA contrast (use `slate-600` / `slate-400` on light, `slate-400` / `slate-300` on dark).
- **Works on phones.** Every screen must be usable at 390px width with no sideways scrolling.
- **Dark mode is required.** Every color has a `dark:` pair. Test both themes.
- **Accessible.** Icon-only buttons need `aria-label` and `title`. Dialogs use `role="dialog"`,
  `aria-modal`, and are labelled by their title. Respect `prefers-reduced-motion` (`motion-reduce:`).

---

## 2. Color tokens

| Role | Light | Dark |
|---|---|---|
| Page background | `bg-[#F3F7FB]` (soft blue tint) | `dark:bg-[#0B1520]` |
| Panel / card | `bg-white` + `border-slate-200` | `dark:bg-slate-900` + `dark:border-slate-800` |
| Inset / subtle surface | `bg-slate-50` | `dark:bg-slate-950/40` |
| Table header row | `bg-[#EEF4F9]`, `text-xs font-semibold text-slate-600` | `dark:bg-slate-800/60`, `dark:text-slate-400` |
| Primary text | `text-slate-900` | `dark:text-slate-100` |
| Secondary text / labels | `text-slate-700` / `text-slate-600` | `dark:text-slate-300` / `dark:text-slate-400` |
| Brand (primary action) | `bg-[#2A7AB0]` hover `#23699A` (AA with white text) | same |
| Active tab / selected item | `bg-[#E2F0FA] text-[#1D6A9C]` | `dark:bg-[#16364C] dark:text-[#7DBBE0]` |
| Brand text / accent | `text-[#2C6F99]` | `dark:text-[#7DBBE0]` |
| Brand tint | `bg-[#4B98C8]/10`, border `#4B98C8/30` | same |
| Success | `emerald-50/200/700` | `dark:emerald-950/40`, `dark:emerald-300` |
| Danger | `rose-50/200/600–700` | `dark:rose-950/40`, `dark:rose-400` |
| Warning | `amber-50/200/800` | `dark:amber-950/40`, `dark:amber-300` |
| Points star | `text-yellow-400` | same |

Rules:
- Use the brand blue only for primary buttons, focus rings, active tabs, toggles that are on, and links.
- Use rose for destructive actions only (delete). Use emerald for "open/active" status tags.
- Never use raw hex colors other than `#4B98C8`, `#2C6F99`, `#7DBBE0`, `#16364C`, `#E8F2F9`.

---

## 3. Typography

| Use | Classes |
|---|---|
| Page title (h1) | `text-2xl sm:text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-100` |
| Section title (h2, toolbar) | `text-lg font-semibold tracking-tight` |
| Dialog title (h3) | `text-base font-semibold` |
| Panel title | `text-base font-semibold` |
| Row primary text | `text-sm font-medium` |
| Row secondary text | `text-xs text-slate-600 dark:text-slate-400` |
| Field label | `labelClass` = `text-sm font-medium text-slate-700 dark:text-slate-300` |
| Helper / hint | `hintClass` = `text-xs text-slate-600 dark:text-slate-400` |
| Big metric number | `text-2xl font-semibold tabular-nums` |

Rules:
- Font: **Inter**, scoped with the `admin-ui` class on the page `<main>` and on dialogs rendered outside it (the site navbar keeps Roboto).
- Font weights: inside `.admin-ui` every weight steps up one level (`font-normal` renders at 500, `font-medium` at 600, `font-semibold` at 700) so Inter's strokes read heavier. Write the normal Tailwind classes; the mapping is in `index.css`. Never `font-bold`/`font-extrabold`/`font-black` in admin UI.
- Numbers and dates use `tabular-nums`.
- No `uppercase`, `tracking-wider`, `tracking-widest` on labels or buttons.
- Long text: `truncate` in rows; `break-words` in detail panels and descriptions.

---

## 4. Spacing, radius, borders, shadows

- **Radius ("rounded squares"):** inputs, buttons, tabs, tags and icon tiles `rounded-lg` (8px); small cards
  `rounded-xl` (12px); panels, tables, cards and dialogs `rounded-2xl` (16px). Avatars, dots and switches stay `rounded-full`.
  Never pill-shaped buttons or tabs, never `rounded-3xl`.
- **Borders:** always 1px `border-slate-200` (light) / `dark:border-slate-800` (panels) or `dark:border-slate-700` (inputs).
- **Shadows:** only `shadow-xl` on dialogs and `shadow-lg` on dropdown menus. Panels have no shadow.
- **Page gutter:** `px-4 sm:px-6 md:px-12`, `max-w-[1600px] mx-auto`, top padding `pt-[106px]` to clear the fixed navbar.
- **Vertical rhythm:** page sections `gap-6`; toolbar `mb-5`; form field stacks `gap-5`; inside a field `gap-1.5`.
- **Padding:** panels `p-4 sm:p-6`; dialog body `px-5 py-5`; dialog header/footer `px-5 py-4`; table cells `px-4 sm:px-5 py-4`.

---

## 5. Components (use these, do not re-create them)

Shared file locations: `frontend/src/components/AdminPage/SharedComponents/`.

| Component | Purpose |
|---|---|
| `AdminPageHeader` | Back link, page title, optional description and action buttons. |
| `AdminTabs` | Segmented tab bar (`role="tablist"`). Full width on phones, shows `shortLabel` below `sm`. |
| `ManagementSidebar` (in `ManagementSection/`) | Section navigation: vertical list from `lg`, dropdown picker below. |
| `ResourceToolbar` | Section title + description, "Add new" primary button, search box, slot for extra selects. |
| `ResourceTable` (in `ManagementSection/`) | Table for resource sections. Phone rows show name + status only; tapping the name opens a detail panel. |
| `UserManagementTab` (in `ManagementSection/`) | Users table with avatar, role tags, points, row actions. |
| `RowActions` | Row actions: icon buttons when the table is wide, otherwise a three-dots button that opens `ActionSheet`. |
| `ActionSheet` | Bottom sheet listing row actions (mobile). |
| `ModalShell` | The one dialog shell: centered card from `sm`, bottom sheet on phones, sticky footer, Escape-to-close, scroll lock. |
| `ToggleRow` | Titled on/off switch row (`role="switch"`). |
| `StatusAlert` / `noticeClass` | Inline success / error / warning messages. |
| `MembersFilter` | Role / committee / club multi-select used by export and email. |
| `ThemedDialog` (+ `useThemedDialog`) | Promise-based confirm / notify popups. Sits above modals (`z-[80]`). |
| `useScrollLock` (hook) | Counted page-scroll lock on `<html>` and `<body>`; use in every overlay. |
| `MemberProfilePanel` / `MemberProfileView` | Side panel and content for a member profile (`components/UserProfile/`). |

### 5.1 Buttons (from `adminStyles.js`)
- `primaryButtonClass`: main action (Save, Add new, Send). One per dialog footer.
- `secondaryButtonClass`: Cancel, Close, Back, Refresh, Export-style secondary actions.
- `dangerButtonClass`: confirm delete only.
- `iconButtonClass` / `iconButtonDangerClass`: square 36px icon buttons in rows and lists (delete uses the danger variant).
- Every button: `h-9`, `text-sm font-medium`, visible focus ring, `disabled:opacity-60`.

### 5.2 Fields
- `fieldClass` for single-line inputs and selects (`h-10`), `textareaClass` for multi-line.
- Labels above the field (`labelClass`, `mb-1.5`). No floating labels, no placeholder-as-label.
- Required fields are marked by the native `required` attribute.
- Error state: border `rose-500` and ring `rose-500/25` on the field, message below in `text-xs text-rose-700`.

### 5.3 Tags and status
- Tags are tinted rounded squares with no border (`rounded-lg`). `tagBrandClass` for roles and committees; `tagSuccessClass` for "Open"; `tagMutedClass` for "Closed" and neutral info; `tagDangerClass` for "Required"; `tagWarnClass` for warnings.
- Tags never use uppercase text.

---

## 6. Action icons (unified meanings)

Use the same icon for the same meaning everywhere. Icon set: `react-icons/fi` (Feather). Size `w-4 h-4` in buttons.

| Meaning | Icon | Label |
|---|---|---|
| View profile | `FiUser` | View profile |
| Add points | `FiPlus` | Add points |
| Manage access (roles) | `FiShield` | Manage access |
| Edit | `FiEdit2` | Edit |
| Delete | `FiTrash2` (danger) | Delete |
| Registrations / applicants | `FiUsers` | Registration panel / Call history & registrations |
| Registration questions | `FiHelpCircle` | Registration questions |
| **Send announcement / send email now** | `FiSend` | Send announcement / Event/Club announcements |
| Edit message template | `FiMail` | Edit call email message |
| Gallery | `FiImage` | Event gallery |
| Social links | `FiShare2` | Club social links |
| Season episodes | `FiVolume2` | Season episodes |
| Open / close form (lock state) | `FiUnlock` (open) / `FiLock` (close) | Open form / Close form |
| More actions (mobile) | `FiMoreHorizontal` | (aria-label "Actions for <name>") |
| Close dialog | `FiX` | Close |
| Back | `FiArrowLeft` | Back / Back to list |
| Refresh | `FiRefreshCw` | Refresh |
| Export | `FiFileText` | Export |

Rule: never reuse an icon for a different meaning in the same screen (for example `FiSend` is only for sending).

---

## 7. Layout patterns

### 7.1 Admin page
1. `AdminPageHeader` (title "Dashboard", description "Administration"; actions on the right).
2. Optional alert (email lock) using `noticeClass.error` with `role="alert"`.
3. `AdminTabs`: Insights / Resources / Email.
4. Tab content inside a single column.

### 7.2 Resource Management
- Desktop (`lg+`): sidebar list (`w-60`) on the left, content panel on the right (`flex-1`, `min-w-0`).
- Phones and tablets: dropdown picker above the panel.
- Content panel: `rounded-2xl border p-4 sm:p-6 min-h-[500px]`.
- Order inside the panel: `ResourceToolbar` → error notice → table or list → pagination (paged sections only).

### 7.3 Tables
- Wrapper: `tableFrameClass` (includes `@container`, so layout depends on the table width).
- Framed table: wrap in `tableFrameClass` (one white card, 16px corners), `<table className={tableClass}>`, header
  `tableHeadRowClass` (tinted band), body rows `group` with `rowBorderClass` on every `<td>` (thin divider, tint on hover).
- Columns: Details (name + secondary text) first; then status / role / time as needed; actions last.
- **Mobile rule:** show only the name and the key status tag under it. Everything else moves into the detail panel
  opened by tapping the name. Hide extra columns below the container breakpoint.
- Breakpoints inside tables use **container queries** (`@min-[900px]`, `@min-[560px]`), not viewport `md:`/`sm:`,
  so the layout reacts to the table's own width (the Admin page is narrower than the window because of the sidebar).
- Table cells never scroll sideways. Long text is `truncate` with a `max-w` that drops at smaller container widths.
- Empty state: dashed border box, `text-sm font-medium` title, muted helper line.
- Loading state: skeleton rows (`animate-pulse`), not a spinner in the middle.

### 7.4 Row actions
- Wide container: icon buttons in a row, right-aligned, `gap-2`. Hidden actions keep their slot (`keepSpace`)
  so icons line up between rows.
- Narrow container / phones: one three-dots button (`iconButtonClass`, aria-label "Actions for <name>") opens the
  `ActionSheet`. Sheet rows are min-height `3.25rem` with the icon in a `w-9 h-9` tile.
- The sheet closes before the action runs, so the action's dialog appears on a clean screen.
- Danger actions (Delete) are last and rose-colored in both the row and the sheet.

### 7.5 Detail panel (phone rows and member profiles)
- Opens with the row name / a "View profile" action. Title = item name.
- Content: a `<dl>` with divider lines; each row `label` (muted) and `value` (medium). Empty values show "Not set".
- Uses `ModalShell` (`size="md"`) with a Close button in the footer.

### 7.6 Dialogs (all use `ModalShell`)
- Sizes: `sm` (confirmations), `md` (simple forms, calls), `lg` (questions, registration panel, gallery), `xl` (episodes,
  export, email preview).
- Structure: header (title, optional subtitle = item name, X button) → scrollable body (`px-5 py-5`) → sticky footer
  (`bg-slate-50`, right-aligned on desktop, stacked with primary on top on phones).
- Footer: Cancel/Close (secondary) on the left, primary on the right. Delete confirm uses `dangerButtonClass`.
- Forms: wrap in `ModalShell onSubmit={...}` so Enter submits; use native `required` validation.
- `busy` prop while saving disables Close and Escape.
- Escape closes only the top-most overlay (`data-modal-overlay` attribute marks overlays).
- Dialogs over dialogs: the newer overlay sits higher (`z-[70]` shells, `z-[80]` ThemedDialog, `z-[65]` action sheet).
- Dialogs never nest `position: fixed` inside a `<tr>` or other table element; render them as siblings.
- Hooks inside dialogs: keep them in an inner component that mounts only while open (`X = open ? <Inner/> : null`),
  so state resets on each open and no hook breaks the rules of hooks.
- Lock the page with `useScrollLock(true)` in every overlay (shells and panels already do this).

### 7.7 Phone behavior (≤ 640px)
- Tabs: full width, icon + short label.
- Section picker: full width dropdown.
- Toolbar: title, then full-width "Add new", then full-width search.
- Table rows: name + status tag only; tap for details; three-dots for actions.
- Dialogs: bottom sheets (`items-end`, `rounded-t-2xl`, slide-up), max height `92dvh`, respect safe-area inset.
- Two-column KPI grid on Insights (`grid-cols-2`); three or more columns from `md`.

### 7.8 Desktop behavior
- Sidebar list for sections; full icon row for actions (when the container is ≥ 900px; titles cap at `max-w-[14rem]`,
  widening to `max-w-xs` at 1200px).
- Dialogs centered, `sm:max-w-*` per size.

---

## 8. Motion

- Dialog open: `sheetUp` (phones) or `scaleIn` (desktop), 0.2–0.25s, `ease-out`.
- Overlay: `fadeIn` 0.2s.
- Panel slide-in: `slideRight` 0.25s.
- Chevrons and toggles: `transition-transform` / `transition-colors` 200ms.
- Every animation needs `motion-reduce:animate-none` or `motion-reduce:transition-none`.
- No bounce, no page-load choreography, no hover lift on cards.

---

## 9. Behavior rules

- Permissions decide visibility: hide buttons the user cannot use (own row for points, `canManageUser`, `isHRMode`,
  `canSeeHRFeedback`). Do not show a disabled version of a button for a permission the user doesn't have.
- Destructive actions always confirm (`ThemedDialog` tone `danger` or `DeleteConfirmModal`).
- Long-running actions show a busy state on their button ("Saving…", "Sending…") and disable repeats.
- Success feedback: a green `noticeClass.success` line that fades after ~4 seconds, or a ThemedDialog `success`.
  Errors stay until dismissed or replaced, and use `role="alert"`.
- Relative time for recent dates ("1mo ago") with the full date and time underneath; full dates in dialogs.
- Points: positive values `+N` in emerald, negative in rose; a single point is "1 point".
- Text copy: sentence case everywhere ("Add points", "Save changes", "No records found"). No all-caps, no trailing "...",
  use the ellipsis character "…" for loading text.
- Empty values: "Not set" in detail panels, "—" in table cells where a value is missing.

---

## 10. Accessibility checklist (run before committing)

- [ ] Every icon-only button has `aria-label` and `title`.
- [ ] Tabs use `role="tablist"` / `role="tab"` / `aria-selected`.
- [ ] Switches use `role="switch"` with `aria-checked`.
- [ ] Dialogs have `role="dialog"` (or `alertdialog`), `aria-modal`, and a label.
- [ ] Focus moves into a dialog on open and returns to the trigger on close.
- [ ] Escape closes the top-most overlay only.
- [ ] Text contrast meets AA in both themes; no text below `text-xs`.
- [ ] Touch targets are at least 36px (`h-9` / `w-9`).

---

## 11. Code rules

- Import style classes from `adminStyles.js`; do not copy long class strings into components.
- Keep one component per file; shared pieces go in `SharedComponents/`.
- Don't put non-component exports in component files (fast refresh lint). Put helpers in their own file.
- Lint must pass for changed files: `npx eslint <files>`. Build must pass: `npx vite build`.
- New overlays must use `ModalShell` (or `ActionSheet` / `ThemedDialog`) and `useScrollLock`.
- Table breakpoints use `@min-[…]` container queries inside a `@container` wrapper.
- No new colors, radii or font weights outside this document. If something is missing, add it to `adminStyles.js`
  first, then use it.

---

## 12. Known exceptions (leave as is unless asked)

- `EventGalleryModal` and `GalleryTab` have full-screen image lightboxes with their own dark styling; they are media
  viewers, not admin forms.
- `RichTextEditor` keeps its own toolbar styling.
- `FeedbackTab` and `HRFeedbackTab` keep their filter toolbars, restyled to the field and tag rules.

---

## 13. Checklist for a new admin screen

1. Use `AdminPageHeader` and `AdminTabs` (or `ResourceToolbar` inside a section).
2. Use `panelClass` for every card or table container; add `@container` if it contains a table.
3. Build the table with `tableHeadRowClass` and `rowBorderClass`; phone shows name + status only.
4. Put actions in `RowActions` with icons from section 6.
5. Put every dialog in `ModalShell` with the right size and a sticky footer.
6. Check: light + dark, 390px, 1440px, no sideways scroll, Escape works, focus returns.
7. Run `npx eslint` on changed files and `npx vite build`.
