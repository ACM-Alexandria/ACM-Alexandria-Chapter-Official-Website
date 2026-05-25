import React, { useState } from "react";
import { FiBarChart2 } from "react-icons/fi";
import { BRAND_DARK, CHART_COLORS } from "./constants";

/**
 * Batch Pie Chart
 * Donut ring with hover-synced legend · center HUD
 */
const BatchChart = ({ data = {} }) => {
  const [hovered, setHovered] = useState(null);

  const list = Object.entries(data)
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => a.name.localeCompare(b.name));

  const total = list.reduce((s, d) => s + d.count, 0);

  if (!total) {
    return (
      <div className="bg-white rounded-2xl border border-slate-200/80 p-12 text-center text-slate-400 text-sm font-medium shadow-sm">
        No batch data available
      </div>
    );
  }

  const r = 52, sw = 13, cx = 80, cy = 80;
  const circ = 2 * Math.PI * r;

  let offset = 0;
  const arcs = list.map((d, i) => {
    const frac = d.count / total;
    const dash = circ * frac;
    const gap = circ - dash;
    const rot = offset;
    offset += frac * 360;
    return { ...d, frac, dash, gap, rot, color: CHART_COLORS[(i + 2) % CHART_COLORS.length] };
  });

  const focus = hovered || { name: "Total", count: total, frac: 1, color: BRAND_DARK };

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-sm hover:shadow-md transition-shadow duration-300 flex flex-col h-full">
      <div className="flex items-center gap-3 mb-4">
        <div
          className="w-9 h-9 rounded-xl flex items-center justify-center"
          style={{ backgroundColor: `${BRAND_DARK}12` }}
        >
          <FiBarChart2 className="w-[18px] h-[18px]" style={{ color: BRAND_DARK }} />
        </div>
        <div>
          <h3 className="text-sm font-bold text-slate-800">Batches</h3>
          <p className="text-[11px] text-slate-400">Members by academic year</p>
        </div>
      </div>

      <div className="flex-1 flex flex-col items-center gap-4">
        {/* Donut */}
        <div className="relative">
          <svg viewBox="0 0 160 160" className="w-40 h-40">
            {arcs.map((a, i) => {
              const isActive = hovered?.name === a.name;
              return (
                <circle
                  key={i}
                  cx={cx}
                  cy={cy}
                  r={r}
                  fill="none"
                  stroke={a.color}
                  strokeWidth={isActive ? sw + 4 : sw}
                  strokeDasharray={`${a.dash} ${a.gap}`}
                  strokeLinecap="round"
                  className="cursor-pointer transition-all duration-300"
                  style={{
                    transform: `rotate(${a.rot - 90}deg)`,
                    transformOrigin: `${cx}px ${cy}px`,
                    opacity: hovered && !isActive ? 0.25 : 1,
                  }}
                  onMouseEnter={() => setHovered(a)}
                  onMouseLeave={() => setHovered(null)}
                />
              );
            })}
          </svg>
          {/* Center HUD */}
          <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
            <span className="text-2xl font-extrabold text-slate-800">{focus.count}</span>
            <span className="text-[9px] font-bold text-slate-400 uppercase tracking-widest mt-0.5 max-w-[80px] text-center truncate">
              {focus.name}
            </span>
            {focus.frac < 1 && (
              <span
                className="text-[10px] font-bold mt-1 px-2 py-0.5 rounded-md"
                style={{ color: focus.color, backgroundColor: `${focus.color}12` }}
              >
                {(focus.frac * 100).toFixed(1)}%
              </span>
            )}
          </div>
        </div>

        {/* Legend */}
        <div className="w-full flex flex-col gap-1 max-h-36 overflow-y-auto">
          {arcs.map((a, i) => (
            <div
              key={i}
              className={`flex items-center justify-between px-3 py-1.5 rounded-lg cursor-pointer transition-all duration-200 ${
                hovered?.name === a.name ? "bg-slate-50" : "hover:bg-slate-50/60"
              }`}
              onMouseEnter={() => setHovered(a)}
              onMouseLeave={() => setHovered(null)}
            >
              <div className="flex items-center gap-2 min-w-0">
                <div className="w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: a.color }} />
                <span className="text-xs font-semibold text-slate-600 truncate">{a.name}</span>
              </div>
              <span className="text-xs font-bold text-slate-500 ml-2 shrink-0">{a.count}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default BatchChart;
