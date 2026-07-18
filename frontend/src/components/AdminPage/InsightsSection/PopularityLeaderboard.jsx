import React, { useState, useEffect } from "react";
import { BRAND, BRAND_DARK } from "./constants";

/**
 * Popularity Leaderboard
 * Ranked badges · animated progress bars
 */
const PopularityLeaderboard = ({ title, icon: Icon, data = [], accentColor = BRAND }) => {
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const t = setTimeout(() => setReady(true), 250);
    return () => clearTimeout(t);
  }, []);

  const maxR = data.length ? Math.max(...data.map((d) => d.count), 1) : 1;

  const rankBadge = (i) => {
    if (i === 0) return { bg: "bg-amber-50", text: "text-amber-700", border: "border-amber-200" };
    if (i === 1) return { bg: "bg-slate-50", text: "text-slate-600", border: "border-slate-200" };
    if (i === 2) return { bg: "bg-orange-50", text: "text-orange-700", border: "border-orange-200" };
    return { bg: "bg-slate-50", text: "text-slate-400", border: "border-slate-100" };
  };

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-sm hover:shadow-md transition-shadow duration-300 flex flex-col h-full">
      <div className="flex items-center gap-3 mb-4">
        <div
          className="w-9 h-9 rounded-xl flex items-center justify-center"
          style={{ backgroundColor: `${accentColor}12` }}
        >
          <Icon className="w-[18px] h-[18px]" style={{ color: accentColor }} />
        </div>
        <div>
          <h3 className="text-sm font-bold text-slate-800">{title}</h3>
          <p className="text-[11px] text-slate-400">Ranked by registrations</p>
        </div>
      </div>

      <div className="flex-1 flex flex-col gap-3.5 justify-center">
        {data.length === 0 ? (
          <p className="text-center text-slate-400 text-sm font-medium py-8">No registrations yet</p>
        ) : (
          data.slice(0, 5).map((item, i) => {
            const pct = (item.count / maxR) * 100;
            const badge = rankBadge(i);
            return (
              <div key={item.id || i}>
                <div className="flex items-center gap-2.5 mb-1">
                  <span
                    className={`w-5 h-5 rounded-md flex items-center justify-center text-[10px] font-extrabold shrink-0 border ${badge.bg} ${badge.text} ${badge.border}`}
                  >
                    {i + 1}
                  </span>
                  <span className="text-[13px] font-semibold text-slate-700 truncate flex-1">
                    {item.name}
                  </span>
                  <span className="text-[11px] font-bold text-slate-500 shrink-0">{item.count}</span>
                </div>
                <div className="w-full h-[7px] bg-slate-100 rounded-full overflow-hidden">
                  <div
                    className="h-full rounded-full transition-all duration-1000 ease-out"
                    style={{
                      width: ready ? `${pct}%` : "0%",
                      background: `linear-gradient(to right, ${BRAND}, ${BRAND_DARK})`,
                    }}
                  />
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default PopularityLeaderboard;
