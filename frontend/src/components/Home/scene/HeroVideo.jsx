// The welcome video behind the hero: a muted, seamless loop in the site's blue (its source project is kept outside
// the repo). It fills its box edge to edge without stretching: the desktop hero, where a box narrower than 16:9
// crops its sides, or the 16:9 frame of the phone and tablet hero, where it shows whole. It sits under a blue veil
// so the headline over it stays readable. Phones get the 720p file, and with reduced motion it shows
// the poster and doesn't play. The ref is the <video>, so the scene can pause it once night covers it.
const small = typeof window !== "undefined" && window.matchMedia("(max-width: 767px)").matches;
const still = typeof window !== "undefined" && window.matchMedia("(prefers-reduced-motion: reduce)").matches;

const HeroVideo = ({ ref }) => (
  <div aria-hidden="true" className="pointer-events-none absolute inset-0 overflow-hidden">
    <video
      ref={ref}
      className="absolute inset-0 h-full w-full object-cover"
      src={small ? "/video/welcome-720.mp4" : "/video/welcome-1080.mp4"}
      poster="/video/welcome-poster.jpg"
      autoPlay={!still}
      muted
      loop
      playsInline
      preload={still ? "none" : "auto"}
    />
    <div className="absolute inset-0 bg-ocean-794/60 dark:bg-ocean-910/70" />
  </div>
);

export default HeroVideo;
