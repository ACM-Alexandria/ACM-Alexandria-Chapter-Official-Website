import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { FiAlertTriangle, FiAward, FiCheckCircle, FiImage, FiSearch } from "react-icons/fi";
import { awardBadges, fetchCommitteeRanking, messageOf, revokeAward } from "../../../services/badgesAdminService";
import { fetchCommittee } from "../../../services/homePageService";
import { resolveMonth } from "../../../services/seasonsAdminService";
import { isCurrentPeriod, todayIso } from "../../../utils/seasonDates";
import useThemedDialog from "../../../hooks/useThemedDialog";
import SpinnerIcon from "../../icons/SpinnerIcon";
import {
  fieldClass,
  hintClass,
  labelClass,
  noticeClass,
  panelClass,
  primaryButtonClass,
  secondaryButtonClass,
  tagBrandClass,
  tagWarnClass,
} from "../SharedComponents/adminStyles";
import MemberAvatar from "./MemberAvatar";
import PosterExportModal from "./PosterExportModal";
import { currentMonth } from "./badgeUtils";

const smallButton =
  "h-8 px-3 inline-flex items-center justify-center gap-1.5 rounded-lg text-sm font-semibold whitespace-nowrap transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ocean-461/40 disabled:opacity-50 disabled:cursor-not-allowed";

// Award a badge within one committee. Pick the badge and the period, then a committee and how many winners
// it gets: the members who already have the badge are at the top; below are the committee's other members
// ranked by their points in the period, with the open winner places marked as suggested.
// Awarding moves a member up; revoking moves them back down. Everything reloads automatically.
const AwardPanel = ({ types, periods }) => {
  const awardable = useMemo(() => types.filter((t) => t.active), [types]);
  const [typeId, setTypeId] = useState(() => awardable[0]?.id ?? "");
  const type = awardable.find((t) => t.id === Number(typeId)) || null;
  const periodType = type?.periodType ?? null;

  const [month, setMonth] = useState(currentMonth);
  const [periodChoice, setPeriodChoice] = useState("");
  const [period, setPeriod] = useState(null);
  const [periodError, setPeriodError] = useState("");

  // Winners for each committee, by committee id; a committee left unset uses the badge's default (3).
  const [winnersByCommittee, setWinnersByCommittee] = useState({});

  const [committees, setCommittees] = useState([]);
  const [committeeId, setCommitteeId] = useState("");
  const [search, setSearch] = useState("");

  const [ranking, setRanking] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(() => new Set());
  const [notice, setNotice] = useState("");
  const [posterOpen, setPosterOpen] = useState(false);
  const { dialog, confirm, notify } = useThemedDialog();

  useEffect(() => {
    fetchCommittee()
      .then((list) => {
        const sorted = (Array.isArray(list) ? list : []).slice().sort((a, b) => a.name.localeCompare(b.name));
        setCommittees(sorted);
        setCommitteeId((current) => current || (sorted[0] ? String(sorted[0].id) : ""));
      })
      .catch(() => setCommittees([]));
  }, []);

  // Quarters or seasons the chosen badge can be awarded for; the one covering today is picked by default.
  const periodOptions = useMemo(
    () => (periodType === "MONTH" ? [] : periods.filter((p) => p.type === periodType)),
    [periods, periodType],
  );

  useEffect(() => {
    setPeriodChoice("");
    setNotice("");
  }, [typeId]);

  // Resolve the period: a calendar month from the month picker, or a quarter/season from the list.
  useEffect(() => {
    let ignore = false;
    setPeriodError("");
    if (!periodType) {
      setPeriod(null);
      return undefined;
    }
    if (periodType === "MONTH") {
      if (!month) {
        setPeriod(null);
        return undefined;
      }
      resolveMonth(month)
        .then((p) => { if (!ignore) setPeriod(p); })
        .catch((e) => { if (!ignore) { setPeriod(null); setPeriodError(messageOf(e)); } });
      return () => { ignore = true; };
    }
    const today = todayIso();
    const chosen = periodOptions.find((p) => String(p.id) === periodChoice)
      || periodOptions.find((p) => isCurrentPeriod(p, today))
      || periodOptions[0]
      || null;
    setPeriod(chosen);
    return undefined;
  }, [periodType, month, periodChoice, periodOptions]);

  const winners = winnersByCommittee[committeeId] ?? null;
  // Typing a number re-ranks after a short pause, without a button; switching committee reloads at once.
  const [debounced, setDebounced] = useState({ committeeId, winners });
  useEffect(() => {
    const timer = setTimeout(() => setDebounced({ committeeId, winners }), 400);
    return () => clearTimeout(timer);
  }, [committeeId, winners]);
  const debouncedWinners = debounced.committeeId === committeeId ? debounced.winners : winners;

  // The period lags one render behind a badge change; nothing loads until it matches the badge.
  const ready = Boolean(type && period && period.type === type.periodType && committeeId);
  const request = useRef(0);

  const load = useCallback(async () => {
    const id = ++request.current;
    setError("");
    if (!ready) {
      setRanking(null);
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const result = await fetchCommitteeRanking(type.id, period.id, Number(committeeId), debouncedWinners);
      if (id === request.current) setRanking(result);
    } catch (e) {
      if (id === request.current) {
        setRanking(null);
        setError(messageOf(e));
      }
    } finally {
      if (id === request.current) setLoading(false);
    }
  }, [ready, type, period, committeeId, debouncedWinners]);

  useEffect(() => { load(); }, [load]);

  const setMemberBusy = (ids, on) => setBusy((prev) => {
    const next = new Set(prev);
    ids.forEach((id) => (on ? next.add(id) : next.delete(id)));
    return next;
  });

  const award = async (members) => {
    if (!ready || members.length === 0) return;
    const ids = members.map((m) => m.memberId);
    setMemberBusy(ids, true);
    setNotice("");
    try {
      await awardBadges(type.id, period.id, members.map((m) => ({ memberId: m.memberId, rank: m.rank })));
      setNotice(members.length === 1
        ? `Awarded ${type.name} to ${members[0].name}.`
        : `Awarded ${type.name} to ${members.length} members.`);
      await load();
    } catch (e) {
      notify({ tone: "danger", title: "Couldn't award", message: messageOf(e) });
    } finally {
      setMemberBusy(ids, false);
    }
  };

  const revoke = async (a) => {
    const ok = await confirm({
      tone: "danger",
      title: "Revoke this badge?",
      message: `${a.member.name} will no longer see ${a.name} (${a.periodLabel}) on their profile or public page. You can award it again.`,
      confirmLabel: "Revoke",
    });
    if (!ok) return;
    setMemberBusy([a.member.id], true);
    setNotice("");
    try {
      await revokeAward(a.id);
      await load();
    } catch (e) {
      notify({ tone: "danger", title: "Couldn't revoke", message: messageOf(e) });
    } finally {
      setMemberBusy([a.member.id], false);
    }
  };

  if (awardable.length === 0) {
    return <p className={`${panelClass} p-6 text-sm text-slate-600 dark:text-slate-400`}>No badges are available. Restart the backend to create them.</p>;
  }

  const members = ranking?.members ?? [];
  const awarded = ranking?.awarded ?? [];
  const query = search.trim().toLowerCase();
  const shownMembers = query ? members.filter((m) => (m.name || "").toLowerCase().includes(query)) : members;
  const suggested = members.filter((m) => m.suggested);
  const winnersShown = winners ?? (ranking && String(ranking.committeeId) === committeeId ? ranking.winners : 3);

  return (
    <div className="flex flex-col gap-4">
      <div className={`${panelClass} p-4 sm:p-5`}>
        <div className="grid gap-3 sm:grid-cols-2">
          <div className="flex flex-col gap-1.5">
            <label htmlFor="award-type" className={labelClass}>Badge</label>
            <select id="award-type" value={typeId} onChange={(e) => setTypeId(e.target.value)} className={fieldClass}>
              {awardable.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
            </select>
          </div>
          <div className="flex flex-col gap-1.5">
            <label htmlFor="award-period" className={labelClass}>Period</label>
            {periodType === "MONTH" ? (
              <input id="award-period" type="month" value={month} onChange={(e) => setMonth(e.target.value)} className={fieldClass} />
            ) : periodOptions.length > 0 ? (
              <select id="award-period" value={period ? String(period.id) : ""} onChange={(e) => setPeriodChoice(e.target.value)} className={fieldClass}>
                {periodOptions.map((p) => <option key={p.id} value={p.id}>{p.label}</option>)}
              </select>
            ) : (
              <p id="award-period" className={`${hintClass} h-10 flex items-center`}>No {periodType === "SEASON" ? "seasons" : "quarters"} yet. The super admin adds them in Season settings.</p>
            )}
          </div>
        </div>
        {periodError && <p role="alert" className={`mt-3 ${noticeClass.error}`}>{periodError}</p>}
        <div className="mt-4 pt-4 border-t border-slate-200 dark:border-ocean-800 flex flex-wrap items-center justify-between gap-3">
          <p className={hintClass}>An image of this period's winners from every committee, to post or share as a story.</p>
          <button
            type="button"
            onClick={() => setPosterOpen(true)}
            disabled={!type || !period || period.type !== type.periodType}
            className={secondaryButtonClass}
          >
            <FiImage className="w-4 h-4" aria-hidden="true" />
            Export poster
          </button>
        </div>
      </div>

      <div className={`${panelClass} p-4 sm:p-5 grid gap-3 grid-cols-[1fr_8rem] sm:grid-cols-[1fr_12rem]`}>
        <div className="flex flex-col gap-1.5 min-w-0">
          <label htmlFor="award-committee" className={labelClass}>Committee</label>
          <select id="award-committee" value={committeeId} onChange={(e) => { setCommitteeId(e.target.value); setSearch(""); setNotice(""); }} className={fieldClass}>
            {committees.length === 0 && <option value="">No committees</option>}
            {committees.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </div>
        <div className="flex flex-col gap-1.5">
          <label htmlFor="award-winners" className={labelClass}>Winners</label>
          <input
            id="award-winners"
            type="number"
            min="1"
            max="50"
            value={winnersShown}
            disabled={!committeeId}
            onChange={(e) => {
              const n = Math.max(1, Math.min(50, Number(e.target.value) || 1));
              setWinnersByCommittee((w) => ({ ...w, [committeeId]: n }));
            }}
            className={fieldClass}
          />
        </div>
      </div>

      {notice && (
        <p role="status" className={`flex items-start gap-2.5 ${noticeClass.success}`}>
          <FiCheckCircle className="w-5 h-5 shrink-0 mt-px" aria-hidden="true" />{notice}
        </p>
      )}
      {error && (
        <p role="alert" className={`flex items-start gap-2.5 ${noticeClass.error}`}>
          <FiAlertTriangle className="w-5 h-5 shrink-0 mt-px" aria-hidden="true" />{error}
        </p>
      )}

      {ready && ranking && (
        <>
          <section aria-labelledby="awarded-title" className={`${panelClass} overflow-hidden`}>
            <div className="px-4 sm:px-5 py-3.5 flex flex-wrap items-baseline justify-between gap-2 border-b border-slate-200 dark:border-ocean-800">
              <h3 id="awarded-title" className="text-base font-semibold text-slate-900 dark:text-slate-100">
                Awarded · {ranking.committeeName}
              </h3>
              <p className={hintClass}>{awarded.length} of {ranking.winners} · {ranking.period.label}</p>
            </div>
            {awarded.length === 0 ? (
              <p className="px-4 sm:px-5 py-4 text-sm text-slate-600 dark:text-slate-400">
                No one in {ranking.committeeName} has {ranking.badgeType.name} for {ranking.period.label} yet.
              </p>
            ) : (
              <ul>
                {awarded.map((a) => (
                  <li key={a.id} className="flex items-center gap-3 px-4 sm:px-5 py-2.5 border-t first:border-t-0 border-slate-100 dark:border-ocean-800">
                    <FiAward className="w-4 h-4 shrink-0 text-amber-600 dark:text-amber-400" aria-hidden="true" />
                    <MemberAvatar name={a.member.name} imageUrl={a.member.profileImageUrl} />
                    <span className="flex-1 min-w-0 truncate text-sm font-medium text-slate-900 dark:text-slate-100">{a.member.name}</span>
                    <button
                      type="button"
                      onClick={() => revoke(a)}
                      disabled={busy.has(a.member.id)}
                      className={`${smallButton} text-rose-700 dark:text-rose-300 hover:bg-rose-50 dark:hover:bg-rose-950/40`}
                    >
                      Revoke
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </section>

          <section aria-labelledby="members-title" className={`${panelClass} overflow-hidden`}>
            <div className="px-4 sm:px-5 py-3.5 flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 dark:border-ocean-800">
              <div>
                <h3 id="members-title" className="text-base font-semibold text-slate-900 dark:text-slate-100">Members</h3>
                <p className={hintClass}>Ranked by points in {ranking.period.label}. Board members aren't ranked.</p>
              </div>
              <button
                type="button"
                onClick={() => award(suggested)}
                disabled={suggested.length === 0 || suggested.some((m) => busy.has(m.memberId))}
                className={primaryButtonClass}
              >
                <FiAward className="w-4 h-4" aria-hidden="true" />
                Award suggested{suggested.length > 0 ? ` (${suggested.length})` : ""}
              </button>
            </div>

            {members.length > 0 && (
              <div className="px-4 sm:px-5 py-3 border-b border-slate-200 dark:border-ocean-800">
                <div className="relative">
                  <FiSearch className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" aria-hidden="true" />
                  <input
                    type="search"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder="Search members by name"
                    aria-label="Search members by name"
                    autoComplete="off"
                    className={`${fieldClass} pl-9`}
                  />
                </div>
              </div>
            )}

            {members.length === 0 ? (
              <p className="px-4 sm:px-5 py-6 text-sm text-slate-600 dark:text-slate-400">
                {awarded.length > 0 ? "Everyone in this committee already has this badge." : "This committee has no members yet."}
              </p>
            ) : shownMembers.length === 0 ? (
              <p className="px-4 sm:px-5 py-6 text-sm text-slate-600 dark:text-slate-400">No member matches “{search.trim()}”.</p>
            ) : (
              <ul>
                {shownMembers.map((m) => (
                  <li
                    key={m.memberId}
                    className={`flex items-center gap-3 px-4 sm:px-5 py-2.5 border-t first:border-t-0 border-slate-100 dark:border-ocean-800 ${m.suggested ? "bg-ocean-20 dark:bg-ocean-845/40" : ""}`}
                  >
                    <span className="w-6 text-sm font-semibold text-slate-600 dark:text-slate-400 tabular-nums">{m.rank}</span>
                    <MemberAvatar name={m.name} imageUrl={m.profileImageUrl} />
                    <span className="flex-1 min-w-0 truncate text-sm font-medium text-slate-900 dark:text-slate-100">{m.name}</span>
                    {m.suggested && <span className={tagBrandClass}>Suggested</span>}
                    {m.tied && <span className={tagWarnClass}>Tied</span>}
                    <span className="w-16 text-right text-sm font-semibold text-ocean-614 dark:text-ocean-316 tabular-nums whitespace-nowrap">{m.points} pts</span>
                    <button
                      type="button"
                      onClick={() => award([m])}
                      disabled={busy.has(m.memberId)}
                      aria-label={`Award ${ranking.badgeType.name} to ${m.name}`}
                      className={`${smallButton} border border-slate-300 dark:border-ocean-745 text-ocean-637 dark:text-ocean-316 hover:bg-ocean-67 dark:hover:bg-ocean-808`}
                    >
                      {busy.has(m.memberId) ? <SpinnerIcon className="w-4 h-4 animate-spin" /> : "Award"}
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </>
      )}

      {ready && !ranking && loading && <p className="text-sm text-slate-600 dark:text-slate-400">Loading members…</p>}
      {posterOpen && type && period && (
        <PosterExportModal key={`${type.id}-${period.id}`} badge={type} period={period} onClose={() => setPosterOpen(false)} />
      )}
      {dialog}
    </div>
  );
};

export default AwardPanel;
