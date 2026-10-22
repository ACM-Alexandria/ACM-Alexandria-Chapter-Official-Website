import { useCallback, useEffect, useRef, useState } from "react";
import { FiArchive, FiCalendar, FiPlus, FiRefreshCw } from "react-icons/fi";
import {
  fetchCurrentBoardSeason,
  fetchPastSeasons,
  fetchPastResource,
  createPastResource,
  updatePastResource,
  deletePastResource,
} from "../../../services/pastBoardsAdminService";
import { formatBoardSeason } from "../../../utils/boardSeasons";
import { isPastBoardSeason } from "../../../utils/validation";
import useThemedDialog from "../../../hooks/useThemedDialog";
import Toast from "../../Toast";
import SpinnerIcon from "../../icons/SpinnerIcon";
import StatusAlert from "../EmailCenterSection/StatusAlert";
import DeleteConfirmModal from "../ManagementSection/DeleteConfirmModal";
import PastResourceForm from "./PastResourceForm";
import PastResourceTable from "./PastResourceTable";
import PastSeasonPicker from "./PastSeasonPicker";
import ArchiveSeasonSection from "./ArchiveSeasonSection";
import { pastBoardResources } from "./pastBoardResources";

const messageOf = (error) => error.response?.data?.message || error.message || "Unable to complete the request.";
const emptyData = () => Object.fromEntries(pastBoardResources.map(({ path }) => [path, []]));

const PastBoardsTab = () => {
  const [currentSeason, setCurrentSeason] = useState(null);
  const [seasons, setSeasons] = useState([]);
  const [season, setSeason] = useState("");
  const [data, setData] = useState(emptyData);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [toast, setToast] = useState("");
  const [notice, setNotice] = useState("");
  const [editing, setEditing] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [saving, setSaving] = useState(false);
  const [archiving, setArchiving] = useState(false);
  const [formError, setFormError] = useState("");
  const generation = useRef(0);
  const mutationInFlight = useRef(false);
  const { dialog, notify } = useThemedDialog();

  const load = useCallback(async (selected) => {
    const request = ++generation.current;
    setLoading(true);
    setLoadError("");
    try {
      const [current, available] = await Promise.all([fetchCurrentBoardSeason(), fetchPastSeasons()]);
      if (request !== generation.current) return false;
      const chosen = selected || available[0] || "";
      setCurrentSeason(current);
      setSeasons(available);
      setSeason(chosen);
      const rows = chosen
        ? await Promise.all(pastBoardResources.map(({ path }) => fetchPastResource(path, chosen)))
        : [];
      if (request !== generation.current) return false;
      setData(chosen
        ? Object.fromEntries(pastBoardResources.map(({ path }, index) => [path, rows[index]]))
        : emptyData());
      return true;
    } catch (error) {
      if (request === generation.current) setLoadError(messageOf(error));
      return false;
    } finally {
      if (request === generation.current) setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
    return () => { generation.current += 1; };
  }, [load]);

  const selectSeason = (value) => {
    if (!isPastBoardSeason(value, currentSeason)) {
      notify({ title: "Invalid season", message: `Choose a starting year from 2000 to ${currentSeason - 1}.` });
      return;
    }
    setSeason(value);
    setNotice("");
    load(value);
  };

  const save = async (payload) => {
    if (mutationInFlight.current) return;
    mutationInFlight.current = true;
    setSaving(true);
    setFormError("");
    try {
      if (editing.item) await updatePastResource(editing.resource.path, editing.item.id, payload);
      else await createPastResource(editing.resource.path, payload);
      setEditing(null);
      const refreshed = await load(season);
      setNotice(refreshed ? "Record saved." : "Record saved. Retry loading the lists to see the latest data.");
    } catch (error) {
      setFormError(messageOf(error));
    } finally {
      mutationInFlight.current = false;
      setSaving(false);
    }
  };

  const remove = async () => {
    if (mutationInFlight.current) return;
    mutationInFlight.current = true;
    setSaving(true);
    try {
      await deletePastResource(deleting.resource.path, deleting.item.id);
      setDeleting(null);
      const refreshed = await load(season);
      setNotice(refreshed ? "Record deleted." : "Record deleted. Retry loading the lists to see the latest data.");
    } catch (error) {
      setToast(messageOf(error));
    } finally {
      mutationInFlight.current = false;
      setSaving(false);
    }
  };

  const startEditing = (resource, item = null) => {
    setFormError("");
    setNotice("");
    setEditing({ resource, item });
  };

  return (
    <section className="space-y-6 animate-[fadeIn_0.4s_ease]">
      <div className="flex flex-col gap-4 border-b border-slate-200 pb-5 dark:border-slate-700 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-start gap-3">
          <div className="rounded-xl bg-[#4B98C8]/10 p-2.5 text-[#205E85] dark:text-sky-300"><FiArchive aria-hidden="true" className="h-5 w-5" /></div>
          <div>
            <h2 className="text-lg font-extrabold text-slate-900 dark:text-white">Past Boards</h2>
            <p className="mt-1 text-xs leading-relaxed text-slate-500 dark:text-slate-300">Browse and manage the chapter’s earlier boards.</p>
          </div>
        </div>
        {currentSeason && (
          <div className="inline-flex w-fit items-center gap-2 rounded-lg bg-slate-100 px-3 py-2 text-xs font-semibold text-slate-500 dark:bg-slate-800 dark:text-slate-300">
            <FiCalendar aria-hidden="true" className="h-3.5 w-3.5" />
            Current: <span className="font-bold text-slate-700 dark:text-slate-100">{formatBoardSeason(currentSeason)}</span>
          </div>
        )}
      </div>

      <ArchiveSeasonSection
        currentSeason={currentSeason}
        disabled={loading || Boolean(loadError) || saving || Boolean(editing) || Boolean(deleting)}
        onBusyChange={(value) => {
          mutationInFlight.current = value;
          setArchiving(value);
        }}
        onArchived={(result) => {
          setNotice("");
          return load(result.archivedSeason);
        }}
        onConflict={() => load(season)}
      />

      <PastSeasonPicker currentSeason={currentSeason} seasons={seasons} season={season} disabled={loading || saving || archiving} onSelect={selectSeason} />

      <StatusAlert type="success" message={!loading && !loadError ? notice : ""} />

      {loadError ? (
        <div role="alert" className="rounded-2xl border border-red-200 bg-red-50 p-5 text-sm text-red-700 dark:border-red-900 dark:bg-red-950/20 dark:text-red-300">
          <p className="font-bold">Couldn’t load past boards</p>
          {notice && <p role="status" className="mt-2 font-semibold">{notice}</p>}
          <p className="mt-1">{loadError}</p>
          <button type="button" disabled={saving || archiving} onClick={async () => { if (await load(season)) setNotice(""); }} className="mt-3 inline-flex items-center gap-2 rounded-lg border border-red-200 bg-white px-3 py-2 font-bold disabled:opacity-50 dark:border-red-800 dark:bg-slate-900"><FiRefreshCw aria-hidden="true" />Retry</button>
        </div>
      ) : loading ? (
        <p role="status" className="flex items-center justify-center gap-3 py-16 text-sm font-semibold text-slate-500 dark:text-slate-300"><SpinnerIcon aria-hidden="true" className="h-4 w-4 animate-spin text-[#4B98C8]" />Loading past boards…</p>
      ) : season ? (
        <>
          {!seasons.includes(Number(season)) && (
            <p className="rounded-xl border border-dashed border-[#4B98C8]/40 bg-[#4B98C8]/5 px-4 py-3 text-sm text-[#205E85] dark:text-sky-300">
              Start {formatBoardSeason(season)} by adding its first record below.
            </p>
          )}
          {pastBoardResources.map((resource) => {
            const needsParent = resource.parent && !data[resource.parent].length;
            return (
              <section key={resource.path} aria-label={resource.label} className="rounded-2xl border border-slate-200 bg-white p-4 dark:border-slate-700 dark:bg-slate-900 sm:p-6">
                <div className="mb-4 flex items-center justify-between gap-3">
                  <div className="flex items-center gap-2.5">
                    <h3 className="text-base font-extrabold text-slate-800 dark:text-slate-100">{resource.label}</h3>
                    <span className="rounded-md bg-slate-100 px-2 py-0.5 text-xs font-bold tabular-nums text-slate-500 dark:bg-slate-800 dark:text-slate-300" aria-label={`${data[resource.path].length} records`}>{data[resource.path].length}</span>
                  </div>
                  <button type="button" disabled={saving || archiving || needsParent} onClick={() => startEditing(resource)} className="inline-flex shrink-0 items-center gap-1.5 rounded-xl bg-[#205E85] px-3 py-2.5 text-xs font-bold text-white transition hover:bg-[#4B98C8] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8] focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-40 sm:px-4">
                    <FiPlus aria-hidden="true" />Add {resource.member ? "member" : resource.path === "clubs" ? "club" : "committee"}
                  </button>
                </div>
                {needsParent && <p className="mb-3 text-xs text-slate-500 dark:text-slate-300">Add {resource.parent === "clubs" ? "a club" : "a committee"} for this season first to enable board members.</p>}
                <PastResourceTable resource={resource} items={data[resource.path]} parents={data[resource.parent] || []} busy={saving || archiving} onEdit={(item) => startEditing(resource, item)} onDelete={(item) => { setNotice(""); setDeleting({ resource, item }); }} />
              </section>
            );
          })}
        </>
      ) : (
        <div className="rounded-2xl border border-dashed border-slate-300 px-6 py-12 text-center dark:border-slate-700">
          <FiArchive aria-hidden="true" className="mx-auto mb-3 h-8 w-8 text-slate-300 dark:text-slate-600" />
          <p className="text-sm font-bold text-slate-700 dark:text-slate-200">No past boards yet</p>
          <p className="mt-2 text-xs text-slate-500 dark:text-slate-300">Choose “Add past season” above to start an earlier board.</p>
        </div>
      )}

      {editing && <PastResourceForm key={`${editing.resource.path}-${editing.item?.id || "new"}`} resource={editing.resource} item={editing.item} parents={data[editing.resource.parent] || []} season={Number(season)} saving={saving} serverError={formError} onSave={save} onClose={() => { if (!mutationInFlight.current) setEditing(null); }} />}
      <DeleteConfirmModal open={Boolean(deleting)} onClose={() => { if (!mutationInFlight.current) setDeleting(null); }} onConfirm={remove} loading={saving} deletingItem={deleting ? { name: deleting.item.userName || deleting.item.name } : null}
        warning={deleting && !deleting.resource.member ? "All board members belonging to this past committee or club will also be permanently deleted." : undefined} />
      {toast && <Toast message={toast} onClose={() => setToast("")} />}
      {dialog}
    </section>
  );
};

export default PastBoardsTab;
