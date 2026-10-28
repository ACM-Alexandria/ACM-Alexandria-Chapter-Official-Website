import React from "react";
import { StackedBars, Legend } from "./InsightsCharts";
import AnimatedNumber from "./AnimatedNumber";

// Tinted number tiles (style C), cycling through four tones.
const TILE_TONES = [
  { tile: "bg-ocean-57 dark:bg-ocean-808", label: "text-ocean-614 dark:text-ocean-316", value: "text-ocean-808 dark:text-white" },
  { tile: "bg-emerald-50 dark:bg-emerald-950/50", label: "text-emerald-700 dark:text-emerald-300", value: "text-emerald-900 dark:text-emerald-50" },
  { tile: "bg-amber-50 dark:bg-amber-950/50", label: "text-amber-800 dark:text-amber-300", value: "text-amber-900 dark:text-amber-50" },
  { tile: "bg-violet-50 dark:bg-violet-950/50", label: "text-violet-700 dark:text-violet-300", value: "text-violet-900 dark:text-violet-50" },
];

const DEFAULT_COLORS = ["var(--color-ocean-461)", "var(--color-chart-green)", "var(--color-chart-amber)", "var(--color-chart-purple)"];
const POINTS_COLORS = ["var(--color-chart-green)", "var(--color-chart-red)"];

const STATUS_TONES = {
  success: "bg-emerald-50 dark:bg-emerald-950/50 text-emerald-800 dark:text-emerald-200 border-emerald-200 dark:border-emerald-900",
  danger: "bg-rose-50 dark:bg-rose-950/50 text-rose-800 dark:text-rose-200 border-rose-200 dark:border-rose-900",
  neutral: "bg-slate-50 dark:bg-ocean-845 text-slate-800 dark:text-slate-200 border-slate-200 dark:border-ocean-745",
};

const cardClass = "rounded-2xl border border-slate-200 dark:border-ocean-800 bg-white dark:bg-ocean-890";

const Tile = ({ kpi, tone, index }) => (
  <div className={`${tone.tile} rounded-2xl p-4 flex flex-col gap-1 min-w-0 insights-rise`} style={{ animationDelay: `${index * 60}ms` }}>
    <span className={`text-sm font-medium truncate ${tone.label}`}>{kpi.label}</span>
    <span className={`text-3xl font-semibold tabular-nums ${tone.value}`}><AnimatedNumber value={kpi.value} /></span>
    {kpi.inPeriod !== null && kpi.inPeriod !== undefined && (
      <span className={`text-xs font-medium tabular-nums ${tone.label}`}><AnimatedNumber value={kpi.inPeriod} /> this period</span>
    )}
  </div>
);

const RankList = ({ ranking, index }) => {
  const max = Math.max(1, ...ranking.items.map((item) => item.value));
  return (
    <section className={`${cardClass} p-5 flex flex-col gap-4 min-w-0 insights-rise`} style={{ animationDelay: `${280 + index * 80}ms` }}>
      <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">{ranking.title}</h3>
      {ranking.items.length === 0 ? (
        <p className="text-sm text-slate-600 dark:text-slate-400">Nothing in this period.</p>
      ) : (
        <ol className="flex flex-col gap-3">
          {ranking.items.map((item, i) => (
            <li key={`${item.label}-${i}`} className="flex items-center gap-3">
              <span className="w-6 h-6 shrink-0 inline-flex items-center justify-center rounded-lg bg-slate-100 dark:bg-ocean-845 text-xs font-semibold tabular-nums text-slate-700 dark:text-slate-300">
                {i + 1}
              </span>
              <span className="min-w-0 flex-1 flex flex-col gap-1">
                <span className="flex items-baseline justify-between gap-3">
                  <span className="min-w-0 truncate text-sm font-medium text-slate-900 dark:text-slate-100">
                    {item.label}
                    {item.detail && <span className="ml-2 font-normal text-xs text-slate-600 dark:text-slate-400">{item.detail}</span>}
                  </span>
                  <span className="text-sm font-semibold tabular-nums text-slate-900 dark:text-slate-100"><AnimatedNumber value={item.value} /></span>
                </span>
                <span className="h-1.5 rounded-full bg-slate-100 dark:bg-ocean-845 overflow-hidden" aria-hidden="true">
                  <span
                    className="block h-full rounded-full bg-ocean-461 insights-bar-x"
                    style={{ width: `${(item.value / max) * 100}%`, animationDelay: `${350 + i * 50}ms` }}
                  />
                </span>
              </span>
            </li>
          ))}
        </ol>
      )}
    </section>
  );
};

// One section tab (style C): tinted tiles, a large chart, rankings and states.
const SectionPanel = ({ data }) => {
  const colors = data.id === "points" ? POINTS_COLORS : DEFAULT_COLORS;
  return (
    <div className="flex flex-col gap-4">
      {data.statuses.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {data.statuses.map((s) => (
            <span key={s.label} className={`inline-flex items-center gap-1.5 h-8 px-3 rounded-full border text-sm font-medium ${STATUS_TONES[s.tone] || STATUS_TONES.neutral}`}>
              {s.label}: {s.value}
            </span>
          ))}
        </div>
      )}

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        {data.kpis.map((kpi, i) => (
          <Tile key={kpi.key} kpi={kpi} tone={TILE_TONES[i % TILE_TONES.length]} index={i} />
        ))}
      </div>

      {data.charts.map((chart) => (
        <section key={chart.key} className={`${cardClass} p-5 flex flex-col gap-3 min-w-0 insights-rise`} style={{ animationDelay: "240ms" }}>
          <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">{chart.title}</h3>
          <StackedBars series={chart.series} colors={colors} label={chart.title} />
          {chart.series.length > 1 && <Legend series={chart.series} colors={colors} />}
        </section>
      ))}

      {data.rankings.length > 0 && (
        <div className={`grid grid-cols-1 ${data.rankings.length > 1 ? "lg:grid-cols-2" : ""} gap-4`}>
          {data.rankings.map((ranking, i) => (
            <RankList key={ranking.key} ranking={ranking} index={i} />
          ))}
        </div>
      )}
    </div>
  );
};

export default SectionPanel;
