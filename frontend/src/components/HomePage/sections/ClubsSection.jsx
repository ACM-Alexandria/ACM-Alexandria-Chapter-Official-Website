import ClubCard from "../cards/ClubCard";
import ExploreMoreButton from "./ExploreMoreButton";

const ClubsSection = ({ loading, clubs, onShowClubDetails }) => {
  // Display only first 2 clubs
  const displayedClubs = clubs && clubs.length > 0 ? clubs.slice(0, 2) : [];

  return (
    <section
      id="clubs"
      className="w-full py-24 px-6 bg-white relative overflow-hidden"
    >
      
      <div className="max-w-7xl mx-auto w-full relative z-10">
        {/* Header Section */}
        <div className="text-center mb-20">
          <h2 
            className="text-4xl md:text-5xl font-extrabold text-slate-900 tracking-tight mb-6"
            data-aos="fade-up"
          >
            Connect & <span className="text-[#205E85]">Collaborate</span>
          </h2>
          <p
            className="text-slate-500 text-lg max-w-2xl mx-auto font-medium"
            data-aos="fade-up"
            data-aos-delay="100"
          >
            Join one of our vibrant clubs and connect with like-minded students
            passionate about technology and innovation.
          </p>
        </div>

        {/* Clubs Grid - Show only first 3 */}
        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 max-w-4xl mx-auto gap-8">
            {[...Array(2)].map((_, i) => (
              <div key={i} className="animate-pulse bg-slate-100 rounded-[2.5rem] h-60 border border-slate-200/50" />
            ))}
          </div>
        ) : displayedClubs && displayedClubs.length > 0 ? (
          <div className="space-y-16">
            <div
              className={`grid gap-8 ${
                displayedClubs.length === 1
                  ? "grid-cols-1 max-w-sm mx-auto"
                  : displayedClubs.length === 2
                    ? "grid-cols-1 md:grid-cols-2 max-w-4xl mx-auto"
                    : "grid-cols-1 lg:grid-cols-2 max-w-5xl mx-auto"
              }`}
            >
              {displayedClubs.map((club, index) => (
                <ClubCard key={club.id} club={club} index={index} onShowDetails={onShowClubDetails} />
              ))}
            </div>

            {/* Explore All Clubs Button */}
            {clubs && clubs.length > 2 && (
              <div className="flex justify-center" data-aos="fade-up">
                <ExploreMoreButton text="Explore All Clubs" to="/clubs" />
              </div>
            )}
          </div>
        ) : (
          <div className="py-20 text-center bg-gray-50 rounded-[3rem] border border-dashed border-slate-200">
            <p className="text-slate-400 font-medium text-lg">No clubs available yet.</p>
          </div>
        )}
      </div>
    </section>
  );
};

export default ClubsSection;
