import ClubCard from "../cards/ClubCard";
import ExploreMoreButton from "./ExploreMoreButton";

const ClubsSection = ({ clubs }) => {
  // Display only first 3 clubs
  const displayedClubs = clubs && clubs.length > 0 ? clubs.slice(0, 3) : [];

  return (
    <section
      id="clubs"
      className="w-full py-20 px-10 bg-gray-50 flex items-center"
    >
      <div className="max-w-6xl mx-auto w-full">
        {/* Header Section */}
        <div className="text-center mb-12">
          <h2
            className="text-4xl font-bold text-gray-800 mb-4"
            data-aos="fade-down"
          >
            Our Clubs
          </h2>
          <div
            className="w-20 h-1 bg-gradient-to-r from-[#4B98C8] to-[#205E85] mx-auto mb-4"
            data-aos="fade-up"
          ></div>
          <p
            className="text-gray-600 text-lg max-w-2xl mx-auto"
            data-aos="fade-up"
          >
            Join one of our vibrant clubs and connect with like-minded students
            passionate about technology and innovation
          </p>
        </div>

        {/* Clubs Grid - Show only first 3 */}
        {displayedClubs && displayedClubs.length > 0 ? (
          <>
            <div
              className={`grid gap-8 mb-12 ${
                displayedClubs.length === 1
                  ? "grid-cols-1 max-w-sm mx-auto"
                  : displayedClubs.length === 2
                    ? "grid-cols-1 md:grid-cols-2 max-w-2xl mx-auto"
                    : "grid-cols-1 md:grid-cols-2 lg:grid-cols-3"
              }`}
            >
              {displayedClubs.map((club) => (
                <ClubCard key={club.id} club={club} />
              ))}
            </div>

            {/* Explore All Clubs Button */}
            {clubs && clubs.length > 3 && (
              <ExploreMoreButton text="Explore All Clubs" />
            )}
          </>
        ) : (
          <div className="text-center py-12">
            <p className="text-gray-500 text-lg">No clubs available yet.</p>
          </div>
        )}
      </div>
    </section>
  );
};

export default ClubsSection;
