import EventCard from "../cards/EventCard";
import ExploreMoreButton from "./ExploreMoreButton";

const EventsSection = ({ events, onShowEventDetails }) => {
  // Display only first 3 events
  const displayedEvents = events && events.length > 0 ? events.slice(0, 3) : [];

  return (
    <section id="events" className="w-full py-24 px-6 bg-gray-50/50 relative overflow-hidden">
      {/* Background decoration */}
      <div className="absolute bottom-0 left-0 w-[600px] h-[600px] bg-[#4B98C8]/5 rounded-full blur-3xl pointer-events-none" />
      
      <div className="max-w-7xl mx-auto w-full relative z-10">
        {/* Header Section */}
        <div className="text-center mb-20">
          <h2
            className="text-lg font-bold text-[#4B98C8] uppercase tracking-[0.2em] mb-3"
            data-aos="fade-up"
          >
            Our Events
          </h2>
          <h3 
            className="text-4xl md:text-5xl font-extrabold text-slate-900 tracking-tight mb-6"
            data-aos="fade-up"
            data-aos-delay="100"
          >
            Experience <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#4B98C8] to-[#205E85]">Excellence</span>
          </h3>
          <p
            className="text-slate-500 text-lg max-w-2xl mx-auto font-medium"
            data-aos="fade-up"
            data-aos-delay="200"
          >
            Stay tuned for exciting events where you can learn, network, and
            grow with our community.
          </p>
        </div>

        {/* Events Grid - Show first 3 */}
        {displayedEvents && displayedEvents.length > 0 ? (
          <div className="space-y-16">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-10">
              {displayedEvents.map((event, index) => (
                <EventCard
                  key={event.id}
                  event={event}
                  index={index}
                  onShowDetails={onShowEventDetails}
                />
              ))}
            </div>

            {/* Explore All Events Button */}
            {events && events.length > 3 && (
              <div className="flex justify-center" data-aos="fade-up">
                <ExploreMoreButton text="Explore All Events" to="/events" />
              </div>
            )}
          </div>
        ) : (
          <div className="py-20 text-center bg-white rounded-[3rem] border border-dashed border-slate-200">
            <p className="text-slate-400 font-medium text-lg">No events available yet.</p>
          </div>
        )}
      </div>
    </section>
  );
};

export default EventsSection;
