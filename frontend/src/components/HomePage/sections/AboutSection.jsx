import CommitteeCard from "../cards/CommitteeCard";
import HighBoardCard from "../cards/HighBoardCard";

const AboutSection = ({ highBoard = [], committees = [] }) => {
  const orderedHighBoard = [...highBoard].sort((a, b) => {
    const firstOrder = a?.order ?? Number.MAX_SAFE_INTEGER;
    const secondOrder = b?.order ?? Number.MAX_SAFE_INTEGER;
    return firstOrder - secondOrder;
  });

  return (
    <section id="about" className="w-full py-20 px-10">
      <div className="max-w-6xl mx-auto">
        {/* Header Section */}
        <div className="text-center mb-12">
          <h2
            className="text-4xl font-bold text-gray-800 mb-4"
            data-aos="fade-down"
          >
            About Us
          </h2>
          <div
            className="w-20 h-1 bg-linear-to-r from-[#4B98C8] to-[#205E85] mx-auto mb-4"
            data-aos="fade-up"
          ></div>
        </div>

        <div className="space-y-4 text-gray-700 mb-16">
          <p className="text-lg leading-relaxed" data-aos="fade-up">
            The Alexandria ACM Student Chapter is a vibrant community of
            passionate computer science students and technology enthusiasts. We
            are dedicated to advancing the field of computing through education,
            research, and professional development.
          </p>
          <p
            className="text-lg leading-relaxed"
            data-aos="fade-up"
            data-aos-delay="100"
          >
            Our chapter serves as a bridge between academic learning and
            industry practices, providing our members with invaluable
            opportunities to grow, learn, and contribute to the ever-evolving
            world of technology.
          </p>
          <p
            className="text-lg leading-relaxed"
            data-aos="fade-up"
            data-aos-delay="200"
          >
            Through collaborative projects, workshops, competitions, and
            networking events, we foster an environment where innovation thrives
            and lasting professional relationships are built.
          </p>
        </div>

        <div className="mt-16">
          <h3
            className="text-3xl font-bold text-gray-800 mb-4 text-center"
            data-aos="fade-down"
          >
            High Board
          </h3>
          <div
            className="w-20 h-1 bg-linear-to-r from-[#4B98C8] to-[#205E85] mx-auto mb-8"
            data-aos="fade-up"
          ></div>

          {orderedHighBoard.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              {orderedHighBoard.map((member, index) => (
                <HighBoardCard key={member.id || index} member={member} />
              ))}
            </div>
          ) : (
            <p className="text-center text-gray-500">
              No high board members available at the moment.
            </p>
          )}
        </div>

        {/* Committees Subsection */}
        <div className="mt-16">
          <h3
            className="text-3xl font-bold text-gray-800 mb-4 text-center"
            data-aos="fade-down"
          >
            Our Committees
          </h3>
          <div
            className="w-20 h-1 bg-linear-to-r from-[#4B98C8] to-[#205E85] mx-auto mb-8"
            data-aos="fade-up"
          ></div>

          {committees && committees.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
              {committees.map((committee, index) => (
                <CommitteeCard
                  key={committee.id || index}
                  committee={committee}
                />
              ))}
            </div>
          ) : (
            <p className="text-center text-gray-500">
              No committees available at the moment.
            </p>
          )}
        </div>
      </div>
    </section>
  );
};

export default AboutSection;
