import { useEffect, useMemo, useRef, useState } from "react";
import { FiAlertTriangle, FiDownload } from "react-icons/fi";
import { getFontEmbedCSS, toPng } from "html-to-image";
import { fetchAwards, messageOf } from "../../../services/badgesAdminService";
import ModalShell from "../SharedComponents/ModalShell";
import { fieldClass, hintClass, noticeClass, primaryButtonClass, secondaryButtonClass } from "../SharedComponents/adminStyles";
import SpinnerIcon from "../../icons/SpinnerIcon";
import PeriodPoster from "./PeriodPoster";
import { POSTER_FORMATS, groupByCommittee, paginate, paginateCommittee } from "./posterLayout";

const MONTHS = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
// How small each page is drawn in the preview.
const PREVIEW_SCALE = { square: 0.36, story: 0.2 };

// "November 2026" for a month; the period's own label ("Q2 · 2027", "Season 2027") otherwise.
const periodTitle = (period) => {
  if (period.type !== "MONTH" || !period.startDate) return period.label;
  const [year, month] = period.startDate.split("-").map(Number);
  return `${MONTHS[month - 1]} ${year}`;
};

const slug = (s) => s.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");

const download = (dataUrl, filename) => {
  const a = document.createElement("a");
  a.href = dataUrl;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
};

// Preview and download the winners poster for a badge and period, square for posts or tall for stories. It can
// show every committee on one poster, one committee on its own (its name and its winners only), or one image per
// committee. A long list is split into several images, downloaded together.
// Mount it only while open, keyed by badge and period, so each opening loads fresh.
const PosterExportModal = ({ onClose, badge, period }) => {
  const [format, setFormat] = useState("square");
  // "all": every committee on one poster; "each": one image per committee; otherwise one committee's id.
  const [scope, setScope] = useState("all");
  const [awards, setAwards] = useState(null);
  const [error, setError] = useState("");
  const [exporting, setExporting] = useState(false);
  const pageRefs = useRef([]);

  useEffect(() => {
    let ignore = false;
    fetchAwards(period.id)
      .then((list) => { if (!ignore) setAwards(list.filter((a) => a.badgeTypeId === badge.id)); })
      .catch((e) => { if (!ignore) setError(messageOf(e)); });
    return () => { ignore = true; };
  }, [period, badge]);

  const groups = useMemo(() => (awards ? groupByCommittee(awards) : []), [awards]);
  const mode = scope === "all" ? "all" : "committee";
  // Each page is the list of committee cards on it; a one-committee page has just one.
  const pages = useMemo(() => {
    if (!awards) return [];
    if (scope === "all") return paginate(groups, format);
    const picked = scope === "each" ? groups : groups.filter((g) => g.id === scope);
    return picked.flatMap((g) => paginateCommittee(g, format)).map((card) => [card]);
  }, [awards, groups, scope, format]);
  const f = POSTER_FORMATS[format];
  const scale = PREVIEW_SCALE[format];
  const title = periodTitle(period);

  const exportAll = async () => {
    setExporting(true);
    setError("");
    try {
      await document.fonts?.ready;
      const nodes = pageRefs.current.slice(0, pages.length).filter(Boolean);
      // Fonts are read once and reused for every page.
      const fontEmbedCSS = await getFontEmbedCSS(nodes[0]);
      const used = {};
      for (let i = 0; i < nodes.length; i += 1) {
        const dataUrl = await toPng(nodes[i], { width: f.width, height: f.height, pixelRatio: 1, cacheBust: true, fontEmbedCSS });
        let part = nodes.length > 1 ? `-${i + 1}` : "";
        if (mode === "committee") {
          const key = slug(pages[i][0].name);
          used[key] = (used[key] || 0) + 1;
          part = `-${key}${used[key] > 1 ? `-${used[key]}` : ""}`;
        }
        download(dataUrl, `${slug(badge.name)}-${slug(period.label)}-${format}${part}.png`);
      }
    } catch (e) {
      console.error("Poster export failed:", e);
      setError("Couldn't create the image. Try again.");
    } finally {
      setExporting(false);
    }
  };

  const count = awards?.length ?? 0;
  const committees = awards ? new Set(awards.map((a) => a.committeeId)).size : 0;

  return (
    <ModalShell
      open
      onClose={onClose}
      busy={exporting}
      size="xl"
      title="Winners poster"
      subtitle={`${badge.name} · ${period.label}`}
      footer={(
        <>
          <button type="button" onClick={onClose} disabled={exporting} className={secondaryButtonClass}>Close</button>
          <button type="button" onClick={exportAll} disabled={exporting || pages.length === 0} className={primaryButtonClass}>
            {exporting ? <SpinnerIcon className="w-4 h-4 animate-spin" /> : <FiDownload className="w-4 h-4" aria-hidden="true" />}
            {pages.length > 1 ? `Download ${pages.length} images` : "Download image"}
          </button>
        </>
      )}
    >
      <div className="flex flex-col gap-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div role="group" aria-label="Poster size" className="inline-flex gap-1 rounded-xl border border-slate-200 dark:border-ocean-800 p-1">
            {Object.entries(POSTER_FORMATS).map(([id, def]) => (
              <button
                key={id}
                type="button"
                aria-pressed={format === id}
                onClick={() => setFormat(id)}
                disabled={exporting}
                className={`h-9 px-3.5 rounded-lg text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ocean-461/40 ${format === id ? "bg-ocean-67 text-ocean-637 dark:bg-ocean-808 dark:text-ocean-222" : "text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-100"}`}
              >
                {def.label} <span className="font-normal opacity-80">· {def.hint}</span>
              </button>
            ))}
          </div>
          {awards && count > 0 && groups.length > 0 && (
            <select
              aria-label="What to include in the poster"
              value={scope}
              onChange={(e) => setScope(e.target.value)}
              disabled={exporting}
              className={`${fieldClass} !w-auto max-w-full`}
            >
              <option value="all">All committees on one poster</option>
              <option value="each">One image per committee</option>
              {groups.map((g) => <option key={g.id} value={g.id}>{g.name} only</option>)}
            </select>
          )}
          {awards && count > 0 && (
            <p className={hintClass}>
              {count} {count === 1 ? "winner" : "winners"} from {committees} {committees === 1 ? "committee" : "committees"}
              {pages.length > 1 ? `, on ${pages.length} images` : ""}. No points are shown.
            </p>
          )}
        </div>

        {error && (
          <p role="alert" className={`flex items-start gap-2.5 ${noticeClass.error}`}>
            <FiAlertTriangle className="w-5 h-5 shrink-0 mt-px" aria-hidden="true" />{error}
          </p>
        )}

        {!awards && !error ? (
          <p className="text-sm text-slate-600 dark:text-slate-400">Loading winners…</p>
        ) : awards && count === 0 ? (
          <p className="text-sm text-slate-600 dark:text-slate-400">
            No one has {badge.name} for {period.label} yet. Award it first, then come back for the poster.
          </p>
        ) : (
          <div className="flex flex-wrap gap-4">
            {pages.map((cards, i) => (
              <div
                key={`${format}-${scope}-${i}`}
                className="shrink-0 overflow-hidden rounded-lg ring-1 ring-slate-200 dark:ring-ocean-800"
                style={{ width: f.width * scale, height: f.height * scale }}
              >
                <div style={{ width: f.width, height: f.height, transform: `scale(${scale})`, transformOrigin: "top left" }}>
                  <PeriodPoster
                    ref={(el) => { pageRefs.current[i] = el; }}
                    format={format}
                    mode={mode}
                    badge={badge}
                    periodTitle={title}
                    periodLabel={period.label}
                    periodType={period.type}
                    cards={cards}
                  />
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </ModalShell>
  );
};

export default PosterExportModal;
