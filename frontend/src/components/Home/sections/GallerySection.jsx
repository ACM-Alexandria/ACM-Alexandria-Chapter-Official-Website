import Marquee from "../../ui/Marquee";

// The sliding row of chapter photos, right under the lighthouse scene so its blue flows into it.
const GallerySection = ({ images }) => (
  <section aria-label="Gallery" className="bg-[#0F3A5A] pb-14 pt-6 dark:bg-[#081826]">
    <Marquee label="Photos from chapter events" speed={90}>
      {images.map((src, i) => <img key={i} src={src} alt="" loading="lazy" className="mr-4 h-40 w-60 rounded-3xl object-cover ring-1 ring-white/10 sm:h-52 sm:w-80" />)}
    </Marquee>
  </section>
);

export default GallerySection;
