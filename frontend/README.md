# ACM Alexandria website: frontend

React 19 + Vite + Tailwind CSS 4. Runtime settings (API URL, contact email, Google client id) are read from
`public/config.js`; copy `public/config.example.js` to start.

```bash
npm install
npm run dev     # http://localhost:5173
npm run lint
npm run build
```

## Folder layout (`src/`)

| Folder | What lives there |
| --- | --- |
| `pages/` | One component per route (see `App.jsx`). |
| `components/Home/` | The home page: the hero and lighthouse scene (`scene/`), its sections (`sections/`) and cards (`cards/`), the data loader (`homeData.js`) and wording (`homeContent.js`). |
| `components/Layout/` | Site frame: `SiteLayout` (navbar, footer, smooth scrolling for public pages), `PageHero`, `IslandNav`, `SiteFooter`, and `siteSections` (the navbar links). |
| `components/ui/` | Shared building blocks in the site design: `Dialog`, `ConfirmDialog`, `SidePanel`, `RegistrationAction`, `MediaCard`, `Pagination`, `EmptyState`, `Marquee`, `OrgTree`, `Portrait`, `Reveal`, `CountUp`, and the class tokens in `uiStyles.js`. |
| `components/details/` | The event, club, program and exclusive form details panels. |
| `components/registration/` | The registration flow: profile check, quick confirm, questions, success. |
| `components/feedback/` | "Help us grow" and suggestions and complaints dialogs. |
| `components/auth/` | Sign in, sign up and the auth page layout. |
| `components/profile/` | Your own profile page: side card, tabs, details, edit form, points. |
| `components/AdminPage/`, `components/UserProfile/` | Admin dashboard and the member profile views admins open. |
| `contexts/` | `AuthProvider` and the `useAuth` hook. |
| `hooks/` | Shared hooks: smooth scrolling, overlays and scroll lock, paged lists, details loading, registration status. |
| `services/` | API calls (axios instance in `api.js`). |
| `utils/` | Formatting, validation, runtime config (`env.js`), and other plain helpers. |
| `styles/motion.css` | The site's keyframes (`hp-*`) and the reduced-motion switch. Global styles are in `index.css`. |

The design of the public site is described in `docs/home-page-redesign/DESIGN.md`.

## Colors

Every color lives in `src/styles/colors.css`, defined once as a Tailwind theme variable. Use it through utilities
(`bg-ocean-676`, `text-gold-180`, `dark:bg-ocean-871`) or, in CSS and inline styles, `var(--color-ocean-676)`.
Do not write a hex or `rgb()` value anywhere else. To change a color across the whole site, edit its line in
`colors.css`. The names are `ocean-N` (brand blues, N grows with darkness), `gold-N` (the lighthouse's light),
`ink-N` (cool greys), and `danger`, `chart` and `linkedin` for colors with a meaning of their own. Tailwind's own
palette (slate, red, emerald, amber) is still used for text greys and status colors.
