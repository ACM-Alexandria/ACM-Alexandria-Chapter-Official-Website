import { useCallback, useEffect, useRef, useState } from "react";
import { FiArchive, FiCalendar, FiEdit2, FiPlus, FiRefreshCw, FiTrash2 } from "react-icons/fi";
import { useAuth } from "../../../contexts/useAuth";
import {
  fetchCurrentBoardSeason,
  fetchPastSeasons,
  fetchPastResource,
  deletePastSeason,
  updatePastSeason,
  createPastResource,
  updatePastResource,
  deletePastResource,
} from "../../../services/pastBoardsAdminService";
import { formatBoardSeason } from "../../../utils/boardSeasons";
import { isPastBoardSeason } from "../../../utils/validation";
import useThemedDialog from "../../../hooks/useThemedDialog";
import Toast from "../../Toast";
import SpinnerIcon from "../../icons/SpinnerIcon";
import { panelClass, primaryButtonClass, secondaryButtonClass, hintClass, noticeClass, tagBrandClass, tagMutedClass } from "../SharedComponents/adminStyles";
import DeleteConfirmModal from "../ManagementSection/DeleteConfirmModal";
import PastResourceForm from "./PastResourceForm";
import PastResourceTable from "./PastResourceTable";
import PastSeasonPicker from "./PastSeasonPicker";
import ArchiveSeasonSection from "./ArchiveSeasonSection";
import RenameSeasonModal from "./RenameSeasonModal";
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
  const [deletingSeason, setDeletingSeason] = useState(false);
  const [renamingSeason, setRenamingSeason] = useState(false);
  const [saving, setSaving] = useState(false);
  const [archiving, setArchiving] = useState(false);
  const [formError, setFormError] = useState("");
  const generation = useRef(0);
  const mutationInFlight = useRef(false);
  const { dialog, notify } = useThemedDialog();
  const { user } = useAuth();
  // Only the super admin can archive the current season; every board role can manage the records
  const isSuperAdmin = user?.role === "SUPER_ADMIN";
  // Every board role can change or delete a season; archiving stays super-admin only
  const canManageSeason = ["SUPER_ADMIN", "ACM_HIGH_BOARD", "ACM_COMMITTEE_BOARD", "ACM_CLUB_BOARD"].includes(user?.role);

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
      notify({ title: "Invalid season", message: `Enter a season from 2000 to ${currentSeason - 1}.` });
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

  const removeSeason = async () => {
    if (mutationInFlight.current) return;
    mutationInFlight.current = true;
    setSaving(true);
    try {
      await deletePastSeason(season);
      setDeletingSeason(false);
      const refreshed = await load();
      setNotice(refreshed ? `Season ${season} deleted.` : `Season ${season} deleted. Retry loading the lists to see the latest data.`);
    } catch (error) {
      setDeletingSeason(false);
      setToast(messageOf(error));
    } finally {
      mutationInFlight.current = false;
      setSaving(false);
    }
  };

  const renameSeason = async (newSeason) => {
    if (mutationInFlight.current) return;
    mutationInFlight.current = true;
    setSaving(true);
    setFormError("");
    try {
      await updatePastSeason(season, newSeason);
      setRenamingSeason(false);
      const refreshed = await load(newSeason);
      setNotice(refreshed ? `Season moved to ${newSeason}.` : `Season moved to ${newSeason}. Retry loading the lists to see the latest data.`);
    } catch (error) {
      setFormError(messageOf(error));
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
    <section className="flex flex-col gap-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0">
          <h2 className="text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-100">Past boards</h2>
          <p className="mt-0.5 text-sm text-slate-600 dark:text-slate-400">Browse and manage the chapter’s earlier boards by season.</p>
        </div>
        {currentSeason && (
          <p className={`${tagBrandClass} w-fit`}>
            <FiCalendar aria-hidden="true" className="mr-1.5 h-3.5 w-3.5" />
            Current: <span className="ml-1 tabular-nums">{formatBoardSeason(currentSeason)}</span>
          </p>
        )}
      </div>

      {isSuperAdmin && (
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
      )}

      <PastSeasonPicker currentSeason={currentSeason} seasons={seasons} season={season} disabled={loading || saving || archiving} onSelect={selectSeason} />

      {canManageSeason && season && seasons.includes(Number(season)) && (
        <div className="flex flex-wrap justify-end gap-2">
          <button type="button" disabled={loading || saving || archiving} onClick={() => { setFormError(""); setRenamingSeason(true); }} className={secondaryButtonClass}>
            <FiEdit2 aria-hidden="true" className="h-4 w-4" />Change year
          </button>
          <button type="button" disabled={loading || saving || archiving} onClick={() => setDeletingSeason(true)} className={`${secondaryButtonClass} text-rose-700 dark:text-rose-400`}>
            <FiTrash2 aria-hidden="true" className="h-4 w-4" />Delete season {season}
          </button>
        </div>
      )}

      {!loading && !loadError && notice && <p role="status" className={noticeClass.success}>{notice}</p>}

      {loadError ? (
        <div role="alert" className={`flex flex-col gap-3 ${noticeClass.error}`}>
          <p className="font-semibold">Couldn’t load past boards</p>
          {notice && <p role="status" className="font-medium">{notice}</p>}
          <p>{loadError}</p>
          <div>
            <button type="button" disabled={saving || archiving} onClick={async () => { if (await load(season)) setNotice(""); }} className={secondaryButtonClass}>
              <FiRefreshCw aria-hidden="true" className="h-4 w-4" />Retry
            </button>
          </div>
        </div>
      ) : loading ? (
        <p role="status" className="flex items-center justify-center gap-3 py-16 text-sm font-medium text-slate-600 dark:text-slate-400">
          <SpinnerIcon aria-hidden="true" className="h-4 w-4 animate-spin text-[#4B98C8]" />Loading past boards…
        </p>
      ) : season ? (
        <>
          {!seasons.includes(Number(season)) && (
            <p className={noticeClass.warning}>
              Start {formatBoardSeason(season)} by adding its first record below.
            </p>
          )}
          {pastBoardResources.map((resource) => {
            const needsParent = resource.parent && !data[resource.parent].length;
            return (
              <section key={resource.path} aria-label={resource.label} className={`${panelClass} p-4 sm:p-6`}>
                <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                  <div className="flex items-center gap-2.5">
                    <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100">{resource.label}</h3>
                    <span className={tagMutedClass} aria-label={`${data[resource.path].length} records`}>{data[resource.path].length}</span>
                  </div>
                  <button type="button" disabled={saving || archiving || needsParent} onClick={() => startEditing(resource)} className={`${primaryButtonClass} w-full sm:w-auto`}>
                    <FiPlus aria-hidden="true" className="h-4 w-4" />Add {resource.member ? "member" : resource.path === "clubs" ? "club" : "committee"}
                  </button>
                </div>
                {needsParent && <p className={`mb-4 ${hintClass}`}>Add {resource.parent === "clubs" ? "a club" : "a committee"} for this season first to enable board members.</p>}
                <PastResourceTable resource={resource} items={data[resource.path]} parents={data[resource.parent] || []} busy={saving || archiving} onEdit={(item) => startEditing(resource, item)} onDelete={(item) => { setNotice(""); setDeleting({ resource, item }); }} />
              </section>
            );
          })}
        </>
      ) : (
        <div className="rounded-2xl border border-dashed border-slate-300 px-6 py-12 text-center dark:border-slate-700">
          <FiArchive aria-hidden="true" className="mx-auto mb-3 h-8 w-8 text-slate-400 dark:text-slate-500" />
          <p className="text-sm font-medium text-slate-800 dark:text-slate-200">No past boards yet</p>
          <p className={`mt-2 ${hintClass}`}>Choose “Add past season” above to start an earlier board.</p>
        </div>
      )}

      {editing && <PastResourceForm key={`${editing.resource.path}-${editing.item?.id || "new"}`} resource={editing.resource} item={editing.item} parents={data[editing.resource.parent] || []} season={Number(season)} saving={saving} serverError={formError} onSave={save} onClose={() => { if (!mutationInFlight.current) setEditing(null); }} />}
      <DeleteConfirmModal open={Boolean(deleting)} onClose={() => { if (!mutationInFlight.current) setDeleting(null); }} onConfirm={remove} loading={saving} deletingItem={deleting ? { name: deleting.item.userName || deleting.item.name } : null}
        warning={deleting && !deleting.resource.member ? "All board members belonging to this past committee or club will also be permanently deleted." : undefined} />
      <DeleteConfirmModal open={deletingSeason} onClose={() => { if (!mutationInFlight.current) setDeletingSeason(false); }} onConfirm={removeSeason} loading={saving}
        deletingItem={{ name: `Season ${season}` }}
        warning="Every board record and member of this season will also be permanently deleted." />
      {renamingSeason && (
        <RenameSeasonModal season={Number(season)} currentSeason={currentSeason} saving={saving} serverError={formError}
          onSave={renameSeason} onClose={() => { if (!mutationInFlight.current) setRenamingSeason(false); }} />
      )}
      {toast && <Toast message={toast} onClose={() => setToast("")} />}
      {dialog}
    </section>
  );
};

export default PastBoardsTab;
