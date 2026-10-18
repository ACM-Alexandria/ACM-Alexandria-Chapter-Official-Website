import { FiEdit2, FiTrash2 } from "react-icons/fi";

const PastResourceTable = ({ resource, items, parents, onEdit, onDelete, busy }) => (
  items.length === 0 ? <p className="py-8 text-center text-slate-500 dark:text-slate-300">No records for this season.</p> :
    <div className="overflow-x-auto">
      <table className="w-full text-left text-sm">
        <thead className="text-slate-500 dark:text-slate-300 border-b border-slate-200 dark:border-slate-700">
          <tr><th className="p-3">Name</th>{resource.member && <><th className="p-3">Role</th><th className="p-3">Order</th></>}
            {resource.parent && <th className="p-3">{resource.parent === "clubs" ? "Club" : "Committee"}</th>}<th className="p-3 text-right">Actions</th></tr>
        </thead>
        <tbody className="divide-y divide-slate-100 dark:divide-slate-700">
          {items.map((item) => <tr key={item.id}>
            <td className="p-3 font-bold">{item.userName || item.name}</td>
            {resource.member && <><td className="p-3">{item.role}</td><td className="p-3">{item.order ?? "—"}</td></>}
            {resource.parent && <td className="p-3">{parents.find((parent) => parent.id === item[resource.relation])?.name || "—"}</td>}
            <td className="p-3"><div className="flex justify-end gap-3">
              <button disabled={busy} onClick={() => onEdit(item)} aria-label={`Edit ${item.userName || item.name}`} className="p-2 text-[#205E85] dark:text-[#4B98C8] disabled:opacity-50"><FiEdit2 /></button>
              <button disabled={busy} onClick={() => onDelete(item)} aria-label={`Delete ${item.userName || item.name}`} className="p-2 text-red-600 dark:text-red-400 disabled:opacity-50"><FiTrash2 /></button>
            </div></td>
          </tr>)}
        </tbody>
      </table>
    </div>
);

export default PastResourceTable;
