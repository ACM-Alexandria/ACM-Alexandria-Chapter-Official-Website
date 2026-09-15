import ClubCard from "../cards/ClubCard";
import { getEnv } from "../../../utils/env";

const ClubsSection = ({ loading, clubs = [], onShowClubDetails }) => {
  const isEnabled = (envVal) => envVal !== "false";

  if (!isEnabled(getEnv("VITE_ENABLE_CLUBS"))) return null;

  return (
    <section id="clubs" className="w-full py-24 px-6 relative overflow-hidden bg-gray-50/50 dark:bg-slate-900">
      <div className="max-w-7xl mx-auto relative z-10">
        <div className="flex flex-col md:flex-row items-center justify-between mb-12 gap-4">
          <div className="text-center md:text-left">
            <h3
              className="text-3xl font-extrabold text-slate-900 dark:text-slate-100 tracking-tight mb-2"
              data-aos="fade-right"
            >
              Our Clubs
            </h3>
            <p className="text-slate-500 font-medium dark:text-slate-400" data-aos="fade-right" data-aos-delay="100">
              Specialized tech communities nurturing core expertise
            </p>
          </div>
          <div className="h-px flex-1 bg-slate-200 dark:bg-slate-600 hidden md:block mx-8 opacity-50" />
        </div>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            {[...Array(2)].map((_, i) => (
              <div key={i} className="animate-pulse bg-slate-100 dark:bg-slate-800 rounded-3xl h-64 border border-slate-200/50 dark:border-slate-700" />
            ))}
          </div>
        ) : clubs && clubs.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            {clubs.map((club, index) => (
              <div
                key={club.id || index}
                className={
                  (club?.boardRoles?.length || 0) > 2
                    ? "md:col-span-2"
                    : ""
                }
                data-aos="fade-up"
                data-aos-delay={index * 100}
              >
                <ClubCard club={club} onShowDetails={onShowClubDetails} />
              </div>
            ))}
          </div>
        ) : (
          <div className="py-12 text-center bg-white dark:bg-slate-800 rounded-3xl border border-dashed border-slate-300 dark:border-slate-600">
            <p className="text-slate-400 dark:text-slate-400 font-medium">
              No clubs available at the moment.
            </p>
          </div>
        )}
      </div>
    </section>
  );
};

export default ClubsSection;
