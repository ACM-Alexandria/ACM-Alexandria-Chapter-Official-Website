import React, { useState, useRef, useEffect } from "react";
import { FiTrendingUp } from "react-icons/fi";
import { BRAND, BRAND_DARK, buildSpline } from "./constants";

/**
 * User Growth Area Chart
 * Crosshair tracking · floating tooltip · gradient fill
 */
const UserGrowthChart = ({ data = [] }) => {
  const svgRef = useRef(null);
  const wrapRef = useRef(null);
  const [active, setActive] = useState(null);
  // Draw at the real width so text stays the same size instead of scaling with the panel
  const [W, setW] = useState(640);
  const H = 290;
  const hasData = data.length >= 2;

  useEffect(() => {
    const el = wrapRef.current;
    if (!el) return undefined;
    const update = () => setW(Math.max(300, Math.round(el.clientWidth)));
    update();
    const observer = new ResizeObserver(update);
    observer.observe(el);
    return () => observer.disconnect();
  }, [hasData]);

  const pad = { t: 28, r: 24, b: 42, l: 50 };
  const iw = W - pad.l - pad.r;
  const ih = H - pad.t - pad.b;

  if (data.length<2) {
    return (
      <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200/80 dark:border-slate-700 p-12 text-center text-slate-600 dark:text-slate-300 text-sm font-medium">
        No growth data available
      </div>
    );
  }

  const maxC = Math.max(...data.map((d) => d.count), 5);

  const pts = data.map((d, i) => ({
    x: pad.l + (i / Math.max(data.length - 1, 1)) * iw,
    y: H - pad.b - (d.count / maxC) * ih,
    ...d,
  }));

  const linePath = buildSpline(pts);
  const areaPath = `${linePath} L ${pts[pts.length - 1].x} ${H - pad.b} L ${pts[0].x} ${H - pad.b} Z`;

  const ySteps = 4;
  const yTicks = Array.from({ length: ySteps + 1 }, (_, i) => ({
    v: Math.round((i / ySteps) * maxC),
    y: H - pad.b - (i / ySteps) * ih,
  }));

  // About one label per 120px so dates never overlap
  const maxLabels = Math.max(2, Math.floor(iw / 120));
  const xStep = Math.max(1, Math.ceil(pts.length / maxLabels));
  const xLabels = pts.filter((_, i) => i % xStep === 0 || i === pts.length - 1);

  const handleMove = (e) => {
    const svg = svgRef.current;
    if (!svg) return;
    const rect = svg.getBoundingClientRect();
    const mx = ((e.clientX - rect.left) / rect.width) * W;
    let closest = pts[0],
      minD = Infinity;
    for (const p of pts) {
      const d = Math.abs(p.x - mx);
      if (d < minD) {
        minD = d;
        closest = p;
      }
    }
    setActive(closest);
  };

  return (
    <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200/80 dark:border-slate-700 p-5 dark:shadow-slate-950/40 transition-shadow duration-300">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-3">
          <div
            className="w-9 h-9 rounded-lg flex items-center justify-center"
            style={{ backgroundColor: `${BRAND}12` }}
          >
            <FiTrendingUp className="w-[18px] h-[18px]" style={{ color: BRAND }} />
          </div>
          <div>
            <h3 className="text-sm font-medium text-slate-800 dark:text-slate-100">User Growth</h3>
            <p className="text-xs text-slate-600 dark:text-slate-400">Cumulative registrations over time</p>
          </div>
        </div>
        {active && (
          <div className="text-right">
            <p className="text-lg font-semibold text-slate-800">{active.count}</p>
            <p className="text-xs font-semibold text-slate-600 dark:text-slate-400">
              {active.date}
            </p>
          </div>
        )}
      </div>

      <div className="relative" ref={wrapRef}>
        <svg
          ref={svgRef}
          viewBox={`0 0 ${W} ${H}`}
          className="w-full h-auto cursor-crosshair"
          onMouseMove={handleMove}
          onMouseLeave={() => setActive(null)}
        >
          <defs>
            <linearGradient id="ugFill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={BRAND} stopOpacity="0.18" />
              <stop offset="100%" stopColor={BRAND} stopOpacity="0.01" />
            </linearGradient>
            <linearGradient id="ugLine" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0%" stopColor={BRAND} />
              <stop offset="100%" stopColor={BRAND_DARK} />
            </linearGradient>
          </defs>

          {/* Y grid */}
          {yTicks.map((t, i) => (
            <g key={i}>
              <line x1={pad.l} y1={t.y} x2={W - pad.r} y2={t.y} stroke="#e2e8f0" strokeWidth="1" />
              <text x={pad.l - 10} y={t.y + 4} textAnchor="end" fill="#94a3b8" className="text-xs" style={{ fontWeight: 600 }}>
                {t.v}
              </text>
            </g>
          ))}

          {/* Area fill + spline */}
          <path d={areaPath} fill="url(#ugFill)" />
          <path d={linePath} fill="none" stroke="url(#ugLine)" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />


          {/* Crosshair + active dot */}
          {active && (
            <>
              <line x1={active.x} y1={pad.t} x2={active.x} y2={H - pad.b} stroke={BRAND} strokeWidth="1" strokeDasharray="4 3" opacity="0.35" />
              <circle cx={active.x} cy={active.y} r="5.5" fill="white" stroke={BRAND} strokeWidth="2.5" />
              <circle cx={active.x} cy={active.y} r="2.5" fill={BRAND} />
            </>
          )}

          {/* X labels */}
          {xLabels.map((t, i) => (
            <text key={i} x={t.x} y={H - 14} textAnchor="middle" fill="#94a3b8" className="text-xs" style={{ fontWeight: 600 }}>
              {t.date}
            </text>
          ))}
        </svg>
      </div>
    </div>
  );
};

export default UserGrowthChart;
