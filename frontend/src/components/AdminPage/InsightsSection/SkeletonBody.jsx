import React from "react";
import { shimmerClass } from "./constants";

/* ─── Skeleton building blocks ─── */
const SkeletonCard = () => (
  <div className={`bg-slate-100/80 rounded-2xl h-[104px] ${shimmerClass}`} />
);

const SkeletonChartWide = ({ h = "h-[340px]" }) => (
  <div className={`bg-white rounded-2xl border border-slate-200/60 p-5 ${h}`}>
    <div className="flex items-center gap-3 mb-5">
      <div className={`w-9 h-9 rounded-xl bg-slate-100 ${shimmerClass}`} />
      <div className="space-y-2 flex-1">
        <div className={`h-3.5 w-28 rounded bg-slate-100 ${shimmerClass}`} />
        <div className={`h-2.5 w-40 rounded bg-slate-50 ${shimmerClass}`} />
      </div>
    </div>
    <div className="flex items-end gap-3 h-[60%] pt-4 px-2">
      {Array.from({ length: 10 }).map((_, i) => (
        <div
          key={i}
          className={`flex-1 rounded-t-lg bg-slate-100/80 ${shimmerClass}`}
          style={{ height: `${25 + Math.sin(i * 0.9) * 35 + 35}%` }}
        />
      ))}
    </div>
  </div>
);

const SkeletonDonut = () => (
  <div className="bg-white rounded-2xl border border-slate-200/60 p-5 h-[380px]">
    <div className="flex items-center gap-3 mb-5">
      <div className={`w-9 h-9 rounded-xl bg-slate-100 ${shimmerClass}`} />
      <div className="space-y-2 flex-1">
        <div className={`h-3.5 w-24 rounded bg-slate-100 ${shimmerClass}`} />
        <div className={`h-2.5 w-36 rounded bg-slate-50 ${shimmerClass}`} />
      </div>
    </div>
    <div className="flex flex-col items-center mt-4 gap-5">
      <div className={`w-36 h-36 rounded-full border-[14px] border-slate-100 ${shimmerClass}`} />
      <div className="w-full space-y-2">
        {[1, 2, 3].map((i) => (
          <div key={i} className={`h-6 rounded-lg bg-slate-50 ${shimmerClass}`} />
        ))}
      </div>
    </div>
  </div>
);

const SkeletonLeaderboard = () => (
  <div className="bg-white rounded-2xl border border-slate-200/60 p-5 h-[280px]">
    <div className="flex items-center gap-3 mb-5">
      <div className={`w-9 h-9 rounded-xl bg-slate-100 ${shimmerClass}`} />
      <div className="space-y-2 flex-1">
        <div className={`h-3.5 w-24 rounded bg-slate-100 ${shimmerClass}`} />
        <div className={`h-2.5 w-32 rounded bg-slate-50 ${shimmerClass}`} />
      </div>
    </div>
    <div className="space-y-5 mt-6">
      {[80, 60, 40].map((w, i) => (
        <div key={i} className="space-y-2">
          <div className="flex items-center gap-2.5">
            <div className={`w-5 h-5 rounded-md bg-slate-100 ${shimmerClass}`} />
            <div className={`h-3 rounded bg-slate-100 flex-1 ${shimmerClass}`} />
            <div className={`h-3 w-8 rounded bg-slate-100 ${shimmerClass}`} />
          </div>
          <div
            className={`h-[7px] rounded-full bg-slate-100 ${shimmerClass}`}
            style={{ width: `${w}%` }}
          />
        </div>
      ))}
    </div>
  </div>
);

/* ─── Full-page skeleton matching the new row-based layout ─── */
const SkeletonBody = () => (
  <div className="space-y-6 animate-[fadeIn_0.3s_ease]">
    {/* Row 1: Metric cards */}
    <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
      {Array.from({ length: 10 }).map((_, i) => (
        <SkeletonCard key={i} />
      ))}
    </div>

    {/* Row 2: User Growth (full width) */}
    <SkeletonChartWide />

    {/* Row 3: Two pie charts side by side */}
    <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
      <SkeletonDonut />
      <SkeletonDonut />
    </div>

    {/* Row 4: Two leaderboards side by side */}
    <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
      <SkeletonLeaderboard />
      <SkeletonLeaderboard />
    </div>
  </div>
);

export default SkeletonBody;
