import { useEffect, useState } from "react";
import { FiArrowDown, FiArrowUp, FiEdit2, FiHelpCircle, FiPlus, FiTrash2, FiX } from "react-icons/fi";
import { createFAQ, deleteFAQ, fetchFAQs, reorderFAQs, updateFAQ } from "../../../services/faqService";
import { fieldClass, primaryButtonClass, secondaryButtonClass, textareaClass } from "../SharedComponents/adminStyles";

const CATEGORIES = ["Joining", "Events", "Committees", "Club", "Radio", "Accounts"];
const emptyForm = { question: "", answer: "", category: "Joining", order: 0 };

const FAQManagementTab = () => {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editing, setEditing] = useState(null);
  const [formOpen, setFormOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const load = async () => {
    setLoading(true);
    try {
      const result = await fetchFAQs({ size: 100 });
      setItems(result.content || []);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const save = async (event) => {
    event.preventDefault();
    setSaving(true); setError("");
    try {
      const payload = { ...form, order: Number(form.order) };
      if (editing) await updateFAQ(editing.id, payload);
      else await createFAQ(payload);
      setEditing(null); setForm(emptyForm); setFormOpen(false);
      await load();
    } catch (saveError) {
      setError(saveError.message);
    } finally { setSaving(false); }
  };

  const move = async (index, direction) => {
    const nextIndex = index + direction;
    if (nextIndex < 0 || nextIndex >= items.length || items[nextIndex].category !== items[index].category) return;
    const reordered = [...items];
    [reordered[index], reordered[nextIndex]] = [reordered[nextIndex], reordered[index]];
    const normalized = reordered.map((item, order) => ({ ...item, order }));
    setItems(normalized);
    try {
      await reorderFAQs(normalized.map(({ id, order }) => ({ id, order })));
    } catch (reorderError) {
      setError(reorderError.message);
      await load();
    }
  };

  const remove = async (item) => {
    if (!window.confirm(`Delete “${item.question}”? This cannot be undone.`)) return;
    setError("");
    try { await deleteFAQ(item.id); await load(); }
    catch (deleteError) { setError(deleteError.message); }
  };

  return <div className="space-y-6">
    <div className="flex flex-col justify-between gap-4 border-b border-slate-200 pb-5 dark:border-slate-800 sm:flex-row sm:items-start">
      <div><h2 className="text-xl font-bold text-slate-900 dark:text-white">Manage FAQs</h2><p className="mt-1 text-sm text-slate-600 dark:text-slate-400">Create, edit, delete, and order the questions shown on the public FAQ page.</p></div>
      <button type="button" onClick={() => { setEditing(null); setForm({ ...emptyForm, order: items.filter((item) => item.category === "Joining").length }); setFormOpen(true); }} className={primaryButtonClass}><FiPlus /> Add FAQ</button>
    </div>
    {error && <div role="alert" className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-900 dark:bg-rose-950/30 dark:text-rose-300">{error}</div>}
    {formOpen && <form onSubmit={save} className="space-y-4 rounded-2xl border border-[#4B98C8]/35 bg-[#4B98C8]/5 p-5 dark:bg-[#16364C]/30">
      <div className="flex items-center justify-between"><h3 className="font-bold">{editing ? "Edit FAQ" : "Add FAQ"}</h3><button type="button" onClick={() => setFormOpen(false)} className="rounded-lg p-1.5 text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-800" aria-label="Close form"><FiX /></button></div>
      <label className="block text-sm font-medium">Question<input required value={form.question} onChange={(e) => setForm({ ...form, question: e.target.value })} className={`mt-1.5 ${fieldClass}`} /></label>
      <label className="block text-sm font-medium">Answer<textarea required rows="4" value={form.answer} onChange={(e) => setForm({ ...form, answer: e.target.value })} className={`mt-1.5 ${textareaClass} resize-y`} /></label>
      <div className="grid gap-4 sm:grid-cols-2"><label className="text-sm font-medium">Category<select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} className={`mt-1.5 ${fieldClass}`}>{CATEGORIES.map((category) => <option key={category}>{category}</option>)}</select></label><label className="text-sm font-medium">Order<input type="number" min="0" value={form.order} onChange={(e) => setForm({ ...form, order: e.target.value })} className={`mt-1.5 ${fieldClass}`} /></label></div>
      <div className="flex justify-end gap-3"><button type="button" onClick={() => setFormOpen(false)} className={secondaryButtonClass}>Cancel</button><button disabled={saving} className={primaryButtonClass}>{saving ? "Saving…" : "Save FAQ"}</button></div>
    </form>}
    {loading ? <p className="py-10 text-center text-sm text-slate-500">Loading FAQs…</p> : items.length === 0 ? <div className="rounded-2xl border border-dashed border-slate-300 p-10 text-center text-sm text-slate-500 dark:border-slate-700">No FAQ entries yet. Add the first one to populate the public page.</div> : <div className="overflow-hidden rounded-2xl border border-slate-200 dark:border-slate-800">{items.map((item, index) => <div key={item.id} className="flex items-center gap-3 border-b border-slate-200 p-4 last:border-0 dark:border-slate-800"><div className="hidden h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-[#E8F2F9] text-[#205E85] sm:flex"><FiHelpCircle /></div><div className="min-w-0 flex-1"><p className="truncate font-semibold text-slate-900 dark:text-white">{item.question}</p><p className="mt-1 text-xs font-semibold uppercase tracking-wide text-[#2C6F99] dark:text-[#7DBBE0]">{item.category} · #{item.order}</p></div><div className="flex shrink-0 items-center gap-1"><button type="button" aria-label="Move up" disabled={index === 0 || items[index - 1].category !== item.category} onClick={() => move(index, -1)} className="rounded-lg p-2 text-slate-500 hover:bg-slate-100 disabled:opacity-30 dark:hover:bg-slate-800"><FiArrowUp /></button><button type="button" aria-label="Move down" disabled={index === items.length - 1 || items[index + 1].category !== item.category} onClick={() => move(index, 1)} className="rounded-lg p-2 text-slate-500 hover:bg-slate-100 disabled:opacity-30 dark:hover:bg-slate-800"><FiArrowDown /></button><button type="button" aria-label="Edit FAQ" onClick={() => { setEditing(item); setForm({ question: item.question, answer: item.answer, category: item.category, order: item.order }); setFormOpen(true); }} className="rounded-lg p-2 text-[#2C6F99] hover:bg-[#4B98C8]/10"><FiEdit2 /></button><button type="button" aria-label="Delete FAQ" onClick={() => remove(item)} className="rounded-lg p-2 text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/30"><FiTrash2 /></button></div></div>)}</div>}
  </div>;
};

export default FAQManagementTab;
