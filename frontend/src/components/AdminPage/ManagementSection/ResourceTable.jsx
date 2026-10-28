import React, { createElement, useState } from "react";
import {
  FiEdit2,
  FiTrash2,
  FiExternalLink,
  FiMail,
  FiUser,
  FiUsers,
  FiLayers,
  FiCalendar,
  FiAward,
  FiBookOpen,
  FiShare2,
  FiHelpCircle,
  FiImage,
  FiRadio,
  FiVolume2,
  FiFileText,
  FiGlobe,
  FiSend,
  FiLock,
  FiUnlock,
  FiMessageSquare,
} from "react-icons/fi";
import { useUserProfile } from "../../../hooks/useUserProfile";
import RowActions from "../SharedComponents/RowActions";
import ModalShell from "../SharedComponents/ModalShell";
import { secondaryButtonClass } from "../SharedComponents/adminStyles";
import {
  panelClass,
  tableClass,
  tableFrameClass,
  rowBorderClass,
  tableHeadRowClass,
  tagMutedClass,
  tagSuccessClass,
} from "../SharedComponents/adminStyles";

const TAB_ICONS = {
  committees: FiLayers,
  events: FiCalendar,
  clubs: FiAward,
  programs: FiBookOpen,
  socialLinks: FiShare2,
  radio: FiRadio,
  exclusiveForms: FiFileText,
  partners: FiGlobe,
};

const BOARD_TABS = ["highboard", "committeeBoard"];
const STATUS_TABS = ["committees", "programs", "exclusiveForms", "clubs", "events"];
const REGISTRATION_TABS = ["events", "clubs", "programs", "exclusiveForms"];

// Whether a call / registration / form is currently open for this item
const isOpenFor = (tab, item) => {
  if (tab === "committees") return Boolean(item.open || item.isOpen);
  if (tab === "exclusiveForms") return Boolean(item.isActive);
  return Boolean(item.registrationOpen || item.open || item.isOpen);
};

const getItemName = (tab, item) => {
  if (tab === "socialLinks") return item.platform;
  if (tab === "radio") return `Season ${item.seasonNumber}`;
  if (tab === "exclusiveForms") return item.title;
  return item.name;
};

const mediaBase = "w-10 h-10 shrink-0 object-cover border border-slate-200 dark:border-slate-700";

const MediaIcon = ({ icon, round = false }) => (
  <div
    className={`w-10 h-10 shrink-0 flex items-center justify-center bg-[#E8F2F9] dark:bg-[#16364C] text-[#2C6F99] dark:text-[#7DBBE0] ${round ? "rounded-full" : "rounded-lg"}`}
    aria-hidden="true"
  >
    {createElement(icon, { className: "w-5 h-5" })}
  </div>
);

const StatusTag = ({ open }) => <span className={open ? tagSuccessClass : tagMutedClass}>{open ? "Open" : "Closed"}</span>;

// One table row. A component of its own so board rows can load the member's profile.
const ResourceRow = ({
  activeTab,
  item,
  loading,
  isHRMode,
  announcingId,
  onEditClick,
  onDeleteClick,
  onToggleCall,
  onEditMessageClick,
  onRegistrationClick,
  onQuestionsClick,
  onSocialsClick,
  onGalleryClick,
  onFeedbackClick,
  onEpisodesClick,
  onAnnounceClick,
  onEmailsClick,
}) => {
  const isBoard = BOARD_TABS.includes(activeTab);
  const { profile, loading: profileLoading } = useUserProfile(isBoard ? item.userId : undefined);
  const [imgError, setImgError] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);

  const name = isBoard
    ? profile?.name || (profileLoading ? "Loading…" : "Unknown user")
    : getItemName(activeTab, item);
  const imageUrl = isBoard ? profile?.profile_image_url : item.imageUrl || item.logoUrl;
  const subtitle = isBoard ? profile?.email : item.description;
  const hasStatus = STATUS_TABS.includes(activeTab);
  const open = hasStatus && isOpenFor(activeTab, item);

  // Details shown in their own column from md up, and under the name on phones
  let detailPrimary = null;
  let detailSecondary = null;
  if (activeTab === "events") {
    detailPrimary = item.location || "Online";
    detailSecondary = item.eventTime ? new Date(item.eventTime).toLocaleString() : "No time set";
  } else if (activeTab === "programs") {
    detailPrimary = item.time || "No schedule set";
    detailSecondary =
      item.startDate && item.endDate
        ? `${new Date(item.startDate).toLocaleDateString()} - ${new Date(item.endDate).toLocaleDateString()}`
        : "No dates set";
  } else if (isBoard) {
    detailPrimary = item.role || "N/A";
  }

  const linkUrl = activeTab === "socialLinks" ? item.url : activeTab === "partners" ? item.website : null;

  const showLinkColumn = activeTab === "socialLinks" || activeTab === "partners";
  // Everything about the item, shown in the detail panel (phones keep only the key line)
  const detailRows = [
    isBoard && { label: "Role", value: item.role },
    isBoard && { label: "Order", value: item.order ?? 99 },
    isBoard && { label: "Email", value: profile?.email },
    activeTab === "events" && { label: "Location", value: item.location || "Online" },
    (activeTab === "events" || activeTab === "programs") && { label: activeTab === "events" ? "Time" : "Schedule", value: detailSecondary || detailPrimary },
    activeTab === "programs" && { label: "Duration", value: item.time },
    hasStatus && { label: activeTab === "committees" ? "Call status" : activeTab === "exclusiveForms" ? "Status" : "Registration", value: open ? "Open" : "Closed" },
    showLinkColumn && { label: "Link", value: linkUrl },
    item.description && { label: "Description", value: item.description },
  ].filter(Boolean);

  // Every action for this row (all icons on desktop, one sheet on phones)
  const actions = [
    // Committee call tools stay available in HR mode
    { key: "call-registrations", label: "Call history & registrations", icon: FiUsers, onClick: () => onRegistrationClick(item), hidden: activeTab !== "committees" },
    { key: "call-questions", label: "Registration questions", icon: FiHelpCircle, onClick: () => onQuestionsClick(item), hidden: activeTab !== "committees" },
    { key: "call-message", label: "Edit call email message", icon: FiMail, onClick: () => onEditMessageClick(item), hidden: activeTab !== "committees" },
    {
      key: "call-announce",
      label: "Resend call email to subscribers",
      icon: FiSend,
      onClick: () => onAnnounceClick(item),
      disabled: announcingId === item.id,
      hidden: activeTab !== "committees" || !open,
    },
    { key: "registrations", label: "Registration panel", icon: FiUsers, onClick: () => onRegistrationClick(item), hidden: !REGISTRATION_TABS.includes(activeTab) },
    { key: "questions", label: "Registration questions", icon: FiHelpCircle, onClick: () => onQuestionsClick(item), hidden: !REGISTRATION_TABS.includes(activeTab) },
    {
      key: "emails",
      label: activeTab === "events" ? "Event announcements" : "Club announcements",
      icon: FiSend,
      onClick: () => onEmailsClick(item),
      hidden: activeTab !== "events" && activeTab !== "clubs",
    },
    // The rest needs edit rights
    {
      key: "toggle",
      label: open ? "Close form" : "Open form",
      icon: open ? FiLock : FiUnlock,
      onClick: () => onToggleCall(item),
      disabled: loading,
      sheetOnly: true,
      hidden: isHRMode || !hasStatus,
    },
    {
      key: "announce",
      label: "Send announcement to subscribers",
      icon: FiSend,
      onClick: () => onAnnounceClick(item),
      disabled: announcingId === item.id,
      hidden: isHRMode || activeTab !== "programs",
    },
    { key: "gallery", label: "Event gallery", icon: FiImage, onClick: () => onGalleryClick(item), hidden: isHRMode || activeTab !== "events" },
    { key: "feedback", label: "Event feedback", icon: FiMessageSquare, onClick: () => onFeedbackClick(item), hidden: activeTab !== "events" },
    { key: "socials", label: "Club social links", icon: FiShare2, onClick: () => onSocialsClick(item), hidden: isHRMode || activeTab !== "clubs" },
    { key: "episodes", label: "Season episodes", icon: FiVolume2, onClick: () => onEpisodesClick(item), hidden: isHRMode || activeTab !== "radio" },
    { key: "edit", label: "Edit", icon: FiEdit2, onClick: () => onEditClick(item), hidden: isHRMode },
    { key: "delete", label: "Delete", icon: FiTrash2, onClick: () => onDeleteClick(item), disabled: loading, danger: true, hidden: isHRMode },
  ];

  const fallbackIcon = TAB_ICONS[activeTab] || FiUser;

  return (
    <>
    <tr className="group">
      {/* Details */}
      <td className={`px-4 sm:px-5 py-4 w-full max-w-0 @min-[900px]:w-auto @min-[900px]:max-w-none ${rowBorderClass}`}>
        <div className="flex items-center gap-3 min-w-0">
          {imageUrl && !imgError ? (
            <img
              src={imageUrl}
              alt=""
              onError={() => setImgError(true)}
              className={`${mediaBase} ${isBoard ? "rounded-full" : "rounded-lg"}`}
            />
          ) : (
            <MediaIcon icon={isBoard ? FiUser : fallbackIcon} round={isBoard} />
          )}
          <div className="min-w-0">
            <button
              type="button"
              onClick={() => setDetailOpen(true)}
              className="block max-w-full text-left text-sm font-medium text-slate-900 dark:text-slate-100 truncate rounded hover:underline underline-offset-2 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/40 @min-[900px]:max-w-[14rem] @min-[1200px]:max-w-xs"
              title={name}
            >
              {name}
            </button>
            {subtitle && (
              <p className="text-xs text-slate-600 dark:text-slate-400 truncate @min-[900px]:max-w-[14rem] @min-[1200px]:max-w-xs">{subtitle}</p>
            )}
            {/* Phones: the hidden columns move under the name */}
            <div className="@min-[900px]:hidden mt-1.5 flex flex-wrap items-center gap-x-2 gap-y-1">
              {hasStatus && <StatusTag open={open} />}
            </div>
          </div>
        </div>
      </td>

      {/* Role / time & location / schedule */}
      {(activeTab === "events" || activeTab === "programs" || isBoard) && (
        <td className={`hidden @min-[900px]:table-cell px-5 py-4 ${rowBorderClass}`}>
          <p className="text-sm text-slate-900 dark:text-slate-100">{detailPrimary}</p>
          {detailSecondary && <p className="mt-0.5 text-xs text-slate-600 dark:text-slate-400 tabular-nums">{detailSecondary}</p>}
        </td>
      )}

      {/* Status (call / registration / form) */}
      {hasStatus && (
        <td className={`hidden @min-[900px]:table-cell px-5 py-4 ${rowBorderClass}`}>
          <div className="flex items-center gap-2.5">
            <StatusTag open={open} />
            {!isHRMode && (
              <button
                type="button"
                onClick={() => onToggleCall(item)}
                disabled={loading}
                className={`h-7 px-2.5 shrink-0 whitespace-nowrap rounded-lg border text-xs font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed ${open
                  ? "border-rose-200 dark:border-rose-900 text-rose-700 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/30"
                  : "border-[#4B98C8]/40 text-[#2C6F99] dark:text-[#7DBBE0] hover:bg-[#4B98C8]/10"}`}
              >
                {open ? "Close form" : "Open form"}
              </button>
            )}
          </div>
        </td>
      )}

      {/* URL / website */}
      {showLinkColumn && (
        <td className={`hidden @min-[900px]:table-cell px-5 py-4 max-w-xs ${rowBorderClass}`}>
          {linkUrl ? (
            <a
              href={linkUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex max-w-full items-center gap-1 text-sm text-[#2C6F99] dark:text-[#7DBBE0] hover:underline underline-offset-2"
            >
              <span className="truncate">{linkUrl}</span>
              <FiExternalLink className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
            </a>
          ) : (
            <span className="text-sm text-slate-500 dark:text-slate-400">No website</span>
          )}
        </td>
      )}

      {/* Sorting order */}
      {isBoard && (
        <td className={`hidden @min-[900px]:table-cell px-5 py-4 text-center text-sm text-slate-700 dark:text-slate-300 tabular-nums ${rowBorderClass}`}>
          {item.order ?? 99}
        </td>
      )}

      {/* Actions */}
      <td className={`px-3 sm:px-5 py-4 text-right whitespace-nowrap ${rowBorderClass}`}>
        <RowActions actions={actions} name={name} />
      </td>
    </tr>
            <ModalShell
          open={detailOpen}
          onClose={() => setDetailOpen(false)}
          title={name}
          size="md"
          footer={
            <button type="button" onClick={() => setDetailOpen(false)} className={secondaryButtonClass}>
              Close
            </button>
          }
        >
          <dl className="divide-y divide-slate-200 dark:divide-slate-800">
            {detailRows.map((row) => (
              <div key={row.label} className="py-3 grid grid-cols-1 sm:grid-cols-[9rem_1fr] gap-1 sm:gap-4">
                <dt className="text-sm text-slate-600 dark:text-slate-400">{row.label}</dt>
                <dd className="text-sm font-medium text-slate-900 dark:text-slate-100 break-words">{row.value || "Not set"}</dd>
              </div>
            ))}
          </dl>
        </ModalShell>    </>
  );
};


const ResourceTable = ({ activeTab, filteredItems, loading, isHRMode = false, ...handlers }) => {
  const hasStatus = STATUS_TABS.includes(activeTab);
  const isBoard = BOARD_TABS.includes(activeTab);
  const detailHeader = activeTab === "events" ? "Time & location" : activeTab === "programs" ? "Duration & schedule" : isBoard ? "Role" : null;
  const statusHeader = activeTab === "committees" ? "Call status" : activeTab === "exclusiveForms" ? "Status" : "Registration status";

  if (loading) {
    return (
      <div className={`${panelClass} overflow-hidden`} aria-busy="true" aria-label="Loading">
        {[0, 1, 2, 3, 4].map((i) => (
          <div key={i} className="px-5 py-4 flex items-center gap-3 border-b border-slate-200 dark:border-slate-800 last:border-b-0 animate-pulse motion-reduce:animate-none">
            <div className="w-10 h-10 rounded-lg bg-slate-200 dark:bg-slate-800" />
            <div className="space-y-2">
              <div className="h-3.5 w-40 rounded bg-slate-200 dark:bg-slate-800" />
              <div className="h-3 w-56 max-w-full rounded bg-slate-200 dark:bg-slate-800" />
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (filteredItems.length === 0) {
    return (
      <div className="py-14 text-center rounded-2xl border border-dashed border-slate-300 dark:border-slate-700">
        <p className="text-sm font-medium text-slate-900 dark:text-slate-100">No records found</p>
        <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">Try a different search, or add a new one.</p>
      </div>
    );
  }

  return (
    <div className={tableFrameClass}>
      <div className="overflow-x-auto">
        <table className={tableClass}>
          <thead>
            <tr className={tableHeadRowClass}>
              <th scope="col" className="px-4 sm:px-5 py-3 font-medium">Details</th>
              {detailHeader && <th scope="col" className="hidden @min-[900px]:table-cell px-5 py-3 font-medium">{detailHeader}</th>}
              {hasStatus && <th scope="col" className="hidden @min-[900px]:table-cell px-5 py-3 font-medium">{statusHeader}</th>}
              {activeTab === "socialLinks" && <th scope="col" className="hidden @min-[900px]:table-cell px-5 py-3 font-medium">URL</th>}
              {activeTab === "partners" && <th scope="col" className="hidden @min-[900px]:table-cell px-5 py-3 font-medium">Website</th>}
              {isBoard && <th scope="col" className="hidden @min-[900px]:table-cell px-5 py-3 font-medium text-center">Order</th>}
              <th scope="col" className="px-3 sm:px-5 py-3"><span className="sr-only">Actions</span></th>
            </tr>
          </thead>
          <tbody>
            {filteredItems.map((item) => (
              <ResourceRow
                key={item.id}
                activeTab={activeTab}
                item={item}
                loading={loading}
                isHRMode={isHRMode}
                {...handlers}
              />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ResourceTable;
