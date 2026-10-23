import React from "react";

const BRAND = "#4B98C8";
const WIDTH = 600;
const HEIGHT = 200;
const PAD_X = 8;
const AXIS = 24;

const formatDay = (iso) => new Date(`${iso}T00:00:00`).toLocaleDateString("en-GB", { day: "numeric", month: "short" });

const AxisLabels = ({ points, width = WIDTH, height = HEIGHT }) =>
  points.length > 0 && (
    <>
      <line x1={PAD_X} x2={width - PAD_X} y1={height - AXIS} y2={height - AXIS} className="stroke-slate-200 dark:stroke-slate-700" />
      <text x={PAD_X} y={height - 6} className="fill-slate-600 dark:fill-slate-400 text-[12px]">{formatDay(points[0].start)}</text>
      <text x={width - PAD_X} y={height - 6} textAnchor="end" className="fill-slate-600 dark:fill-slate-400 text-[12px]">
        {formatDay(points[points.length - 1].start)}
      </text>
    </>
  );

const Empty = () => <p className="py-12 text-center text-sm text-slate-600 dark:text-slate-400">No activity in this period.</p>;

// Small trend line for a number card.
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

// One series over time as a filled line. wide charts pass a wider viewBox so lines keep their shape.
export const AreaChart = ({ points, label, height = HEIGHT, width = WIDTH }) => {
  if (!points.some((p) => p.value > 0)) return <Empty />;
  const max = Math.max(1, ...points.map((p) => p.value));
  const plot = height - AXIS - 8;
  const step = points.length > 1 ? (width - PAD_X * 2) / (points.length - 1) : 0;
  const x = (i) => PAD_X + i * step;
  const y = (v) => height - AXIS - (v / max) * plot;
  const line = points.map((p, i) => `${i ? "L" : "M"}${x(i).toFixed(1)},${y(p.value).toFixed(1)}`).join(" ");
  const area = `${line} L${x(points.length - 1).toFixed(1)},${height - AXIS} L${PAD_X},${height - AXIS} Z`;
  const last = points[points.length - 1]?.value ?? 0;

  return (
    <svg viewBox={`0 0 ${width} ${height}`} className="w-full h-auto" role="img" aria-label={`${label}: ${last} at the end of the period`}>
      <path d={area} className="insights-fill" fill={BRAND} fillOpacity="0.12" />
      <path d={line} pathLength="1" className="insights-draw" fill="none" stroke={BRAND} strokeWidth="2" strokeLinejoin="round" />
      <AxisLabels points={points} width={width} height={height} />
    </svg>
  );
};

// Several series stacked per bucket, as bars. colors line up with series.
export const StackedBars = ({ series, colors, label }) => {
  const points = series[0]?.points ?? [];
  const totals = points.map((_, i) => series.reduce((sum, s) => sum + (s.points[i]?.value ?? 0), 0));
  if (!totals.some((t) => t > 0)) return <Empty />;

  const max = Math.max(1, ...totals);
  const plot = HEIGHT - AXIS - 8;
  const slot = (WIDTH - PAD_X * 2) / points.length;
  const barWidth = Math.max(2, Math.min(32, slot * 0.7));

  return (
    <svg viewBox={`0 0 ${WIDTH} ${HEIGHT}`} className="w-full h-auto" role="img" aria-label={`${label}: ${totals.reduce((a, b) => a + b, 0)} in this period`}>
      {points.map((p, i) => {
        let top = HEIGHT - AXIS;
        return series.map((s, si) => {
          const value = s.points[i]?.value ?? 0;
          const h = (value / max) * plot;
          top -= h;
          return h > 0 ? (
            <rect
              key={`${i}-${s.key}`}
              x={PAD_X + i * slot + (slot - barWidth) / 2}
              y={top}
              width={barWidth}
              height={h}
              rx="2"
              fill={colors[si % colors.length]}
              className="insights-bar"
              style={{ animationDelay: `${Math.min(i * 25, 700)}ms` }}
            >
              <title>{`${s.label}, ${formatDay(p.start)}: ${value}`}</title>
            </rect>
          ) : null;
        });
      })}
      <AxisLabels points={points} />
    </svg>
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
