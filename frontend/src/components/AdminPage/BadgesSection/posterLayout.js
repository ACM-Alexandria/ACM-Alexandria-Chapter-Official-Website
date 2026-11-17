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
    if (!groups.has(key)) groups.set(key, { id: String(key), name: a.committeeName || "Committee", winners: [] });
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

// The one-committee poster: the committee's name, then its winners in rows. Up to `one.max` winners go in a
// single column of big rows; more go in two columns of smaller rows; beyond `two.max` they continue on another
// image. Sizes are in poster pixels, like the rest.
export const FOCUS = {
  square: {
    nameSize: 58, nameGap: 26,
    one: { max: 3, row: 112, avatar: 80, name: 34, gap: 14, width: 720 },
    two: { max: 8, row: 92, avatar: 64, name: 27, gap: 14, width: 900 },
  },
  story: {
    nameSize: 74, nameGap: 34,
    one: { max: 4, row: 140, avatar: 100, name: 40, gap: 18, width: 800 },
    two: { max: 12, row: 112, avatar: 80, name: 31, gap: 16, width: 900 },
  },
};

// Pages for one committee: [{ name, continued, winners }].
export const paginateCommittee = (group, format) => {
  const size = FOCUS[format].two.max;
  const pages = [];
  for (let i = 0; i < group.winners.length; i += size) {
    pages.push({ name: group.name, continued: i > 0, winners: group.winners.slice(i, i + size) });
  }
  return pages;
};
