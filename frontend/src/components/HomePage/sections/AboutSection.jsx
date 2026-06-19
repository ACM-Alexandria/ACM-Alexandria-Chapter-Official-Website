import CommitteeCard from "../cards/CommitteeCard";
import HighBoardCard from "../cards/HighBoardCard";
import GallerySection from "./GallerySection";

const AboutSection = ({ loading, highBoard = [], committees = [], onApplyClick, isRegistrationModalOpen }) => {
  const isEnabled = (envVal) => envVal !== "false";

  const orderedHighBoard = [...highBoard].sort((a, b) => {
    const firstOrder = a?.order ?? Number.MAX_SAFE_INTEGER;
    const secondOrder = b?.order ?? Number.MAX_SAFE_INTEGER;
    return firstOrder - secondOrder;
  });

  return (
    <section id="about" className="w-full py-24 px-6 relative overflow-hidden bg-gray-50/50">
      
      <div className="max-w-7xl mx-auto relative z-10">
        {/* Header Section */}
        <div className="text-center mb-16">
          <h2 
            className="text-4xl md:text-5xl font-extrabold text-slate-900 tracking-tight"
            data-aos="fade-up"
          >
            Leading the Future of <span className="text-[#205E85]">Computing</span>
          </h2>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-start mb-24">
          <div className="space-y-6 text-slate-600">
            <p className="text-lg md:text-xl font-medium leading-relaxed" data-aos="fade-right">
              The Alexandria ACM Student Chapter is a vibrant community of
              passionate computer science students and technology enthusiasts. We
              are dedicated to advancing the field of computing through education,
              research, and professional development.
            </p>
            <p
              className="text-lg leading-relaxed opacity-80"
              data-aos="fade-right"
              data-aos-delay="100"
            >
              Our chapter serves as a bridge between academic learning and
              industry practices, providing our members with invaluable
              opportunities to grow, learn, and contribute to the ever-evolving
              world of technology.
            </p>
          </div>
          <div className="bg-white p-8 rounded-3xl shadow-xl shadow-slate-200/50 border border-slate-100" data-aos="fade-left">
            <p className="text-lg leading-relaxed text-slate-600 italic">
              "Through collaborative projects, workshops, competitions, and
              networking events, we foster an environment where innovation thrives
              and lasting professional relationships are built."
            </p>
            <div className="mt-6 flex items-center gap-2">
              <span className="inline-block px-3 py-1 bg-[#4B98C8]/10 text-[#205E85] font-bold rounded-lg text-xs uppercase tracking-wider">
                Our Mission
              </span>
            </div>
          </div>
        </div>

        {/* High Board Section */}
        {isEnabled(import.meta.env.VITE_ENABLE_HIGHBOARD) && (
          <div className="mt-24">
            <div className="flex flex-col md:flex-row items-center justify-between mb-12 gap-4">
              <div className="text-center md:text-left">
                <h3
                  className="text-3xl font-extrabold text-slate-900 tracking-tight mb-2"
                  data-aos="fade-right"
                >
                  High Board
                </h3>
                <p className="text-slate-500 font-medium" data-aos="fade-right" data-aos-delay="100">
                  The visionary leadership behind our chapter
                </p>
              </div>
              <div className="h-px flex-1 bg-slate-200 hidden md:block mx-8 opacity-50" />
            </div>

            {loading ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {[...Array(4)].map((_, i) => (
                  <div key={i} className="animate-pulse bg-slate-100 rounded-3xl h-72 border border-slate-200/50" />
                ))}
              </div>
            ) : orderedHighBoard.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {orderedHighBoard.map((member, index) => (
                  <HighBoardCard key={member.id || index} member={member} index={index} />
                ))}
              </div>
            ) : (
              <div className="py-12 text-center bg-white rounded-3xl border border-dashed border-slate-300">
                <p className="text-slate-400 font-medium">
                  No high board members available at the moment.
                </p>
              </div>
            )}
          </div>
        )}

        {/* Committees Section */}
        {isEnabled(import.meta.env.VITE_ENABLE_COMMITTEES) && (
          <div className="mt-24">
            <div className="flex flex-col md:flex-row items-center justify-between mb-12 gap-4">
              <div className="text-center md:text-left">
                <h3
                  className="text-3xl font-extrabold text-slate-900 tracking-tight mb-2"
                  data-aos="fade-right"
                >
                  Our Committees
                </h3>
                <p className="text-slate-500 font-medium" data-aos="fade-right" data-aos-delay="100">
                  The specialized teams driving our initiatives
                </p>
              </div>
              <div className="h-px flex-1 bg-slate-200 hidden md:block mx-8 opacity-50" />
            </div>

            {loading ? (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                {[...Array(2)].map((_, i) => (
                  <div key={i} className="animate-pulse bg-slate-100 rounded-3xl h-64 border border-slate-200/50" />
                ))}
              </div>
            ) : committees && committees.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                {committees.map((committee, index) => (
                  <div
                    key={committee.id || index}
                    className={
                      (committee?.boardRoles?.length || 0) > 2
                        ? "md:col-span-2"
                        : ""
                    }
                    data-aos="fade-up"
                    data-aos-delay={index * 100}
                  >
                    <CommitteeCard 
                      committee={committee} 
                      onApplyClick={onApplyClick} 
                      isRegistrationModalOpen={isRegistrationModalOpen}
                    />
                  </div>
                ))}
              </div>
            ) : (
              <div className="py-12 text-center bg-white rounded-3xl border border-dashed border-slate-300">
                <p className="text-slate-400 font-medium">
                  No committees available at the moment.
                </p>
              </div>
            )}
          </div>
        )}

        {/* Gallery Section */}
        {isEnabled(import.meta.env.VITE_ENABLE_GALLERY) && (
          <GallerySection />
        )}
      </div>
    </section>

  );
};

export default AboutSection;
