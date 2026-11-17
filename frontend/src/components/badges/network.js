// The web of nodes and lines behind a badge card. The wide layout (1200×627) uses a fixed list of nodes
// [x, y, radius], the same as in the LinkedIn image the backend draws (BadgeCardRenderer.NODES), so the page and
// the image match. The narrow layout (phones) makes its own list. Two nodes closer than `reach` are joined by a
// line that fades with distance.
export const WIDE_NODES = [
  [518.7, 280.3, 3.0], [458.9, 298.9, 5.1], [1044.1, 216.8, 3.5], [931.6, 118.4, 3.2], [735.9, 213.2, 2.3],
  [1099.3, 129.4, 3.9], [988.0, 32.3, 3.7], [277.2, 344.6, 3.6], [117.4, 419.3, 4.8], [1078.0, 406.8, 4.1],
  [220.2, 208.6, 3.3], [373.8, 398.5, 3.5], [466.4, 581.4, 4.0], [904.0, 197.1, 5.0], [1105.7, 498.3, 4.2],
  [153.3, 295.6, 2.2], [121.8, 454.1, 3.4], [942.5, 175.5, 2.8], [790.6, 527.8, 2.1], [29.6, 76.1, 2.7],
  [722.4, 471.4, 3.0], [1164.9, 134.4, 5.2], [1165.3, 160.2, 2.9], [11.8, 54.2, 4.7], [985.6, 452.6, 3.4],
  [379.5, 140.7, 3.6], [943.5, 156.5, 3.3], [194.2, 349.8, 3.4], [63.5, 349.3, 5.2], [431.7, 260.4, 4.6],
  [403.7, 123.5, 4.4], [1002.1, 361.9, 5.3], [554.0, 31.0, 4.4], [3.8, 426.3, 2.1],
];

const random = (seed) => () => {
  seed |= 0;
  seed = (seed + 0x6D2B79F5) | 0;
  let t = Math.imul(seed ^ (seed >>> 15), 1 | seed);
  t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
  return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
};

// Nodes for a box of the given size; always the same for the same seed.
export const makeNodes = (seed, count, width, height) => {
  const next = random(seed);
  return Array.from({ length: count }, () => [next() * width, next() * height, 2 + next() * 3.5]);
};

// The lines between nearby nodes: [x1, y1, x2, y2, opacity].
export const linesOf = (nodes, reach) => {
  const lines = [];
  nodes.forEach((a, i) => nodes.slice(i + 1).forEach((b) => {
    const d = Math.hypot(a[0] - b[0], a[1] - b[1]);
    if (d < reach) lines.push([a[0], a[1], b[0], b[1], 0.3 * (1 - d / reach)]);
  }));
  return lines;
};
