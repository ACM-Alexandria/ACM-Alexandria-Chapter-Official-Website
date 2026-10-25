// Twinkling stars and a rare shooting star. Shown only in dark mode, behind the hero content.
const stars = Array.from({ length: 110 }, (_, i) => ({
  x: (i * 37.7) % 100,
  y: (i * 61.3) % 100,
  size: i % 9 === 0 ? 3 : i % 3 === 0 ? 2 : 1.5,
  delay: (i % 7) * 0.65,
}));

const NightSky = () => (
  <div aria-hidden="true" className="pointer-events-none absolute inset-0 hidden overflow-hidden dark:block">
    {stars.map((s, i) => (
      <span
        key={i}
        className="absolute rounded-full bg-[#D3E9F7]"
        style={{ left: `${s.x}%`, top: `${s.y}%`, width: s.size, height: s.size, animation: `hp-twinkle ${3.5 + s.delay}s ease-in-out ${s.delay}s infinite` }}
      />
    ))}
    <span className="absolute right-[12%] top-[14%] h-px w-28 bg-gradient-to-l from-white to-transparent" style={{ animation: "hp-shoot 14s ease-out 4s infinite" }} />
  </div>
);

export default NightSky;
