import CommitteeCard from "../cards/CommitteeCard";

const AboutSection = ({ committees = [] }) => {
  return (
    <section id="about" className="w-full py-20 px-10">
      <div className="max-w-6xl mx-auto">
        {/* Header Section */}
        <div className="text-center mb-12">
          <h2
            className="text-4xl font-bold text-gray-800 mb-4"
            data-aos="fade-right"
          >
            About Us
          </h2>
        </div>

        <div className="space-y-4 text-gray-700 mb-16">
          <p className="text-lg leading-relaxed" data-aos="fade-up">
            The Alexandria ACM Student Chapter is a vibrant community of passionate computer science students and technology enthusiasts. We are dedicated to advancing the field of computing through education, research, and professional development.
          </p>
          <p className="text-lg leading-relaxed" data-aos="fade-up" data-aos-delay="100">
            Our chapter serves as a bridge between academic learning and industry practices, providing our members with invaluable opportunities to grow, learn, and contribute to the ever-evolving world of technology.
          </p>
          <p className="text-lg leading-relaxed" data-aos="fade-up" data-aos-delay="200">
            Through collaborative projects, workshops, competitions, and networking events, we foster an environment where innovation thrives and lasting professional relationships are built.
          </p>
        </div>

        {/* Committees Subsection */}
        <div className="mt-16">
          <h3
            className="text-3xl font-bold text-gray-800 mb-8 text-center"
            data-aos="fade-right"
          >
            Our Committees
          </h3>
          
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
