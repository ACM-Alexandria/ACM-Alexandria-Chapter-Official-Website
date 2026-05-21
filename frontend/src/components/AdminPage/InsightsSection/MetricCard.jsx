import React from "react";

const MetricCard = ({ label, value, subText, icon: Icon, color }) => (
  <div className="bg-white rounded-2xl border border-slate-200/80 p-4 flex items-center gap-4 shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300 cursor-default group">
    <div
      className="w-11 h-11 rounded-xl flex items-center justify-center shrink-0 transition-transform duration-300 group-hover:scale-110"
      style={{ backgroundColor: `${color}14` }}
    >
      <Icon className="w-5 h-5" style={{ color }} />
    </div>
    <div className="min-w-0">
      <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest leading-none mb-1">
        {label}
      </p>
      <p className="text-2xl font-extrabold text-slate-800 leading-none tracking-tight">
        {value}
      </p>
      {subText && (
        <p className="text-[10px] text-slate-400 font-medium mt-0.5 truncate">
          {subText}
        </p>
      )}
    </div>
  </div>
);

export default MetricCard;
