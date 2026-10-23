import React, { useState } from "react";

const BRAND = "#4B98C8";
const WIDTH = 600;
const HEIGHT = 220;
const PAD_L = 44; // room for the y axis numbers
const PAD_R = 12;
const PAD_T = 12;
const PAD_B = 30; // room for the x axis dates
const MAX_X_TICKS = 6;

const formatDay = (iso) => new Date(`${iso}T00:00:00`).toLocaleDateString("en-GB", { day: "numeric", month: "short" });
const formatDayLong = (iso) =>
  new Date(`${iso}T00:00:00`).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" });
const clamp = (n, lo, hi) => Math.min(hi, Math.max(lo, n));

// The top of the y axis, rounded up to a clean number (47 becomes 50, 260 becomes 300)
const niceTop = (value) => {
  if (value <= 0) return 1;
  const magnitude = 10 ** Math.floor(Math.log10(value));
  return Math.ceil(value / magnitude) * magnitude;
};

// Which buckets get a date label, spread evenly and always including the first and last
const xTicks = (count) => {
  if (count <= 1) return [0];
  const n = Math.min(count, MAX_X_TICKS);
  return [...new Set(Array.from({ length: n }, (_, k) => Math.round((k * (count - 1)) / (n - 1))))];
};

const Empty = () => <p className="py-12 text-center text-sm text-slate-600 dark:text-slate-400">No activity in this period.</p>;

// Gridlines, y axis numbers and x axis dates.
const Axes = ({ top, width, height, points, xOf, yOf }) => (
  <>
    {[0, top / 2, top].map((t) => (
      <g key={t}>
        <line
          x1={PAD_L}
          x2={width - PAD_R}
          y1={yOf(t)}
          y2={yOf(t)}
          strokeDasharray={t === 0 ? undefined : "3 4"}
          className={t === 0 ? "stroke-slate-300 dark:stroke-slate-600" : "stroke-slate-200 dark:stroke-slate-800"}
        />
        <text x={PAD_L - 8} y={yOf(t) + 4} textAnchor="end" className="fill-slate-600 dark:fill-slate-400 text-[12px] tabular-nums">
          {Math.round(t).toLocaleString()}
        </text>
      </g>
    ))}
    {xTicks(points.length).map((i) => (
      <text
        key={points[i].start}
        x={xOf(i)}
        y={height - 8}
        textAnchor={i === 0 ? "start" : i === points.length - 1 ? "end" : "middle"}
        className="fill-slate-600 dark:fill-slate-400 text-[12px]"
      >
        {formatDay(points[i].start)}
      </text>
    ))}
  </>
);

// A small dark bubble that follows the hovered point.
const Tooltip = ({ leftPct, topPct, children }) => {
  const align = leftPct < 18 ? "0%" : leftPct > 82 ? "-100%" : "-50%";
  return (
    <div
      role="tooltip"
      className="pointer-events-none absolute z-10 min-w-[8rem] rounded-lg bg-slate-900 dark:bg-slate-100 px-3 py-2 text-xs text-white dark:text-slate-900 tabular-nums"
      style={{ left: `${leftPct}%`, top: `${topPct}%`, transform: `translate(${align}, calc(-100% - 10px))` }}
    >
      {children}
    </div>
  );
};

// Trend line for a number card.
export const Sparkline = ({ values }) => {
  if (!values || values.length < 2) return null;
  const max = Math.max(1, ...values);
  const step = 100 / (values.length - 1);
  const points = values.map((v, i) => `${(i * step).toFixed(2)},${(22 - (v / max) * 20).toFixed(2)}`).join(" ");
  return (
    <svg viewBox="0 0 100 24" preserveAspectRatio="none" className="w-full h-7" aria-hidden="true">
      <polyline points={points} pathLength="1" className="insights-draw" fill="none" stroke={BRAND} strokeWidth="1.5" vectorEffect="non-scaling-stroke" strokeLinejoin="round" />
    </svg>
  );
};

// One series over time as a filled line. Hover a point to see its date and value.
export const AreaChart = ({ points, label, height = HEIGHT, width = WIDTH }) => {
  const [hover, setHover] = useState(null);
  if (!points.some((p) => p.value > 0)) return <Empty />;

  const values = points.map((p) => p.value);
  const top = niceTop(Math.max(...values));
  const plotW = width - PAD_L - PAD_R;
  const plotH = height - PAD_T - PAD_B;
  const xOf = (i) => (points.length > 1 ? PAD_L + (i * plotW) / (points.length - 1) : PAD_L + plotW / 2);
  const yOf = (v) => PAD_T + plotH - (v / top) * plotH;
  const line = points.map((p, i) => `${i ? "L" : "M"}${xOf(i).toFixed(1)},${yOf(p.value).toFixed(1)}`).join(" ");
  const area = `${line} L${xOf(points.length - 1).toFixed(1)},${PAD_T + plotH} L${xOf(0).toFixed(1)},${PAD_T + plotH} Z`;

  const onPointer = (e) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const px = ((e.clientX - rect.left) / rect.width) * width;
    const i = points.length > 1 ? Math.round(((px - PAD_L) / plotW) * (points.length - 1)) : 0;
    setHover(clamp(i, 0, points.length - 1));
  };

  const total = values.reduce((a, b) => a + b, 0);
  return (
    <div className="relative">
      <svg
        viewBox={`0 0 ${width} ${height}`}
        className="w-full h-auto touch-pan-y"
        role="img"
        aria-label={`${label}: ${total} in this period`}
        onPointerMove={onPointer}
        onPointerDown={onPointer}
        onPointerLeave={() => setHover(null)}
      >
        <Axes top={top} width={width} height={height} points={points} xOf={xOf} yOf={yOf} />
        <path d={area} className="insights-fill" fill={BRAND} fillOpacity="0.12" />
        <path d={line} pathLength="1" className="insights-draw" fill="none" stroke={BRAND} strokeWidth="2" strokeLinejoin="round" />
        {hover !== null && (
          <g>
            <line x1={xOf(hover)} x2={xOf(hover)} y1={PAD_T} y2={PAD_T + plotH} className="stroke-slate-400 dark:stroke-slate-500" strokeDasharray="2 3" />
            <circle cx={xOf(hover)} cy={yOf(values[hover])} r="4.5" fill={BRAND} className="stroke-white dark:stroke-slate-900" strokeWidth="2" />
          </g>
        )}
      </svg>
      {hover !== null && (
        <Tooltip leftPct={(xOf(hover) / width) * 100} topPct={(yOf(values[hover]) / height) * 100}>
          <div className="text-slate-300 dark:text-slate-600">{formatDayLong(points[hover].start)}</div>
          <div className="text-sm font-semibold">{values[hover].toLocaleString()} {label.toLowerCase()}</div>
        </Tooltip>
      )}
    </div>
  );
};

// Several series stacked per bucket, as bars. Hover a column to see each part and the total.
export const StackedBars = ({ series, colors, label, height = HEIGHT, width = WIDTH }) => {
  const [hover, setHover] = useState(null);
  const points = series[0]?.points ?? [];
  const totals = points.map((_, i) => series.reduce((sum, s) => sum + (s.points[i]?.value ?? 0), 0));
  if (!totals.some((t) => t > 0)) return <Empty />;

  const top = niceTop(Math.max(...totals));
  const plotW = width - PAD_L - PAD_R;
  const plotH = height - PAD_T - PAD_B;
  const slot = plotW / points.length;
  const barWidth = Math.max(2, Math.min(32, slot * 0.7));
  const xCenter = (i) => PAD_L + slot * i + slot / 2;
  const yOf = (v) => PAD_T + plotH - (v / top) * plotH;

  const onPointer = (e) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const px = ((e.clientX - rect.left) / rect.width) * width;
    setHover(clamp(Math.floor((px - PAD_L) / slot), 0, points.length - 1));
  };

  const grandTotal = totals.reduce((a, b) => a + b, 0);
  return (
    <div className="relative">
      <svg
        viewBox={`0 0 ${width} ${height}`}
        className="w-full h-auto touch-pan-y"
        role="img"
        aria-label={`${label}: ${grandTotal} in this period`}
        onPointerMove={onPointer}
        onPointerDown={onPointer}
        onPointerLeave={() => setHover(null)}
      >
        <Axes top={top} width={width} height={height} points={points} xOf={xCenter} yOf={yOf} />
        {hover !== null && (
          <rect x={PAD_L + slot * hover} y={PAD_T} width={slot} height={plotH} className="fill-slate-900/5 dark:fill-white/5" />
        )}
        {points.map((p, i) => {
          let stackTop = PAD_T + plotH;
          return series.map((s, si) => {
            const value = s.points[i]?.value ?? 0;
            const h = (value / top) * plotH;
            stackTop -= h;
            return h > 0 ? (
              <rect
                key={`${p.start}-${s.key}`}
                x={xCenter(i) - barWidth / 2}
                y={stackTop}
                width={barWidth}
                height={h}
                rx="2"
                fill={colors[si % colors.length]}
                className="insights-bar"
                style={{ animationDelay: `${Math.min(i * 25, 700)}ms` }}
              />
            ) : null;
          });
        })}
      </svg>
      {hover !== null && totals[hover] > 0 && (
        <Tooltip leftPct={(xCenter(hover) / width) * 100} topPct={(yOf(totals[hover]) / height) * 100}>
          <div className="text-slate-300 dark:text-slate-600">{formatDayLong(points[hover].start)}</div>
          {series.map((s, si) => (
            <div key={s.key} className="flex items-center justify-between gap-4">
              <span className="inline-flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-sm" style={{ backgroundColor: colors[si % colors.length] }} aria-hidden="true" />
                {s.label}
              </span>
              <span className="font-semibold">{(s.points[hover]?.value ?? 0).toLocaleString()}</span>
            </div>
          ))}
          <div className="mt-1 border-t border-white/20 dark:border-slate-900/20 pt-1 flex justify-between gap-4 font-semibold">
            <span>Total</span>
            <span>{totals[hover].toLocaleString()}</span>
          </div>
        </Tooltip>
      )}
    </div>
  );
};

// Coloured key under a multi-series chart.
export const Legend = ({ series, colors }) => (
  <ul className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-600 dark:text-slate-400">
    {series.map((s, i) => (
      <li key={s.key} className="inline-flex items-center gap-1.5">
        <span className="w-2.5 h-2.5 rounded-sm" style={{ backgroundColor: colors[i % colors.length] }} aria-hidden="true" />
        {s.label}
      </li>
    ))}
  </ul>
);
