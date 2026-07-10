import React, { useState } from "react";
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
} from "react-icons/fi";

const ResourceTable = ({
  activeTab,
  filteredItems,
  loading,
  onEditClick,
  onDeleteClick,
  onToggleCall,
  onEditMessageClick,
  onRegistrationClick,
  onQuestionsClick,
  onSocialsClick,
  onGalleryClick,
  onEpisodesClick,
}) => {
  const [imgErrors, setImgErrors] = useState({});

  const handleImgError = (id) => {
    setImgErrors((prev) => ({ ...prev, [`${activeTab}-${id}`]: true }));
  };

  const renderMedia = (item) => {
    const url = item.imageUrl || item.logoUrl;
    const hasError = imgErrors[`${activeTab}-${item.id}`] || !url;

    if (hasError) {
      let IconComponent = FiUser;
      let bgColor = "bg-slate-50 text-slate-400 border border-slate-200";

      if (activeTab === "committees") {
        IconComponent = FiLayers;
        bgColor = "bg-sky-50 text-sky-500 border border-sky-100";
      } else if (activeTab === "events") {
        IconComponent = FiCalendar;
        bgColor = "bg-orange-50 text-orange-500 border border-orange-100";
      } else if (activeTab === "clubs") {
        IconComponent = FiAward;
        bgColor = "bg-teal-50 text-teal-500 border border-teal-100";
      } else if (activeTab === "programs") {
        IconComponent = FiBookOpen;
        bgColor = "bg-indigo-50 text-indigo-500 border border-indigo-100";
      } else if (activeTab === "socialLinks") {
        IconComponent = FiShare2;
        bgColor = "bg-rose-50 text-rose-500 border border-rose-100";
      } else if (activeTab === "radio") {
        IconComponent = FiRadio;
        bgColor = "bg-purple-50 text-purple-500 border border-purple-100";
      }

      return (
        <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${bgColor} shrink-0`}>
          <IconComponent className="w-5 h-5" />
        </div>
      );
    }

    return (
      <img
        src={url}
        alt={item.name}
        onError={() => handleImgError(item.id)}
        className="w-10 h-10 rounded-xl object-cover border border-slate-200 shrink-0"
      />
    );
  };
  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="w-8 h-8 border-4 border-slate-200 border-t-[#4B98C8] rounded-full animate-spin" />
      </div>
    );
  }

  if (filteredItems.length === 0) {
    return (
      <div className="text-center py-20 border border-dashed border-slate-200 rounded-xl">
        <p className="text-slate-400 text-sm font-medium">No records found.</p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left border-collapse">
        <thead>
          <tr className="border-b border-slate-200 text-slate-400 text-[10px] font-bold uppercase tracking-widest">
            <th className="pb-3 pl-2">Details</th>
            {(activeTab === "highboard" || activeTab === "committeeBoard") && (
              <th className="pb-3">Role</th>
            )}
            {(activeTab === "events" || activeTab === "programs") && (
              <th className="pb-3">{activeTab === "events" ? "Time & Location" : "Duration & Schedule"}</th>
            )}
            {(activeTab === "committees" || activeTab === "programs") && (
              <th className="pb-3">{activeTab === "committees" ? "Call Status" : "Registration Status"}</th>
            )}
            {activeTab === "socialLinks" && (
              <th className="pb-3">URL</th>
            )}
            {(activeTab === "highboard" || activeTab === "committeeBoard") && <th className="pb-3 text-center">Order</th>}
            <th className="pb-3 pr-2 text-right">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 text-xs font-semibold text-slate-700">
          {filteredItems.map((item) => (
            <tr key={item.id} className="hover:bg-slate-50/55 transition-colors">
              {/* Details Column (Image + Title) */}
              <td className="py-3.5 pl-2">
                <div className="flex items-center gap-3.5">
                  {renderMedia(item)}
                  <div className="min-w-0 max-w-[200px] sm:max-w-[300px]">
                    <p className="font-extrabold text-slate-800 truncate">
                      {activeTab === "socialLinks" ? item.platform : activeTab === "radio" ? `Season ${item.seasonNumber}` : item.name}
                    </p>
                    {item.description && (
                      <p className="text-[10px] text-slate-400 truncate mt-0.5">{item.description}</p>
                    )}
                  </div>
                </div>
              </td>

              {/* Role Column */}
              {(activeTab === "highboard" || activeTab === "committeeBoard") && (
                <td className="py-3.5 text-slate-500 font-bold">{item.role || "N/A"}</td>
              )}

              {/* Event details column */}
              {activeTab === "events" && (
                <td className="py-3.5 text-slate-500">
                  <p className="font-bold">{item.location || "Online"}</p>
                  {item.eventTime ? (
                    <p className="text-[10px] text-slate-400 mt-0.5 font-bold">
                      {new Date(item.eventTime).toLocaleString()}
                    </p>
                  ) : (
                    <p className="text-[10px] text-slate-400 mt-0.5">No time set</p>
                  )}
                </td>
              )}

              {activeTab === "programs" && (
                <td className="py-3.5 text-slate-500">
                  <p className="font-bold">{item.time || "No schedule set"}</p>
                  {item.startDate && item.endDate ? (
                    <p className="text-[10px] text-slate-400 mt-0.5 font-bold">
                      {new Date(item.startDate).toLocaleDateString()} - {new Date(item.endDate).toLocaleDateString()}
                    </p>
                  ) : (
                    <p className="text-[10px] text-slate-400 mt-0.5">No dates set</p>
                  )}
                </td>
              )}

              {/* Committee Call Status or Program Registration Status column */}
              {(activeTab === "committees" || activeTab === "programs") && (
                <td className="py-3.5">
                  <div className="flex items-center gap-3">
                    <span
                      className={`px-2 py-1 rounded-full text-[10px] font-extrabold uppercase tracking-wide border ${
                        (activeTab === "committees" ? (item.open || item.isOpen) : item.registrationOpen)
                          ? "bg-emerald-50 text-emerald-600 border-emerald-200"
                          : "bg-slate-50 text-slate-500 border-slate-200"
                      }`}
                    >
                      {(activeTab === "committees" ? (item.open || item.isOpen) : item.registrationOpen) ? "Open" : "Closed"}
                    </span>
                    <button
                      onClick={() => onToggleCall(item)}
                      className={`text-[10px] font-extrabold uppercase tracking-wide px-2 py-1 rounded-lg border transition-all active:scale-95 ${
                        (activeTab === "committees" ? (item.open || item.isOpen) : item.registrationOpen)
                          ? "bg-red-50 text-red-600 border-red-200 hover:bg-red-100"
                          : "bg-sky-50 text-[#4B98C8] border-sky-200 hover:bg-sky-100"
                      }`}
                    >
                      {(activeTab === "committees" ? (item.open || item.isOpen) : item.registrationOpen) ? "Close Call" : "Open Call"}
                    </button>
                  </div>
                </td>
              )}

              {/* Sorting Order Column */}
              {(activeTab === "highboard" || activeTab === "committeeBoard") && (
                <td className="py-3.5 text-center text-slate-400 font-bold">{item.order ?? 99}</td>
              )}

              {activeTab === "socialLinks" && (
                <td className="py-3.5 text-slate-500 max-w-[200px] sm:max-w-[300px] truncate">
                  <a
                    href={item.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="hover:underline flex items-center gap-1 text-[#4B98C8]"
                  >
                    {item.url} <FiExternalLink className="w-3 h-3" />
                  </a>
                </td>
              )}

              {/* Action buttons */}
              <td className="py-3.5 pr-2 text-right">
                <div className="flex justify-end gap-2.5">
                  {activeTab === "committees" && (
                    <>
                      <button
                        onClick={() => onRegistrationClick(item)}
                        title="View Call History & Registrations"
                        className="p-2 bg-slate-50 hover:bg-emerald-50 text-slate-600 hover:text-emerald-600 rounded-lg transition-colors"
                      >
                        <FiUsers className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => onQuestionsClick(item)}
                        title="Manage Registration Questions"
                        className="p-2 bg-slate-50 hover:bg-sky-50 text-slate-600 hover:text-sky-600 rounded-lg transition-colors"
                      >
                        <FiHelpCircle className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => onEditMessageClick(item)}
                        title="Edit Call Email Message"
                        className="p-2 bg-slate-50 hover:bg-indigo-50 text-slate-600 hover:text-indigo-600 rounded-lg transition-colors"
                      >
                        <FiMail className="w-3.5 h-3.5" />
                      </button>
                    </>
                  )}
                  {(activeTab === "events" || activeTab === "clubs" || activeTab === "programs") && (
                    <button
                      onClick={() => onRegistrationClick(item)}
                      title="View Registration Panel"
                      className="p-2 bg-slate-50 hover:bg-emerald-50 text-slate-600 hover:text-emerald-600 rounded-lg transition-colors"
                    >
                      <FiUsers className="w-3.5 h-3.5" />
                    </button>
                  )}
                  {(activeTab === "events" || activeTab === "clubs" || activeTab === "programs") && (
                    <button
                      onClick={() => onQuestionsClick(item)}
                      title="Manage Registration Questions"
                      className="p-2 bg-slate-50 hover:bg-sky-50 text-slate-600 hover:text-sky-600 rounded-lg transition-colors"
                    >
                      <FiHelpCircle className="w-3.5 h-3.5" />
                    </button>
                  )}
                  {activeTab === "events" && (
                    <button
                      onClick={() => onGalleryClick(item)}
                      title="Manage Event Gallery"
                      className="p-2 bg-slate-50 hover:bg-purple-50 text-slate-600 hover:text-purple-600 rounded-lg transition-colors"
                    >
                      <FiImage className="w-3.5 h-3.5" />
                    </button>
                  )}
                  {activeTab === "clubs" && (
                    <button
                      onClick={() => onSocialsClick(item)}
                      title="Manage Club Social Links"
                      className="p-2 bg-slate-50 hover:bg-rose-50 text-slate-600 hover:text-rose-500 rounded-lg transition-colors"
                    >
                      <FiShare2 className="w-3.5 h-3.5" />
                    </button>
                  )}
                  {activeTab === "radio" && (
                    <button
                      onClick={() => onEpisodesClick(item)}
                      title="Manage Season Episodes"
                      className="p-2 bg-slate-50 hover:bg-purple-50 text-slate-600 hover:text-purple-600 rounded-lg transition-colors"
                    >
                      <FiVolume2 className="w-3.5 h-3.5" />
                    </button>
                  )}
                  <button
                    onClick={() => onEditClick(item)}
                    className="p-2 bg-slate-50 hover:bg-sky-50 text-slate-600 hover:text-sky-600 rounded-lg transition-colors"
                  >
                    <FiEdit2 className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => onDeleteClick(item)}
                    className="p-2 bg-slate-50 hover:bg-red-50 text-slate-600 hover:text-red-500 rounded-lg transition-colors"
                  >
                    <FiTrash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ResourceTable;
