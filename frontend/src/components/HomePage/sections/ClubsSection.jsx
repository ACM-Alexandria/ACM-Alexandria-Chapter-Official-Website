import ClubCard from "../cards/ClubCard";
import ExploreMoreButton from "./ExploreMoreButton";

const ClubsSection = ({ clubs, onShowClubDetails }) => {
  // Display only first 2 clubs
  const displayedClubs = clubs && clubs.length > 0 ? clubs.slice(0, 2) : [];

  return (
    <section
      id="clubs"
      className="w-full py-24 px-6 bg-white relative overflow-hidden"
    >
      {/* Background decoration */}
      <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-[#205E85]/5 rounded-full blur-3xl pointer-events-none" />
      
      <div className="max-w-7xl mx-auto w-full relative z-10">
        {/* Header Section */}
        <div className="text-center mb-20">
          <h2
            className="text-lg font-bold text-[#4B98C8] uppercase tracking-[0.2em] mb-3"
            data-aos="fade-up"
          >
            Our Clubs
          </h2>
          <h3 
            className="text-4xl md:text-5xl font-extrabold text-slate-900 tracking-tight mb-6"
            data-aos="fade-up"
            data-aos-delay="100"
          >
            Connect & <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#4B98C8] to-[#205E85]">Collaborate</span>
          </h3>
          <p
            className="text-slate-500 text-lg max-w-2xl mx-auto font-medium"
            data-aos="fade-up"
            data-aos-delay="200"
          >
            Join one of our vibrant clubs and connect with like-minded students
            passionate about technology and innovation.
          </p>
        </div>

        {/* Clubs Grid - Show only first 3 */}
        {displayedClubs && displayedClubs.length > 0 ? (
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
