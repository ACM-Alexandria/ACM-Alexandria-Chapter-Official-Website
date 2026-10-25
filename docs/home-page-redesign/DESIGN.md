# Home page redesign: design and implementation spec

> **Status: built.** The design below is implemented in `frontend/src/pages/HomePage.jsx` and
> `frontend/src/components/Home/` (shared pieces in `frontend/src/components/ui/`). The preview route, the
> `reference/` copies of the preview code, the `VITE_ENABLE_*` section flags and the services section have since
> been removed: every section always shows when it has content. Read the code for the current behaviour; this
> document records the intent behind it.

This document describes the new public home page of the ACM Alexandria chapter website, so an engineer or coding
agent can build it into the real app and get exactly the result that was approved in the preview.

The approved design is **template 4 ("Your layout")** of the preview route `/home-preview/4`
(`frontend/src/pages/homePreview/`). The preview reads a static snapshot of the live data; the real page must read
the same data from the API.

## 0. How to use this document

- **The code in [`reference/`](reference/) is the source of truth for every visual detail.** It is a verbatim copy
  of the approved preview files. Port it; do not redesign, restyle, re-time or "improve" anything. Every class
  name, colour, duration and scroll range in it was tuned and signed off.
- This document explains what each piece is, how it behaves, where it goes in the app, how to feed it real data,
  and what current home page features must survive.
- Where this document and the reference code disagree on a visual detail, the reference code wins. Where they
  disagree on data, routing or app integration, this document wins.
- Section 10 lists the decisions that are not covered by the preview, each with a default. Use the default unless
  the maintainer says otherwise.

### Reference files

| Reference file | What it is |
| --- | --- |
| `YourLayout.jsx` | The whole page: section order, people section, teams explorer, activity sections, cards |
| `parts/HeroPharos.jsx` | The pinned hero + lighthouse scene (one scroll-driven animation loop) |
| `parts/Pharos.jsx` | The lighthouse SVG drawing with blueprint, build, scan line, flame and windows |
| `pharosGeometry.js` | Lighthouse polygons, windows, flame paths, light position, responsive frame classes |
| `parts/HeroIntro.jsx` | Hero text column: badge, headline, tagline, two buttons |
| `parts/NightSky.jsx` | Twinkling stars + shooting star, dark mode only |
| `parts/CommitteeOrbit.jsx` | Static orbit used only by the reduced-motion version of the hero |
| `parts/PharosFinale.jsx` | Reduced-motion version of the lighthouse scene (also usable as a standalone scene) |
| `parts/IslandNav.jsx` | Navbar that turns from full-width transparent into a floating rounded bar |
| `parts/Marquee.jsx` | Infinite sliding row (gallery) |
| `parts/OrgTree.jsx` | Root node with members branching underneath, lines draw in on view |
| `parts/Portrait.jsx` | Tall photo card with name and role over the photo |
| `parts/Avatar.jsx` | Photo with initials fallback |
| `parts/PreviewFooter.jsx` | Footer look (logo, email, social icons) |
| `Reveal.jsx` | Fade + lift once when scrolled into view |
| `CountUp.jsx` | Number counts up once when scrolled into view |
| `useInView.js` | IntersectionObserver hook, fires once |
| `useScrollFrame.js` | `clamp01`, `prefersReducedMotion`, rAF-throttled scroll/resize hook |
| `useSmoothScroll.js` | Lenis smooth scrolling + in-page anchor handling |
| `liveHelpers.js` | Formatting helpers (`shortName`, `initials`, `eventDate`, `summary`, `tagline`, `contactEmail`) and the snapshot import that must be replaced |
| `previewStyles.css` | All keyframes (`hp-*`) and the reduced-motion kill switch |

`liveSnapshot.json` is deliberately **not** exported: it contains members' names and photo links and must never be
committed. The real page reads the API instead (section 9).

---

## 1. The experience in one paragraph

The page opens on a deep blue hero: "Learn it, build it, then ship it." on the left and the chapter's committees
and clubs orbiting the ACM logo on the right. As the visitor scrolls, the screen stays pinned: the orbit rings crack
and fall, the sky turns to night, and a blueprint of the Pharos (the lighthouse from the chapter logo) draws itself.
The orbiting committees and clubs then fall out of orbit in pairs onto the lighthouse, and each pair merges into its
tier as the tier fills in, so the chapter's teams literally build the Pharos. The flame ignites and the beam sweeps
down across the sentence "Alexandria lit the way for the ancient world. We light the way into computing.", lighting
each word only when the beam reaches it, and comes to rest exactly on the "Join ACM Alexandria" button. The page then
continues with a sliding photo gallery, the people (high board tree and a committees/clubs explorer), and three
activity sections (events, programs, ACM Radio), each with one big featured card and three small ones that zoom and
settle in with the scroll. Scrolling is smooth (Lenis) and the navbar floats as an island once you leave the top.

---

## 2. Scope

**Changes**

- `/` (the home page) gets the new design. `frontend/src/pages/HomePage.jsx` is rewritten.
- New components under `frontend/src/components/Home/` (section 3).
- New shared hooks in `frontend/src/hooks/`.

**Does not change**

- Every other route and page (`/events`, `/programs`, `/radio`, `/radio/seasons/:id`, `/boards`, `/login`,
  `/register`, `/profile`, `/admin`, …).
- The API, the backend, auth, `AuthContext`, the theme mechanism (`.dark` class on `<html>`, `localStorage.theme`).
- The detail sidebars, registration modal and exclusive forms components (they are reused, section 10).

**Removed at the end**

- The preview route `/home-preview/:style` and its redirect in `App.jsx`.
- The folder `frontend/src/pages/homePreview/` (templates 1–3, the switcher, `previewData.js`, `liveSnapshot.json`).
- The old home sections in `frontend/src/components/HomePage/sections/` that are no longer imported (check with a
  search before deleting; `BoardsPage` and other pages may import cards from `components/HomePage/cards`, keep
  anything still imported).

---

## 3. Target file layout

Port the reference files to these paths. Keep each file's content; only change imports, data access and the
items called out in sections 9 and 10.

| Reference | Target |
| --- | --- |
| `YourLayout.jsx` | `pages/HomePage.jsx` (page shell + data loading) and split into `components/Home/sections/PeopleSection.jsx`, `components/Home/sections/TeamsExplorer.jsx`, `components/Home/sections/ActivitySection.jsx`, `components/Home/cards/FeatureCard.jsx`, `components/Home/cards/SmallCard.jsx`, `components/Home/sections/GallerySection.jsx` |
| `parts/HeroPharos.jsx` | `components/Home/scene/HeroPharos.jsx` |
| `parts/Pharos.jsx` | `components/Home/scene/Pharos.jsx` |
| `pharosGeometry.js` | `components/Home/scene/pharosGeometry.js` |
| `parts/HeroIntro.jsx` | `components/Home/scene/HeroIntro.jsx` |
| `parts/NightSky.jsx` | `components/Home/scene/NightSky.jsx` |
| `parts/CommitteeOrbit.jsx` | `components/Home/scene/CommitteeOrbit.jsx` |
| `parts/PharosFinale.jsx` | `components/Home/scene/PharosFinale.jsx` |
| `parts/IslandNav.jsx` | `components/Home/IslandNav.jsx` (extended, section 5.1) |
| `parts/PreviewFooter.jsx` | `components/Home/HomeFooter.jsx` (extended, section 5.6) |
| `parts/Marquee.jsx` | `components/Home/ui/Marquee.jsx` |
| `parts/OrgTree.jsx` | `components/Home/ui/OrgTree.jsx` |
| `parts/Portrait.jsx` | `components/Home/ui/Portrait.jsx` |
| `parts/Avatar.jsx` | `components/Home/ui/Avatar.jsx` |
| `Reveal.jsx` | `components/Home/ui/Reveal.jsx` |
| `CountUp.jsx` | `components/Home/ui/CountUp.jsx` |
| `useInView.js` | `hooks/useInView.js` |
| `useScrollFrame.js` | `hooks/useScrollFrame.js` |
| `useSmoothScroll.js` | `hooks/useSmoothScroll.js` |
| `liveHelpers.js` (helpers only) | `components/Home/homeFormat.js` |
| (new) | `components/Home/homeData.js`: API → design model normaliser (section 9) |
| `previewStyles.css` | `components/Home/home.css`, imported once by `pages/HomePage.jsx` |

Rules for the split:

- Non-component exports never live in `.jsx` component files (the project's ESLint `react-refresh` rule flags them).
  Helpers and constants go in `.js` files, as `pharosGeometry.js` and `homeFormat.js` already do.
- Components receive data as props. Only `pages/HomePage.jsx` fetches. Nothing imports a global `live` object.
- Keep file-level and component-level comments from the reference; they explain the tuned numbers.
- `lenis@1.1.18` is already in `frontend/package.json`; keep it.

---

## 4. Design tokens

The page uses the chapter blues from the logo and the admin design system (`docs/admin-design-system.md`).
Use these exact values (they appear as Tailwind arbitrary values in the reference code).

### 4.1 Colour

| Role | Light | Dark |
| --- | --- | --- |
| Page background | `#F3F7FB` | `#0A1724` |
| Hero / gallery / radio / footer background | `#0F3A5A` | `#081826` |
| Night sky gradient (scene) | `linear-gradient(180deg,#081826 0%,#06121D 45%,#0B2236 78%,#0F3A5A 100%)` | same |
| Surface (cards, white sections) | `#FFFFFF` | `#0F2233` |
| Surface ring | `#D6E6F2` | `white/10` |
| Primary (buttons, accents) | `#205E85` (hover `#16364C`) | `#4B98C8` with text `#081826` |
| Accent text | `#2A7AB0` | `#7DBBE0` |
| Headline accent ("then ship it.") | `#7DBBE0` | `#7DBBE0` |
| Text on blue | `#CFE6F5` / `#BFE0F5` | same |
| Body text | `slate-800` | `slate-100` |
| Muted text | `slate-600` | `#A9C7DB` |
| Soft blue fill (avatars, chips) | `#E2F0FA` text `#1D6A9C` | `#16364C` text `#7DBBE0` / `#9FD0EE` |
| Tree lines | `#9CC5E0` | `#2C5878` |
| Stars | `#D3E9F7` | `#D3E9F7` |
| Lighthouse tower / shaded face | `#3D82B5` / `#235E8B` | same |
| Blueprint stroke | `#9FD0EE` | same |
| Scan line | `#E9F6FF` | same |
| Unlit flame / core | `#4B98C8` / `#2C6F99` | same |
| Lit flame / core | `#FFC857` / `#FFF4CC` | same |
| Unlit / lit windows | `#DCEEFA` / `#FFD97A` | same |
| Lit words | `#FFF4CC` | same |
| Join button (scene) | bg `#FFE7A3`, text `#06121D`, glow `0 0 40px rgba(255,220,130,0.45)` | same |
| Open / closed tag | `emerald-50`/`emerald-700`, `slate-100`/`slate-600` | `emerald-950/50`/`emerald-300`, `white/5`/`#A9C7DB` |

### 4.2 Typography

- Font: **Inter**, through the existing `.admin-ui` class in `src/index.css` (it also steps every weight up one
  level: normal 500, medium 600, semibold 700). The page root has `className="hp admin-ui …"`.
- Headline (hero): `text-[2.6rem]` phone, `sm:text-6xl`, `lg:text-[4.25rem]`, `xl:text-[4.75rem]`, `leading-[1.03]`,
  `tracking-[-0.035em]`, semibold. First line `whitespace-nowrap`.
- Scene sentence: `text-2xl` phone, `sm:text-4xl`, `md:text-5xl`, `lg:text-7xl`, `leading-[1.08]`,
  `tracking-[-0.035em]`; max width `15ch` / `md:16ch` / `lg:18ch`.
- Section titles: `text-4xl sm:text-5xl`, `tracking-tight`, `textWrap: balance`.
- Activity counts: `text-6xl sm:text-7xl`, tabular numbers.
- Body: `text-lg` on desktop, `text-base` in the hero tagline on phones.

### 4.3 Shape, depth, layout

- Container: `mx-auto max-w-[1280px] px-5 sm:px-8`.
- Radii: big cards `rounded-[2rem]`, explorer panel `rounded-[2.25rem]`, portraits `rounded-[1.75rem]`, small
  cards and gallery photos `rounded-3xl`, buttons `rounded-xl`, chips `rounded-full`.
- Shadows: chips `0_10px_30px_-10px_rgba(4,24,40,0.6)`; floating nav `0_14px_40px_-18px_rgba(8,24,38,0.5)`;
  high board root `0_18px_40px_-20px_rgba(15,58,90,0.8)`.
- Breakpoints (Tailwind defaults): phone `<640`, `sm` 640, `md` 768, `lg` 1024 (desktop layout starts here),
  `xl` 1280.

---

## 5. Page structure

Order, top to bottom (all inside `<div className="hp admin-ui min-h-screen bg-[#F3F7FB] text-slate-800 dark:bg-[#0A1724] dark:text-slate-100">`):

1. `IslandNav` (fixed)
2. `HeroPharos` (`id="top"`, pinned scene)
3. Gallery marquee (`aria-label="Gallery"`)
4. `<main>`: People (`id="people"`), Events (`id="events"`), Programs (`id="programs"`), Radio (`id="radio"`)
5. Footer

Plus the existing overlays that must stay mounted (section 10): detail sidebars, registration modal, exclusive forms.

### 5.1 Island navbar (`IslandNav`)

Behaviour (keep exactly):

- At the top: full width (`max-w-[1280px]`), transparent, `h-[72px]`, white text, no radius.
- Once `window.scrollY > window.innerHeight * 0.5` it becomes a floating rounded bar: `pt-2.5` offset, `h-14`,
  `max-w-[1080px]`, `rounded-2xl`, `bg-white/95` (dark `#0F2233/95`), `backdrop-blur-md`, ring and shadow, dark text.
- Every change is a `300ms ease-out` transition, both directions. The logo circle shrinks from `h-10` to `h-9`.
- Section links (`People`, `Events`, `Programs`, `Radio`) show from `lg` up; they are `#id` anchors handled by Lenis
  with a `-88px` offset (`useSmoothScroll`).
- Right side: "Join us" button to `/register` (white button at the top, primary button when floating).

Must be added (the preview had no auth, theme toggle or phone menu; the current `components/HomePage/Navbar.jsx`
does, and none of it may be lost):

- **Theme toggle**: reuse `components/Layout/ThemeToggleButton.jsx` and the same state logic as the current
  Navbar (saved choice in `localStorage.theme`, else `prefers-color-scheme`; toggles `.dark` on `<html>`). Place it
  left of the right-side button. It must read well on both the transparent and the floating state.
- **Auth area**, replacing "Join us" when relevant:
  - loading: the small spinner from the current Navbar;
  - signed out: "Join us" (`/register`) plus a "Sign in" text link (`/login`);
  - signed in: the current avatar button and dropdown (My Profile, Admin Tools for
    `SUPER_ADMIN`/`ACM_HIGH_BOARD`/`ACM_COMMITTEE_BOARD`/`ACM_CLUB_BOARD`, HR Dashboard for HR committee members,
    Log out with the existing `LogoutConfirmModal`). Restyle the dropdown with the page tokens (white / `#0F2233`
    surface, `rounded-2xl`, ring), keep its logic.
- **Phone and tablet menu** (`<lg`): a menu button in the bar opening a panel below it (same surface as the
  floating bar, `rounded-2xl`, full width minus `px-3`) with the section links, theme toggle and auth actions. Lock
  body scroll while open, close on Escape, on link click and on outside click (same rules as the current Navbar).
- **Feature flags**: show a section link only if its section renders (`VITE_ENABLE_EVENTS`, `VITE_ENABLE_PROGRAMS`,
  `VITE_ENABLE_RADIO`; People shows if `VITE_ENABLE_ABOUT` or `VITE_ENABLE_COMMITTEES` or `VITE_ENABLE_CLUBS`).
- **Off the home page** the current Navbar navigates to `/` then scrolls; IslandNav is only used on `/`, other pages
  keep their current Navbar.

### 5.2 Hero + lighthouse scene (`HeroPharos`)

One `<section id="top">` of height `520vh` with a `sticky top-0 h-screen overflow-hidden` scene inside. One
`requestAnimationFrame` loop reads the clock (for the orbit) and the scroll progress `p` (0 at the top of the
section, 1 when its bottom reaches the bottom of the screen), and writes CSS custom properties on the section.
Elements read those properties in `calc()` / `clamp()`. React never re-renders per frame.

**Never split the hero and the lighthouse into two sections.** It was tried; the hand-off between them is visible.
The planets must fall from their exact current orbit positions, which needs the single loop.

#### Layers (bottom to top)

1. Section background `#0F3A5A` / dark `#081826`; light-mode dot grid (`radial-gradient(#fff 1px, transparent 1px)`,
   `28px`, opacity `0.08 * (1 - --night)`).
2. `NightSky` (dark mode only).
3. Night gradient, opacity `--night`; 60 stars, opacity `--night`.
4. Lighthouse frame (`pharosFrameClass`, `aspectRatio: 1 / 2`) containing the beam `div` and `<Pharos plan build light />`.
5. Three wave layers at the bottom (`h-[16%]`), rising in with `--night`.
6. Closing sentence + CTA block, opacity `--words`.
7. Hero grid: `HeroIntro` (fades out with `--out`) and the orbit (two `BrokenRing`s + centre logo that fades with `--logo`).
8. Merge ripples (one per planet).
9. Planet chips (`<ul aria-label="Committees and clubs">`).

#### Timeline (values of `p`)

`range(p, start, length) = clamp01((p - start) / length)`.

| Variable | Range of `p` | Drives |
| --- | --- | --- |
| `--brk` | 0.02 → 0.14 | Orbit ring pieces crack, drift, spin and drop one after another |
| `--out` | 0.02 → 0.12 | Hero text fades out and lifts 90px (pointer events off past 0.5) |
| `--night` | 0.04 → 0.18 | Sky turns to night, stars and waves appear |
| `--plan` | 0.05 → 0.17 | Blueprint outline of the lighthouse draws (stroke dash) |
| `--logo` | 0.08 → 0.18 | Centre logo fades and shrinks to 60% |
| `--words` | 0.13 → 0.23 | The sentence block appears, every word dim (opacity 0.12) |
| planets | pair `k`, side `s` start at `0.08 + 0.065k + 0.018s`; fall lasts 0.10; merge lasts 0.045 | Planet leaves orbit on an arc (easeInOut, 60px lift, spin), lands on its tier, then shrinks and fades while a ripple expands |
| `--build` | average of the four tiers' merge progress | Tiers fill bottom-up behind a scan line |
| `--ignite` | 0.43 → 0.49 | Flame turns warm and flickers, glow grows, windows light, beam fades in |
| `--sweep` | 0.50 → 0.86 | Beam rotates from 208° down to the Join button |
| `--cta` | `--sweep` 0.90 → 1.00 | Join button + caption fade in and rise 24px (pointer events past 0.5) |
| fallback | 0.90 → 0.95 | Any word the beam could not reach (small screens) finishes lighting |

#### Planets

- The first 8 of `[...committees, ...clubs]` (committees first). Committees orbit at radius `0.44 × ring width`,
  period 70 s, clockwise, chip `text-sm`, logo `h-8`. Clubs at radius `0.26`, period 50 s, counter-clockwise,
  `text-xs`, logo `h-6`. Starting angles spread evenly from the top.
- Planet `i` belongs to tier `floor(i / 2)`, side `i % 2`.
- Landing points on the drawing (viewBox 200 × 400): tier y `[362, 286, 170, 100]`, spread either side of the centre
  `[40, 30, 22, 14]`. Converted to pixels from the measured lighthouse rect every frame.
- A tier with no planets (fewer than 8 teams) builds on its own schedule (as if its second planet had merged).
- On phones (`<sm`) chips show only the round logo; the name is `hidden sm:inline`.

#### Beam and words

- Lantern position = lighthouse rect + `pharosLight` (`x 0.5`, `y 0.155` of the drawing).
- Beam: `div` `h-[106vmax] w-[150vmax] origin-left`, placed at the lantern with `marginTop: -53vmax`,
  `transform: rotate(var(--beam))`, `clipPath: polygon(0 49.4%, 100% 32%, 100% 68%, 0 50.6%)`, background
  `linear-gradient(90deg, rgba(255,236,170,0.75) 0%, rgba(255,226,140,0.28) 35%, rgba(255,226,140,0) 85%)`,
  opacity `--ignite × 0.9`. Sizing in `vmax` keeps the same thin beam on portrait phones.
- `--beam = 208° − sweep × (208° − angleToJoinButton)`. The end angle is measured every frame from the lantern to
  the centre of the actual `<Link>` (the CTA wrapper's first child), so the beam always ends on the button at any
  screen size.
- A word lights (`opacity 0.12 → 1`) only once the beam edge (`BEAM_HALF = 6°` off centre) passes the word's centre,
  with a 5° fade. Words are separate `inline-block` spans with refs.

#### Responsive frame

- Hero grid: one column below `lg` (`pt-20`, `gap-8`), two columns from `lg` (`1.05fr 1fr`, `pt-16`, `gap-10`).
- Orbit size: `max-w-[min(560px,86vw,calc(100svh-500px))]` below `lg`, `max-w-[min(560px,58vh)]` from `lg`.
- Lighthouse frame (`pharosFrameClass`): bottom 13%, right −4% and `h-[42vh]` on phones, `sm` right 2% `h-[52vh]`,
  `lg` right 10% `h-[min(68vh,620px)]`. The sentence stays left of the lighthouse at every size (phones use the
  smaller type and narrow max width from 4.2).
- CTA block: `max-w-[62%]` below `lg` so the caption never runs under the tower; button `h-11 px-5`, `sm:h-12 sm:px-7`.
- Merge ripple `h-10 w-10`, `sm:h-16 sm:w-16`.

#### Reduced motion

If `prefers-reduced-motion: reduce`, render a normal-height hero (`HeroIntro` + static `CommitteeOrbit`) followed by
`PharosFinale` showing the finished, lit lighthouse with the committees as chips in the sky. No pinning, no loops.

### 5.3 The lighthouse drawing (`Pharos`)

`<Pharos plan build light />`, each 0..1, number or CSS expression (`"var(--build)"`).

- Geometry (`pharosGeometry.js`): viewBox `0 0 200 400`. Four levels built bottom-up: 0 stepped platform; 1 tapered
  base tier with a 2 × 3 window grid; 2 notched ledge with dentils and tapered middle tier with a tall arched window;
  3 upper ledge, slim top tier with an arched window, small domed lantern. Every block is a polygon with a darker
  right face (60% across). The flame is a curling teardrop with an inner tongue.
- `plan`: thin `#9FD0EE` outline of every polygon and the flame, drawn with `pathLength="1"` and
  `strokeDashoffset: calc(1 - plan)`, opacity `plan × 0.75`.
- `build`: level `k` reveals with `clamp(0, build × 4 − k, 1)` through `clipPath: inset(calc((1 − progress) × 100%) 0 0 0)`;
  a bright scan line (`#E9F6FF`, glow) rides the top of the reveal and fades at both ends.
- `light`: glow circle scales in, the blue flame crossfades to the warm flame (with `hp-flicker`), windows turn warm.

### 5.4 Gallery

- `<section aria-label="Gallery" className="bg-[#0F3A5A] pb-14 pt-6 dark:bg-[#081826]">` directly under the scene,
  so the scene's bottom colour flows into it.
- `Marquee` with `duration={60}`, `label="Photos from chapter events"`. Images `mr-4 h-40 w-60 sm:h-52 sm:w-80
  rounded-3xl object-cover ring-1 ring-white/10`, `loading="lazy"`, empty `alt` (decorative).
- Hover pauses; reduced motion shows one static copy.
- No scroll-pinned or "apple" animation on the gallery (rejected).

### 5.5 People (`id="people"`)

- Section `py-16 sm:py-24`. Header (`Reveal`): "Led by students, for students", the line "The high board leads the
  chapter. Every committee and club has its own heads under it.", and a "See past boards →" link to `/boards`.
- **High board tree**: `OrgTree` with a root pill (logo in white circle, "High board", "ACM Alexandria · this year")
  and members rendered as `Portrait` (`aspect-[4/5]`). Lines draw in on view; from `sm` all members sit on one row
  with connectors, on phones two columns without lines.
- **Teams explorer** (`Reveal`): one panel with a Committees / Clubs tab switch (with counts) and a scrollable row of
  team chips. The selected team shows its logo (`h-24 w-24 rounded-3xl`), name, Open/Closed tag, description, and an
  `OrgTree` of its board as `Portrait`s. Switching re-keys the block so it animates in (`hp-rise`).

### 5.6 Activity sections (events, programs, radio)

`ActivitySection` props: `id, icon, label, count, countLabel, text, to, cta, flip, tone, featured, more`.

| Section | Tone | Flip | Featured | More (max 3) | CTA |
| --- | --- | --- | --- | --- | --- |
| Events | white | no | newest event, badge "Latest", full date, summary 190 chars, room | next 3 events, `SmallCard` 4:3 | "See all N events" → `/events` |
| Programs | default (page bg) | yes | first program, badge "Registration open"/"closed", time, summary, start–end dates | next 3 programs | "See all programs" → `/programs` |
| Radio | blue | no | newest season, badge "Newest season", "Season N", "N episodes so far" | next 3 seasons, square cards | "Listen to ACM Radio" → `/radio`; small cards → `/radio/seasons/:id` |

- Layout: `lg:grid-cols-[0.7fr_1.3fr]`; the intro column (icon label, big `CountUp` number, count label, text, CTA)
  is `lg:sticky lg:top-28`. Section `py-16 sm:py-24`.
- Scroll link: `--sp` = `clamp01((innerHeight − sectionTop) / (innerHeight × 0.85))`, written by `useScrollFrame`.
  - Intro column: `translateY((1 − sp) × 48px)`, opacity `0.15 + 0.85 sp`.
  - `FeatureCard`: `scale(0.84 + 0.16 sp) translateY((1 − sp) × 70px)`, its photo `scale(1.4 − 0.4 sp)` (16:9).
  - `SmallCard` `i`: `translateY((1 − sp) × (110 + 70i)px × --lift)`, opacity `1.4 sp − 0.2`.
- Phones (`<sm`): the three small cards are a horizontal swipe row (`snap-x snap-mandatory overflow-x-auto`,
  cards `w-[72%]`, scrollbar hidden, row bleeds to the screen edge with `-mx-5 px-5`), and `--lift` is 0 so cards
  only fade (a sliding card would be clipped by the scroll row). From `sm`: 3-column grid, `--lift` 1.
- No full "apple" pinned stage on these sections (rejected); only the scroll-linked zoom above.

### 5.7 Footer

Look from `PreviewFooter`: `#0F3A5A` / `#081826`, logo in white circle, "ACM Alexandria", mail link, round social
icon buttons (`FiFacebook`, `FiInstagram`, `FiLinkedin`, `FiYoutube`, `FiTwitter` for X/Twitter, `FiGlobe` fallback).
Social links come from `/api/socialLinks`. See section 10 for the current footer's features that must be kept.

---

## 6. Motion system

- **Technique**: scroll position → CSS custom properties on a parent → `calc()`/`clamp()` in `style` props. Use
  independent `translate` / `rotate` / `scale` properties when a CSS animation already owns `transform`.
- **Loops**: the scene runs its own rAF loop (it needs the clock for the orbit). Other scroll effects use
  `useScrollFrame` (rAF-throttled scroll/resize listener). Never `setState` per frame.
- **Smooth scroll**: `useSmoothScroll()` once in `HomePage` (`new Lenis({ duration: 1.15, smoothWheel: true })`,
  own rAF, `#id` clicks via `lenis.scrollTo(target, { offset: -88 })`, `#top` to 0). Lenis 1.1.18 has no `anchors`
  option; keep the manual click handler. Skipped for reduced motion. Destroy on unmount.
- **Keyframes** (`home.css`): `hp-rise`, `hp-fade`, `hp-pop`, `hp-orbit`, `hp-counter`, `hp-marquee`, `hp-twinkle`,
  `hp-shoot`, `hp-grow-y`, `hp-grow-x`, `hp-flicker`, `hp-wave`, `hp-ring`.
- **Reduced motion**: `.hp *` animations and transitions off; scene replaced (5.2); `--sp` forced to 1; `CountUp`
  shows the final number; `Reveal` shows content immediately; Lenis not started.
- **Known traps** (each one happened during the design):
  - An element with an `hp-pop` animation (`fill-mode: both`) ignores inline `opacity`. Fade a wrapper instead
    (the centre logo does this).
  - The sentence must be gated by its own `--words`, or it ghosts behind the hero text.
  - Word lighting must be geometric (beam angle vs word angle), not time-based, or words light before the beam
    touches them.
  - The beam end angle must target the button element itself, not its wrapper, or the button appears late.
  - Beam sized in `vh`/`vw` becomes a wide wedge on portrait phones; keep `vmax`.

---

## 7. Responsive summary

| Area | Phone `<640` | Tablet `640–1023` | Desktop `≥1024` |
| --- | --- | --- | --- |
| Nav | bar + menu button | bar + menu button | bar + section links |
| Hero | stacked; buttons side by side `h-11`; orbit fits remaining height; logo-only chips | stacked; chips with names | two columns |
| Scene | sentence `text-2xl` left, lighthouse `42vh` bottom right | `text-4xl`/`md:text-5xl`, lighthouse `52vh` | `text-7xl`, lighthouse `min(68vh,620px)` |
| Gallery | photos 240 × 160 | 320 × 208 | 320 × 208 |
| High board | 2 columns, no lines | one row with lines | one row with lines |
| Activity | stacked; small cards swipe row | stacked; 3-column small cards | intro sticky left, cards right |

Verified widths during design: 375 × 812, 768 × 1024, 800 × 676 and desktop.

---

## 8. Dark mode

- Driven by the existing `.dark` class (`@custom-variant dark` in `index.css`). Every colour in 4.1 has its pair.
- The hero in dark mode uses the night sky from the start (`NightSky` is `hidden dark:block`; the dot grid is
  `dark:hidden`). The scene's night transition still runs on top.
- Chips: white / `#12304A` with `#205E85` / `#D3E9F7` text.

---

## 9. Data wiring

### 9.1 Sources

All from `frontend/src/services/homePageService.js` (existing functions, existing `api` axios instance):

| Data | Call | Notes |
| --- | --- | --- |
| Committees | `fetchCommittee()` → `/api/committee` | array, each with `boardRoles` |
| Clubs | `fetchAllClubs()` → `/api/clubs` | Spring page or array, use `.content` when present; each with `boardRoles` |
| High board | `fetchHighBoard()` → `/api/highboard` | array of board role records |
| Events | `fetchEvents(0)` → `/api/events?page=0` | Spring page; `totalElements` is the real count |
| Programs | `fetchPrograms(0)` → `/api/program?page=0` | Spring page |
| Radio seasons | `fetchSeasons(0)` → `/api/radio/seasons?page=0` | page; each season with `episodes` array |
| Gallery | `fetchGalleryImages()` → `/api/gallery` | array of `{ imageUrl }` |
| Social links | `fetchSocialLinks()` → `/api/socialLinks` | array of `{ platform, url }` |
| Partners, active forms | `fetchPartners()`, `fetchActiveExclusiveForms()` | only if their feature flags are on (section 10) |

Load with `Promise.allSettled` like `fetchHomePageData` so one failing call never blanks the page.

### 9.2 Design model (`homeData.js`)

The components expect exactly the shapes of the preview snapshot. Normalise once, in `homeData.js`. This is the
mapping the snapshot was built with from the live API:

```js
// homeData.js: turns API responses into the shapes the home page components use.
const items = (data) => (Array.isArray(data) ? data : data?.content ?? []);
const byOrder = (a, b) => (a.order ?? 99) - (b.order ?? 99);

export const toMember = (record) => ({
  name: record.userName || record.user?.name || "",
  role: record.role,
  order: record.order,
  image: record.profileImageUrl || record.user?.profileImageUrl || "",
});

export const toHomeData = ({ committees, clubs, highBoard, events, programs, seasons, gallery, social }) => ({
  committees: items(committees).map((c) => ({
    id: c.id, name: c.name.trim(), description: c.description, logoUrl: c.logoUrl, open: Boolean(c.open),
    board: (c.boardRoles || []).map(toMember).sort(byOrder),
  })),
  clubs: items(clubs).map((c) => ({
    id: c.id, name: c.name.trim(), description: c.description, imageUrl: c.imageUrl, open: Boolean(c.registrationOpen),
    board: (c.boardRoles || []).map(toMember).sort(byOrder),
  })),
  highBoard: items(highBoard).map(toMember).sort(byOrder),
  events: items(events)
    .map((e) => ({ id: e.id, name: e.name.trim(), description: e.description, imageUrl: e.imageUrl, time: e.eventTime, location: e.location, open: Boolean(e.registrationOpen) }))
    .sort((a, b) => new Date(b.time) - new Date(a.time)),
  programs: items(programs).map((p) => ({
    id: p.id, name: p.name, description: p.description, imageUrl: p.imageUrl,
    startDate: p.startDate, endDate: p.endDate, time: p.time, open: Boolean(p.registrationOpen),
  })),
  radio: items(seasons).map((r) => ({ id: r.id, season: r.seasonNumber, imageUrl: r.imageUrl, episodes: (r.episodes || []).length })),
  gallery: items(gallery).map((g) => g.imageUrl).filter(Boolean),
  social: items(social).map((s) => ({ platform: s.platform, url: s.url })),
  counts: {
    events: events?.totalElements ?? items(events).length,
    programs: programs?.totalElements ?? items(programs).length,
    radio: seasons?.totalElements ?? items(seasons).length,
  },
});
```

Verify each field name against a real response before relying on it (the names above are what the live API
returned when the snapshot was taken). Then:

- `ActivitySection` counts use `counts.*` (the preview used the length of page 0, which caps at the page size).
  Episodes total = sum of `radio[].episodes`.
- The events "featured" item is the newest by `time`; "more" are the next three.
- `homeFormat.js` keeps `shortName`, `initials`, `eventDate`, `summary`, `tagline`, `contactEmail` from
  `liveHelpers.js` unchanged; drop `live`, `stats` and the JSON import.

### 9.3 Loading, empty and error states

- **Loading**: the scene and hero render immediately with no planets (the loop already handles zero planets: tiers
  build on their own schedule). When data arrives the planets pop in with their `hp-pop` stagger. Below the fold,
  show skeleton blocks with the same sizes as the real cards (`rounded-[2rem]` 16:9 block + three `rounded-3xl`
  blocks; portrait-shaped blocks in the people section) in `bg-[#E2F0FA] dark:bg-[#16364C]` with `animate-pulse`.
- **Empty**: an activity section with no items is not rendered (and its nav link hidden). The teams explorer hides
  a tab with zero teams. The gallery section is not rendered with zero photos.
- **Missing images**: `Portrait`/`Avatar` fall back to initials; card images keep their aspect box with the soft blue
  fill.
- **Errors**: log like the current page does; never block the rest of the page.

---

## 10. Current home page features to keep, and decisions

The current `HomePage.jsx` does more than the preview shows. None of this may be silently dropped.

| Feature today | Default in the new design |
| --- | --- |
| `VITE_ENABLE_*` flags per section (ABOUT, COMMITTEES, CLUBS, GALLERY, EVENTS, PROGRAMS, RADIO, SERVICES, PARTNERS, EXCLUSIVE_FORMS) | Keep. Each new section renders only if its flag is not `"false"`. People = ABOUT or COMMITTEES or CLUBS (explorer tabs follow COMMITTEES / CLUBS). |
| Event / club / program detail sidebars (`EventDetailsSidebar`, `ClubDetailsSidebar`, `ProgramDetailsSidebar`) | Keep mounted. Featured and small event/program cards open the sidebar for that item instead of navigating; the "See all" CTA still goes to the list page. Team chips in the explorer keep their behaviour; add a "Details" button on a selected club that opens `ClubDetailsSidebar`. |
| Deep links `?openEventId`, `?openClubId`, `?openProgramId`, `?openCommitteeId`, `?openExclusiveFormId` (used after login redirects) | Keep the existing effect as is. |
| Committee registration (`RegistrationModal`, apply button, `myCommitteeId` logic) | Keep. In the teams explorer, a selected committee that is open shows an "Apply" primary button (hidden for the user's own committee) that opens the modal. |
| Exclusive forms (`ActiveFormsSection`, floating "Exclusive Opportunities" button) | Keep behind its flag. Render the section restyled as a white `rounded-[2rem]` card block right after the gallery; restyle the floating button with the primary tokens (`#205E85`, `rounded-xl`). |
| Services section | Keep behind its flag, after Radio, restyled with the page tokens and `Reveal`. |
| Partners | Keep behind its flag, before the footer, as a second `Marquee` of partner logos on a white band. |
| Footer newsletter, "Help us grow" modal, HR feedback modal, navigation and resources columns | Keep the features; restyle the footer to the `PreviewFooter` look and place these as a second row (newsletter field + links) above the logo/social row. |
| Active section indicator in the navbar | Optional; if kept, use the existing scroll logic and show a soft pill behind the active link. |
| `AOS` init (`disable: true`) | Remove from the home page; it is not used. |
| Signed-in users and the "Join ACM Alexandria" CTA | Keep the CTA text and `/register` link for signed-out users. For signed-in users point it to `/profile` with the text "Go to your profile". The beam still targets whatever button is rendered. |

---

## 11. Implementation steps

1. Create `components/Home/home.css` from `previewStyles.css`; create `hooks/useInView.js`,
   `hooks/useScrollFrame.js`, `hooks/useSmoothScroll.js` from the reference.
2. Port the UI pieces (`Marquee`, `OrgTree`, `Portrait`, `Avatar`, `Reveal`, `CountUp`) and `homeFormat.js`;
   fix imports.
3. Port the scene: `pharosGeometry.js`, `Pharos.jsx`, `NightSky.jsx`, `HeroIntro.jsx`, `CommitteeOrbit.jsx`,
   `PharosFinale.jsx`, `HeroPharos.jsx`. Replace `liveHelpers` imports with `homeFormat`; replace the CTA link with
   the auth-aware one (section 10).
4. Write `homeData.js` (9.2) and verify field names against real responses.
5. Split `YourLayout.jsx` into the section and card components; they take data via props.
6. Build `IslandNav` with the additions in 5.1, and `HomeFooter` (5.7 + section 10).
7. Rewrite `pages/HomePage.jsx`: load data (9.1), normalise, keep the sidebars, modal, deep links, exclusive forms,
   flags; render in the order of section 5; call `useSmoothScroll()`; import `home.css`.
8. Remove the preview route and folder, and unused old home sections (section 2).
9. Run `npm run lint` in `frontend/` and fix everything.
10. Walk through the acceptance checklist.

---

## 12. Acceptance checklist

Check at 375 × 812, 768 × 1024, 1024 × 768, 1440 × 900, in light and dark mode:

- [ ] No horizontal page scroll at any width (`document.documentElement.scrollWidth === innerWidth`).
- [ ] Hero fits one screen on a 375 × 812 phone; buttons side by side; chips are logo-only below 640px.
- [ ] Scrolling the scene: rings crack, night falls, blueprint draws, planets fall from where they are in orbit,
      merge into tiers bottom-up with ripples, tiers fill behind the scan line.
- [ ] Flame ignites after the last tier; the beam sweeps down; each word lights only when the beam reaches it.
- [ ] The beam comes to rest centred on the Join button; the button fades in as the beam arrives, not after.
- [ ] Scrolling back up reverses every step exactly.
- [ ] The sentence sits left of the lighthouse on phones and tablets too, and the CTA caption never overlaps the tower.
- [ ] Navbar: full-width transparent at the top, floating bar after half a screen, 300ms both ways; theme toggle,
      auth menu and phone menu work.
- [ ] Gallery slides continuously and pauses on hover.
- [ ] High board tree lines draw in once; explorer tabs and chips switch teams with the rise animation.
- [ ] Activity sections: intro sticky on desktop; featured card zooms in; small cards settle one after another; on
      phones the small cards swipe horizontally and are never clipped.
- [ ] Counts use the real totals; featured event is the newest.
- [ ] Detail sidebars, registration modal, deep links and exclusive forms still work.
- [ ] With `prefers-reduced-motion: reduce`: no pinning, no loops, the finished lit lighthouse is shown, Lenis is off.
- [ ] `npm run lint` passes; no console errors; no per-frame React re-renders (React DevTools profiler idle while
      scrolling the scene).
- [ ] `liveSnapshot.json` and the preview folder are gone and nothing imports them.
