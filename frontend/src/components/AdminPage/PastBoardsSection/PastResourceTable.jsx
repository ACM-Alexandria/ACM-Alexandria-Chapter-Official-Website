import { useState } from "react";
import { FiEdit2, FiTrash2 } from "react-icons/fi";
import RowActions from "../SharedComponents/RowActions";
import ModalShell from "../SharedComponents/ModalShell";
import {
  hintClass,
  rowBorderClass,
  secondaryButtonClass,
  tableClass,
  tableFrameClass,
  tableHeadRowClass,
} from "../SharedComponents/adminStyles";

// Records of one past-board section. Columns collapse with the table's own width (container queries);
// on phones the name opens a detail dialog with every field.
const PastResourceTable = ({ resource, items, parents, onEdit, onDelete, busy }) => {
  const [detail, setDetail] = useState(null);

  if (items.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-slate-300 px-4 py-10 text-center dark:border-[#28405A]">
        <p className="text-sm font-medium text-slate-800 dark:text-slate-200">No records for this season</p>
        <p className={`mt-1 ${hintClass}`}>Use the add button above to create the first one.</p>
      </div>
    );
  }

  const nameOf = (item) => item.userName || item.name;
  const parentLabel = resource.parent === "clubs" ? "Club" : "Committee";
  const parentNameOf = (item) => parents.find((parent) => parent.id === item[resource.relation])?.name || "Not set";
  const subtitleOf = (item) => (resource.member ? item.role : resource.parent ? parentNameOf(item) : "");

  const detailRows = (item) => {
    if (!resource.member) return [["Description", item.description], ["Logo", item.logoUrl]];
    const rows = [["Role", item.role], ["Display order", item.order], ["Photo", item.profileImageUrl], ["LinkedIn", item.linkedinUrl]];
    if (resource.parent) rows.push([parentLabel, parentNameOf(item)]);
    return rows;
  };

  return (
    <>
      <div className={tableFrameClass}>
        <table className={tableClass}>
          <thead>
            <tr className={tableHeadRowClass}>
              <th scope="col" className="px-4 py-3 text-left font-semibold sm:px-5">Name</th>
              {resource.member && <th scope="col" className="hidden px-4 py-3 text-left font-semibold @min-[560px]:table-cell sm:px-5">Role</th>}
              {resource.member && <th scope="col" className="hidden px-4 py-3 text-left font-semibold @min-[900px]:table-cell sm:px-5">Order</th>}
              {resource.parent && <th scope="col" className="hidden px-4 py-3 text-left font-semibold @min-[560px]:table-cell sm:px-5">{parentLabel}</th>}
              <th scope="col" className="w-px px-4 py-3 sm:px-5"><span className="sr-only">Actions</span></th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className="group">
                <td className={`min-w-0 px-4 py-3 sm:px-5 ${rowBorderClass}`}>
                  <button
                    type="button"
                    onClick={() => setDetail(item)}
                    className="block max-w-full truncate text-left text-sm font-medium text-slate-900 hover:text-[#2C6F99] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/40 dark:text-slate-100 dark:hover:text-[#7DBBE0]"
                  >
                    {nameOf(item)}
                  </button>
                  {subtitleOf(item) && <p className={`mt-0.5 truncate @min-[560px]:hidden ${hintClass}`}>{subtitleOf(item)}</p>}
                </td>
                {resource.member && <td className={`hidden px-4 py-3 text-sm text-slate-700 @min-[560px]:table-cell sm:px-5 dark:text-slate-300 ${rowBorderClass}`}>{item.role}</td>}
                {resource.member && <td className={`hidden px-4 py-3 text-sm tabular-nums text-slate-700 @min-[900px]:table-cell sm:px-5 dark:text-slate-300 ${rowBorderClass}`}>{item.order ?? "—"}</td>}
                {resource.parent && <td className={`hidden max-w-[14rem] truncate px-4 py-3 text-sm text-slate-700 @min-[560px]:table-cell sm:px-5 dark:text-slate-300 ${rowBorderClass}`}>{parentNameOf(item)}</td>}
                <td className={`px-4 py-3 sm:px-5 ${rowBorderClass}`}>
                  <RowActions
                    name={nameOf(item)}
                    actions={[
                      { key: "edit", label: "Edit", icon: FiEdit2, onClick: () => onEdit(item), disabled: busy },
                      { key: "delete", label: "Delete", icon: FiTrash2, danger: true, onClick: () => onDelete(item), disabled: busy },
                    ]}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {detail && (
        <ModalShell
          open
          size="sm"
          onClose={() => setDetail(null)}
          title={nameOf(detail)}
          subtitle={resource.label}
          footer={<button type="button" onClick={() => setDetail(null)} className={secondaryButtonClass}>Close</button>}
        >
          <dl className="divide-y divide-slate-200 dark:divide-[#1E3248]">
            {detailRows(detail).map(([label, value]) => (
              <div key={label} className="flex items-start justify-between gap-4 py-3">
                <dt className={hintClass}>{label}</dt>
                <dd className="break-words text-right text-sm font-medium text-slate-900 dark:text-slate-100">{value || "Not set"}</dd>
              </div>
            ))}
          </dl>
        </ModalShell>
      )}
    </>
  );
};

export default PastResourceTable;
