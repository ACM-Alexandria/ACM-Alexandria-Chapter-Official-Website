import ProgramCard from "../cards/ProgramCard";
import ExploreMoreButton from "./ExploreMoreButton";

const ProgramsSection = ({ programs }) => {
  // Display only first 2 programs
  const displayedPrograms =
    programs && programs.length > 0 ? programs.slice(0, 2) : [];

  return (
    <section
      id="programs"
      className="w-full py-20 px-10 bg-gray-50 flex items-center"
    >
      <div className="max-w-6xl mx-auto w-full">
        {/* Header Section */}
        <div className="text-center mb-12">
          <h2
            className="text-4xl font-bold text-gray-800 mb-4"
            data-aos="fade-down"
          >
            Our Programs
          </h2>
          <div
            className="w-20 h-1 bg-gradient-to-r from-[#4B98C8] to-[#205E85] mx-auto mb-4"
            data-aos="fade-up"
          ></div>
          <p
            className="text-gray-600 text-lg max-w-2xl mx-auto"
            data-aos="fade-up"
          >
            Explore our programs designed to enhance your skills and knowledge
            in various aspects of computer science and technology
          </p>
        </div>

        {/* Programs Grid - Show only first 2 */}
        {displayedPrograms && displayedPrograms.length > 0 ? (
          <>
            <div
              className={`grid gap-8 mb-12 ${
                displayedPrograms.length === 1
                  ? "grid-cols-1 max-w-4xl mx-auto"
                  : "grid-cols-1 md:grid-cols-2 max-w-6xl mx-auto"
              }`}
            >
              {displayedPrograms.map((program) => (
                <ProgramCard key={program.id} program={program} />
              ))}
            </div>

            {/* Explore All Programs Button */}
            {programs && programs.length > 2 && (
              <ExploreMoreButton text="Explore All Programs" />
            )}
          </>
        ) : (
          <div className="text-center py-12">
            <p className="text-gray-500 text-lg">No programs available yet.</p>
          </div>
        )}
      </div>
    </section>
  );
};

export default ProgramsSection;
