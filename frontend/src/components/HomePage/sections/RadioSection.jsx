import SeasonCard from "../cards/SeasonCard";
import ExploreMoreButton from "./ExploreMoreButton";

const RadioSection = ({ loading, seasons }) => {
  // Display only first 2 seasons (the most recent 2)
  const displayedSeasons =
    seasons && seasons.length > 0 ? seasons.slice(0, 2) : [];

  return (
    <section
      id="radio"
      className="w-full py-24 px-6 bg-slate-50 relative overflow-hidden"
    >
      {/* Decorative gradient blur background to distinguish the section */}
      <div className="absolute top-1/2 left-1/4 -translate-y-1/2 w-96 h-96 bg-[#4B98C8]/5 rounded-full blur-[100px] pointer-events-none" />
      <div className="absolute bottom-10 right-10 w-96 h-96 bg-[#205E85]/5 rounded-full blur-[100px] pointer-events-none" />

      <div className="max-w-7xl mx-auto w-full relative z-10">
        {/* Header Section */}
        <div className="text-center mb-20">
          <h2
            className="text-4xl md:text-5xl font-extrabold text-slate-900 tracking-tight mb-6"
            data-aos="fade-up"
          >
            Tune In to <span className="text-[#205E85]">ACM Radio</span>
          </h2>
          <p
            className="text-slate-500 text-lg max-w-2xl mx-auto font-medium"
            data-aos="fade-up"
            data-aos-delay="100"
          >
            Dive into our podcast seasons featuring technical insights, career guidance, and exclusive conversations with industry leaders and academic experts.
          </p>
        </div>

        {/* Seasons Grid */}
        {loading ? (
          <div className="grid grid-cols-1 lg:grid-cols-2 max-w-5xl mx-auto gap-12">
            {[...Array(2)].map((_, i) => (
              <div
                key={i}
                className="animate-pulse bg-white/70 backdrop-blur rounded-[2.5rem] h-96 border border-slate-200/50"
              />
            ))}
          </div>
        ) : displayedSeasons && displayedSeasons.length > 0 ? (
          <div className="space-y-16">
            <div
              className={`grid gap-12 ${
                displayedSeasons.length === 1
                  ? "grid-cols-1 max-w-xl mx-auto"
                  : "grid-cols-1 lg:grid-cols-2 max-w-5xl mx-auto"
              }`}
            >
              {displayedSeasons.map((season, index) => (
                <SeasonCard key={season.id} season={season} index={index} />
              ))}
            </div>

            {/* Explore All Seasons Button */}
            {seasons && seasons.length > 2 && (
              <div className="flex justify-center" data-aos="fade-up">
                <ExploreMoreButton text="Explore All Seasons" to="/radio" />
              </div>
            )}
          </div>
        ) : (
          <div className="py-20 text-center bg-white rounded-[3rem] border border-dashed border-slate-200 max-w-xl mx-auto">
            <p className="text-slate-400 font-medium text-lg">No seasons available yet.</p>
          </div>
        )}
      </div>
    </section>
  );
};

export default RadioSection;
