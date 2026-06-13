import ProgramCard from "../cards/ProgramCard";
import ExploreMoreButton from "./ExploreMoreButton";

const ProgramsSection = ({ loading, programs }) => {
  // Display only first 2 programs
  const displayedPrograms =
    programs && programs.length > 0 ? programs.slice(0, 2) : [];

  return (
    <section
      id="programs"
      className="w-full py-24 px-6 bg-white relative overflow-hidden"
    >
      
      <div className="max-w-7xl mx-auto w-full relative z-10">
        {/* Header Section */}
        <div className="text-center mb-20">
          <h2 
            className="text-4xl md:text-5xl font-extrabold text-slate-900 tracking-tight mb-6"
            data-aos="fade-up"
          >
            Empower Your <span className="text-[#205E85]">Skills</span>
          </h2>
          <p
            className="text-slate-500 text-lg max-w-2xl mx-auto font-medium"
            data-aos="fade-up"
            data-aos-delay="100"
          >
            Explore our programs designed to enhance your skills and knowledge
            in various aspects of computer science and technology.
          </p>
        </div>

        {/* Programs Grid - Show only first 2 */}
        {loading ? (
          <div className="grid grid-cols-1 lg:grid-cols-2 max-w-7xl mx-auto gap-12">
            {[...Array(2)].map((_, i) => (
              <div key={i} className="animate-pulse bg-slate-100 rounded-[2.5rem] h-60 border border-slate-200/50" />
            ))}
          </div>
        ) : displayedPrograms && displayedPrograms.length > 0 ? (
          <div className="space-y-16">
            <div
              className={`grid gap-12 ${
                displayedPrograms.length === 1
                  ? "grid-cols-1 max-w-4xl mx-auto"
                  : "grid-cols-1 lg:grid-cols-2 max-w-7xl mx-auto"
              }`}
            >
              {displayedPrograms.map((program, index) => (
                <ProgramCard key={program.id} program={program} index={index} />
              ))}
            </div>

            {/* Explore All Programs Button */}
            {programs && programs.length > 2 && (
              <div className="flex justify-center" data-aos="fade-up">
                <ExploreMoreButton text="Explore All Programs" />
              </div>
            )}
          </div>
        ) : (
          <div className="py-20 text-center bg-gray-50 rounded-[3rem] border border-dashed border-slate-200">
            <p className="text-slate-400 font-medium text-lg">No programs available yet.</p>
          </div>
        )}
      </div>
    </section>
  );
};

export default ProgramsSection;
