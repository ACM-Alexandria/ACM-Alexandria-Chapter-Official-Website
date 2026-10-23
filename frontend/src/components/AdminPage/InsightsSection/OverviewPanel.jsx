import React, { createElement, useState } from "react";
import { FiLayers, FiAward, FiBookOpen, FiCalendar } from "react-icons/fi";
import { AreaChart, Sparkline } from "./InsightsCharts";
import AnimatedNumber from "./AnimatedNumber";

const OPEN_ICONS = { committeeCall: FiLayers, club: FiAward, program: FiBookOpen, event: FiCalendar };
const OPEN_PREVIEW = 6;

const cardClass = "rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900";

const KpiCard = ({ kpi, index }) => (
  <div className={`${cardClass} p-4 flex flex-col gap-1 min-w-0 insights-rise`} style={{ animationDelay: `${index * 60}ms` }}>
    <span className="text-sm text-slate-600 dark:text-slate-400 truncate">{kpi.label}</span>
    <span className="text-2xl font-semibold tabular-nums text-slate-900 dark:text-slate-100"><AnimatedNumber value={kpi.value} /></span>
    {kpi.inPeriod !== null && kpi.inPeriod !== undefined ? (
      <span className="text-xs font-medium tabular-nums text-emerald-700 dark:text-emerald-400">+<AnimatedNumber value={kpi.inPeriod} /> this period</span>
    ) : (
      <span className="text-xs text-slate-600 dark:text-slate-400">All time</span>
    )}
    {kpi.trend && <div className="mt-2"><Sparkline values={kpi.trend} /></div>}
  </div>
);

const ChartCard = ({ title, summary, children, delay = 0 }) => (
  <section className={`${cardClass} p-5 flex flex-col gap-3 min-w-0 insights-rise`} style={{ animationDelay: `${delay}ms` }}>
    <div className="flex items-baseline justify-between gap-3">
      <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">{title}</h3>
      <span className="text-sm tabular-nums text-slate-600 dark:text-slate-400">{summary}</span>
    </div>
    {children}
  </section>
);

const sumOf = (points) => points.reduce((sum, p) => sum + p.value, 0);

// Registrations of every type added up per bucket, for one clean line.
const combine = (series) =>
  (series[0]?.points ?? []).map((p, i) => ({ start: p.start, value: series.reduce((sum, s) => sum + (s.points[i]?.value ?? 0), 0) }));

const OpenNow = ({ items }) => {
  const [expanded, setExpanded] = useState(false);
  const shown = expanded ? items : items.slice(0, OPEN_PREVIEW);
  return (
    <section className={`${cardClass} min-w-0 insights-rise`} style={{ animationDelay: "420ms" }}>
      <div className="px-5 py-4 flex items-baseline justify-between gap-3 border-b border-slate-200 dark:border-slate-800">
        <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">Open right now</h3>
        <span className="text-sm tabular-nums text-slate-600 dark:text-slate-400">{items.length}</span>
      </div>
      {items.length === 0 ? (
        <p className="px-5 py-6 text-sm text-slate-600 dark:text-slate-400">Nothing is open at the moment.</p>
      ) : (
        <ul>
          {shown.map((item, i) => (
            <li key={`${item.type}-${item.name}-${i}`} className="px-5 py-3 flex items-center gap-3 border-b border-slate-200 dark:border-slate-800 last:border-b-0">
              <span className="w-8 h-8 shrink-0 inline-flex items-center justify-center rounded-lg bg-[#4B98C8]/10 text-[#2C6F99] dark:text-[#7DBBE0]">
                {createElement(OPEN_ICONS[item.type] || FiCalendar, { className: "w-4 h-4", "aria-hidden": true })}
              </span>
              <span className="min-w-0 flex-1">
                <span className="block text-sm font-medium text-slate-900 dark:text-slate-100 truncate">{item.name}</span>
                <span className="block text-xs text-slate-600 dark:text-slate-400 truncate">{item.detail}</span>
              </span>
              <span className="text-sm tabular-nums text-slate-700 dark:text-slate-300 whitespace-nowrap">
                {item.registrations.toLocaleString()} <span className="hidden sm:inline">{item.registrations === 1 ? "registration" : "registrations"}</span>
              </span>
            </li>
          ))}
        </ul>
      )}
      {items.length > OPEN_PREVIEW && (
        <button
          type="button"
          onClick={() => setExpanded((v) => !v)}
          className="w-full px-5 py-3 text-sm font-medium text-[#2C6F99] dark:text-[#7DBBE0] border-t border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800/60 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-[#4B98C8]/50"
        >
          {expanded ? "Show less" : `Show all ${items.length}`}
        </button>
      )}
    </section>
  );
};

// Global overview (style A): number cards with trend lines, two charts, and what is open right now.
const OverviewPanel = ({ data }) => {
  const members = data.charts.find((c) => c.key === "newMembers");
  const registrations = data.charts.find((c) => c.key === "registrations");
  const growth = data.charts.find((c) => c.key === "userGrowth");
  const growthPoints = growth?.series[0].points ?? [];
  const registrationPoints = registrations ? combine(registrations.series) : [];

  return (
    <div className="flex flex-col gap-4">
      <div className="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-5 gap-3">
        {data.kpis.map((kpi, i) => (
          <KpiCard key={kpi.key} kpi={kpi} index={i} />
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {members && (
          <ChartCard title={members.title} summary={`${sumOf(members.series[0].points).toLocaleString()} this period`} delay={240}>
            <AreaChart points={members.series[0].points} label={members.title} />
          </ChartCard>
        )}
        {registrations && (
          <ChartCard title={registrations.title} summary={`${sumOf(registrationPoints).toLocaleString()} this period`} delay={300}>
            <AreaChart points={registrationPoints} label={registrations.title} />
            <dl className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs">
              {registrations.series.map((s) => (
                <div key={s.key} className="min-w-0">
                  <dt className="text-slate-600 dark:text-slate-400 truncate">{s.label}</dt>
                  <dd className="font-medium tabular-nums text-slate-900 dark:text-slate-100">{sumOf(s.points).toLocaleString()}</dd>
                </div>
              ))}
            </dl>
          </ChartCard>
        )}
      </div>

      {growth && (
        <ChartCard
          delay={360}
          title={growth.title}
          summary={`${(growthPoints[growthPoints.length - 1]?.value ?? 0).toLocaleString()} members`}
        >
          <AreaChart points={growthPoints} label={growth.title} width={1200} height={220} />
        </ChartCard>
      )}

      <OpenNow items={data.openNow} />
    </div>
  );
};

export default OverviewPanel;
