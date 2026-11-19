// The confetti on the winners poster: small strips placed by hand in the margins and in the empty parts of the
// header, away from the logo, the title and the cards, like on the badge banner. Each strip is
// [x, y, rotation°, length, color] in poster pixels; the opacity cycles so they don't all look the same.
const G = "#F5B301";
const L = "#FFE7A3";
const B = "#8FD0F5";
const W = "#FFFFFF";
const OPACITY = [0.8, 0.6, 0.9, 0.55];

const SQUARE = [
  // The empty top right of the header
  [800, 70, 30, 24, L], [880, 120, -40, 22, B], [960, 80, 55, 26, G], [1010, 170, -25, 22, W],
  [850, 312, 45, 24, G], [930, 330, -35, 22, L], [990, 290, 20, 26, B], [780, 300, -50, 22, W],
  // The top edge
  [240, 48, -30, 22, B], [420, 46, 40, 24, G], [600, 48, -20, 22, L], [720, 46, 55, 22, W],
  // The left and right edges
  [38, 230, 35, 24, B], [42, 330, -45, 22, L], [36, 450, 20, 26, G], [44, 560, -30, 22, W],
  [38, 680, 50, 24, B], [42, 790, -20, 22, L], [36, 900, 40, 24, G], [44, 990, -55, 22, W],
  [1040, 400, -35, 24, G], [1036, 500, 30, 22, W], [1044, 610, -45, 24, L], [1038, 720, 20, 22, B],
  [1042, 830, 50, 24, G], [1036, 930, -30, 22, W], [1046, 1000, 35, 24, B],
  // Beside the closing line
  [110, 1014, 25, 24, L], [968, 1010, -50, 24, G],
  // Between the header and the cards
  [300, 356, -30, 22, G], [540, 350, 40, 22, L], [700, 362, -45, 24, B],
];

const STORY = [
  // Beside the medal
  [90, 230, 30, 30, L], [200, 300, -40, 28, B], [120, 380, 50, 30, G], [300, 240, -25, 28, W],
  [250, 420, 40, 30, L], [70, 470, -50, 28, B], [990, 240, 40, 30, G], [880, 310, -35, 28, B],
  [960, 400, 25, 30, W], [760, 250, 50, 28, L], [820, 420, -45, 30, G], [1010, 470, 35, 28, L],
  // The top edge
  [180, 60, -30, 28, B], [360, 52, 40, 30, G], [540, 48, -20, 28, W], [720, 56, 55, 28, L], [900, 60, -45, 30, B],
  // The left edge
  [40, 560, -35, 28, G], [46, 680, 25, 30, W], [40, 800, 50, 28, B], [44, 920, -30, 30, L],
  [40, 1040, 40, 28, G], [46, 1160, -45, 30, W], [40, 1280, 20, 28, B], [44, 1400, 55, 30, L],
  [40, 1520, -25, 28, G], [46, 1640, 35, 30, W], [40, 1760, -50, 28, B], [44, 1850, 30, 30, L],
  // The right edge
  [1040, 600, 30, 28, B], [1034, 720, -40, 30, L], [1040, 840, 45, 28, G], [1036, 960, -25, 30, W],
  [1040, 1080, 35, 28, B], [1034, 1200, -50, 30, L], [1040, 1320, 20, 28, G], [1036, 1440, 45, 30, W],
  [1040, 1560, -30, 28, B], [1034, 1680, 40, 30, L], [1040, 1800, -45, 28, G],
  // The bottom edge and beside the closing line
  [160, 1862, 30, 28, G], [340, 1864, -40, 30, B], [520, 1860, 50, 28, L], [700, 1864, -25, 30, W],
  [880, 1860, 40, 28, G], [130, 1790, 40, 28, L], [960, 1780, -35, 28, B],
  // Between the header and the content
  [200, 660, -35, 28, B], [420, 654, 45, 30, G], [650, 664, -30, 28, L], [860, 656, 40, 30, W],
];

// Edge strips are kept inside the gold frame (the frame sits 16 and 26 px from the edge).
const withOpacity = (strips) => strips.map(([x, y, rotation, length, color], i) => ({ x: Math.min(Math.max(x, 46), 1034), y, rotation, length, color, opacity: OPACITY[i % OPACITY.length] }));

export const POSTER_STRIPS = { square: withOpacity(SQUARE), story: withOpacity(STORY) };
