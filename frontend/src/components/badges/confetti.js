// The confetti on a badge card: small strips placed by hand around the edges, away from the medal, the logo and
// (mostly) the text. The wide layout (1200×627) is the same as in the LinkedIn image the backend draws
// (BadgeCardRenderer.STRIPS), so the page and the image match. Each strip is [x, y, rotation°, length, color, opacity].
const GOLD = "#F5B301";
const LIGHT = "#FFE7A3";
const BLUE = "#8FD0F5";
const WHITE = "#FFFFFF";

export const WIDE_STRIPS = [
  [120, 72, 25, 16, LIGHT, 0.7], [300, 50, -35, 14, BLUE, 0.65], [470, 42, 60, 16, GOLD, 0.7],
  [640, 44, -20, 14, WHITE, 0.55], [820, 40, 40, 16, BLUE, 0.6], [955, 46, -50, 14, LIGHT, 0.65],
  [92, 560, -30, 16, GOLD, 0.65], [262, 586, 45, 14, WHITE, 0.5], [432, 558, -60, 16, BLUE, 0.65],
  [612, 592, 20, 14, LIGHT, 0.7], [782, 590, -40, 16, GOLD, 0.65], [942, 594, 55, 14, WHITE, 0.5],
  [1092, 590, -20, 16, BLUE, 0.6], [1168, 232, 35, 14, LIGHT, 0.65], [1150, 332, -45, 16, GOLD, 0.6],
  [1172, 432, 15, 14, BLUE, 0.6], [50, 150, 30, 14, BLUE, 0.55], [44, 250, 50, 14, WHITE, 0.5],
  [48, 480, -25, 16, LIGHT, 0.6], [1164, 130, -30, 14, WHITE, 0.55],
  [150, 110, -40, 16, BLUE, 0.7], [330, 130, 35, 14, LIGHT, 0.7], [400, 230, -55, 16, GOLD, 0.65],
  [385, 400, 25, 14, WHITE, 0.55], [310, 520, -30, 16, GOLD, 0.65], [160, 520, 50, 14, BLUE, 0.6],
  [66, 350, -35, 16, GOLD, 0.6], [1150, 200, -40, 14, BLUE, 0.65], [1150, 470, 30, 16, LIGHT, 0.65],
  [1040, 540, -55, 14, WHITE, 0.55], [1175, 300, 20, 16, GOLD, 0.7], [1152, 380, -25, 14, WHITE, 0.55],
  [1176, 500, 40, 14, LIGHT, 0.6],
];

// The narrow layout (400×760, phones): strips in the margins only.
export const TALL_STRIPS = [
  [40, 32, 25, 14, LIGHT, 0.65], [112, 52, -35, 14, BLUE, 0.6], [204, 26, 50, 14, GOLD, 0.65],
  [262, 64, -20, 14, WHITE, 0.5], [16, 240, 30, 14, BLUE, 0.55], [22, 420, -40, 16, LIGHT, 0.6],
  [14, 600, 20, 14, GOLD, 0.6], [386, 300, -30, 14, WHITE, 0.5], [380, 480, 45, 14, BLUE, 0.55],
  [388, 640, -15, 14, LIGHT, 0.6], [60, 728, -30, 16, GOLD, 0.6], [160, 742, 40, 14, WHITE, 0.5],
  [252, 726, -55, 16, BLUE, 0.6], [340, 740, 20, 14, LIGHT, 0.6],
  [14, 110, 45, 14, GOLD, 0.6], [26, 330, -30, 14, WHITE, 0.5], [18, 520, 35, 14, BLUE, 0.55],
  [384, 200, 25, 14, LIGHT, 0.6], [372, 380, -45, 14, GOLD, 0.55], [386, 560, 30, 14, WHITE, 0.5],
];
