// Sizes and page-splitting for the winners poster (PeriodPoster). Everything is in poster pixels.
// A card's height is known from its winner count, so pages are filled without measuring the DOM.

export const POSTER_FORMATS = {
  square: {
    label: "Square",
    hint: "1080 × 1080, for posts",
    width: 1080,
    height: 1080,
    sidePadding: 64,
    columns: 3,
    // Room for committee cards between the header and the footer.
    contentHeight: 640,
    gap: 18,
    card: { padding: 22, title: 22, titleGap: 14, row: 46, rowGap: 11, nameSize: 19, titleSize: 17, avatarText: 16 },
  },
  story: {
    label: "Story",
    hint: "1080 × 1920, for stories",
    width: 1080,
    height: 1920,
    sidePadding: 88,
    columns: 2,
    contentHeight: 1000,
    gap: 20,
    card: { padding: 26, title: 30, titleGap: 16, row: 64, rowGap: 14, nameSize: 27, titleSize: 23, avatarText: 21 },
  },
};

const cardHeight = (c, winners) =>
  c.padding * 2 + c.title + c.titleGap + winners * c.row + (winners - 1) * c.rowGap;

// Winners by rank (unranked last), then by when they were awarded.
const byRank = (a, b) =>
  (a.rank ?? Infinity) - (b.rank ?? Infinity) || String(a.awardedAt).localeCompare(String(b.awardedAt));

// Groups a period's awards by committee, in committee name order.
export const groupByCommittee = (awards) => {
  const groups = new Map();
  awards.forEach((a) => {
    const key = a.committeeId ?? a.committeeName;
    if (!groups.has(key)) groups.set(key, { name: a.committeeName || "Committee", winners: [] });
    groups.get(key).winners.push(a);
  });
  return [...groups.values()]
    .map((g) => ({ ...g, winners: g.winners.slice().sort(byRank) }))
    .sort((a, b) => a.name.localeCompare(b.name));
};

// Splits committees into pages. A committee too long for one page continues on a second card.
export const paginate = (committees, format) => {
  const f = POSTER_FORMATS[format];
  const c = f.card;
  const maxWinners = Math.max(1, Math.floor((f.contentHeight - (c.padding * 2 + c.title + c.titleGap) + c.rowGap) / (c.row + c.rowGap)));

  const cards = committees.flatMap((g) => {
    const parts = [];
    for (let i = 0; i < g.winners.length; i += maxWinners) {
      parts.push({ name: g.name, continued: i > 0, winners: g.winners.slice(i, i + maxWinners) });
    }
    return parts;
  });

  const rows = [];
  for (let i = 0; i < cards.length; i += f.columns) rows.push(cards.slice(i, i + f.columns));

  const pages = [];
  let page = [];
  let used = 0;
  rows.forEach((row) => {
    const height = Math.max(...row.map((card) => cardHeight(c, card.winners.length)));
    const needed = page.length ? used + f.gap + height : height;
    if (page.length && needed > f.contentHeight) {
      pages.push(page);
      page = [];
      used = height;
    } else {
      used = needed;
    }
    page.push(...row);
  });
  if (page.length) pages.push(page);
  return pages;
};
