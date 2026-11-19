// Geometry of the Pharos as drawn in the chapter logo: a stepped platform, a tapered square base tier with a
// grid of windows, a notched ledge, a tapered middle tier and a slim top tier (each with a tall arched window),
// a small domed lantern, and the curling flame. viewBox is 0 0 200 400.
// Levels build from the ground up: 0 platform, 1 base tier, 2 ledge and middle tier, 3 upper ledge, top tier and lantern.
export const pharosViewBox = "0 0 200 400";

// A tapered block: top edge from xl1 to xr1 at y1, bottom edge from xl2 to xr2 at y2.
const taper = (y1, xl1, xr1, y2, xl2, xr2) => ({
  points: `${xl1},${y1} ${xr1},${y1} ${xr2},${y2} ${xl2},${y2}`,
  // The darker right face, starting 60% of the way across.
  shade: `${xl1 + (xr1 - xl1) * 0.6},${y1} ${xr1},${y1} ${xr2},${y2} ${xl2 + (xr2 - xl2) * 0.6},${y2}`,
});
const block = (y, h, x, w) => taper(y, x, x + w, y + h, x, x + w);
const dentils = (y, from, to, step = 10, size = 6) =>
  Array.from({ length: Math.floor((to - from) / step) + 1 }, (_, i) => block(y, 5, from + i * step, size));

export const pharosLevels = [
  [block(350, 12, 40, 120), block(362, 12, 28, 144)],
  [taper(222, 58, 142, 350, 52, 148)],
  [block(209, 13, 48, 104), ...dentils(204, 51, 145), taper(132, 70, 130, 209, 66, 134)],
  [block(120, 12, 62, 76), taper(80, 82, 118, 120, 78, 122), block(73, 7, 78, 44), { points: "84,73 116,73 112,62 88,62", shade: "104,73 116,73 112,62 100,62" }],
];

// Windows: `arch` windows have a rounded top, the base tier has a plain grid.
export const pharosWindows = [
  ...[240, 280, 318].flatMap((y) => [{ level: 1, x: 72, y, w: 14, h: 22 }, { level: 1, x: 114, y, w: 14, h: 22 }]),
  { level: 2, x: 93, y: 146, w: 14, h: 46, arch: true },
  { level: 3, x: 95, y: 86, w: 10, h: 26, arch: true },
];

// The logo's flame: a teardrop that curls back on itself, with an inner tongue.
export const pharosFlame = "M100 6 C 118 22, 122 40, 109 54 C 104 59, 96 59, 91 54 C 80 43, 84 28, 95 19 C 94 29, 98 35, 103 33 C 107 25, 105 14, 100 6 Z";
export const pharosFlameCore = "M100 30 C 106 37, 107 44, 103 50 C 101 52, 99 52, 97 50 C 94 45, 96 38, 100 30 Z";

// Where the lighthouse stands in the closing scene: bottom right, beside the sentence, scaled down on smaller screens.
export const pharosFrameClass = "absolute bottom-[13%] right-[-4%] h-[42vh] sm:right-[2%] sm:h-[52vh] lg:right-[10%] lg:h-[min(68vh,620px)]";

// Where the light sits, as a share of the drawing's width and height (the lantern top).
export const pharosLight = { x: 0.5, y: 0.155 };

// Bounding box of a polygon's points string.
export const pointsBox = (points) => {
  const xs = [];
  const ys = [];
  points.split(" ").forEach((pair) => {
    const [x, y] = pair.split(",").map(Number);
    xs.push(x);
    ys.push(y);
  });
  return { left: Math.min(...xs), right: Math.max(...xs), top: Math.min(...ys), bottom: Math.max(...ys) };
};
