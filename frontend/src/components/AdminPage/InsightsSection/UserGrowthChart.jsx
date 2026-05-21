import React, { useState, useRef, useCallback } from "react";
import { FiTrendingUp } from "react-icons/fi";
import { BRAND, BRAND_DARK, buildSpline } from "./constants";

/**
 * User Growth Area Chart
 * Crosshair tracking · floating tooltip · gradient fill
 */
const UserGrowthChart = ({ data = [] }) => {
  const svgRef = useRef(null);
  const [active, setActive] = useState(null);

  const W = 640, H = 290;
  const pad = { t: 28, r: 24, b: 42, l: 50 };
  const iw = W - pad.l - pad.r;
  const ih = H - pad.t - pad.b;

  if (!data.length) {
    return (
      <div className="bg-white rounded-2xl border border-slate-200/80 p-12 text-center text-slate-400 text-sm font-medium shadow-sm">
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

  const xStep = Math.max(1, Math.floor(pts.length / 6));
  const xLabels = pts.filter((_, i) => i % xStep === 0 || i === pts.length - 1);

  const handleMove = useCallback(
    (e) => {
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
    },
    [pts],
  );

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-sm hover:shadow-md transition-shadow duration-300">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-3">
          <div
            className="w-9 h-9 rounded-xl flex items-center justify-center"
            style={{ backgroundColor: `${BRAND}12` }}
          >
            <FiTrendingUp className="w-[18px] h-[18px]" style={{ color: BRAND }} />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-800">User Growth</h3>
            <p className="text-[11px] text-slate-400">Cumulative registrations over time</p>
          </div>
        </div>
        {active && (
          <div className="text-right">
            <p className="text-lg font-extrabold text-slate-800">{active.count}</p>
            <p className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
              {active.date}
            </p>
          </div>
        )}
      </div>

      <div className="relative">
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
              <text x={pad.l - 10} y={t.y + 4} textAnchor="end" fill="#94a3b8" className="text-[10px]" style={{ fontWeight: 600 }}>
                {t.v}
              </text>
            </g>
          ))}

          {/* Area fill + spline */}
          <path d={areaPath} fill="url(#ugFill)" />
          <path d={linePath} fill="none" stroke="url(#ugLine)" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />

          {/* Dots */}
          {pts.map((p, i) => (
            <circle key={i} cx={p.x} cy={p.y} r="3" fill="white" stroke={BRAND} strokeWidth="2" />
          ))}

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
            <text key={i} x={t.x} y={H - 14} textAnchor="middle" fill="#94a3b8" className="text-[9px]" style={{ fontWeight: 600 }}>
              {t.date}
            </text>
          ))}
        </svg>
      </div>
    </div>
  );
};

export default UserGrowthChart;
