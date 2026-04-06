import EventPageCard from "../cards/EventPageCard";
import ExploreMoreButton from "./ExploreMoreButton";

const EventsSection = ({ events }) => {
  // Display only first 5 events
  const displayedEvents = events && events.length > 0 ? events.slice(0, 5) : [];

  return (
    <section id="events" className="w-full py-20 px-10 flex items-center">
      <div className="max-w-6xl mx-auto w-full">
        {/* Header Section */}
        <div className="text-center mb-12">
          <h2
            className="text-4xl font-bold text-gray-800 mb-4"
            data-aos="fade-down"
          >
            Upcoming Events
          </h2>
          <div
            className="w-20 h-1 bg-gradient-to-r from-[#4B98C8] to-[#205E85] mx-auto mb-4"
            data-aos="fade-up"
          ></div>
          <p
            className="text-gray-600 text-lg max-w-2xl mx-auto"
            data-aos="fade-up"
          >
            Stay tuned for exciting events where you can learn, network, and
            grow with our community
          </p>
        </div>

        {/* Events Grid - Show first 5 */}
        {displayedEvents && displayedEvents.length > 0 ? (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8 mb-12">
              {displayedEvents.map((event) => (
                <EventPageCard key={event.id} event={event} />
              ))}
            </div>

            {/* Explore All Events Button */}
            {events && events.length > 5 && (
              <ExploreMoreButton text="Explore All Events" to="/events" />
            )}
          </>
        ) : (
          <div className="text-center py-12">
            <p className="text-gray-500 text-lg">No events available yet.</p>
          </div>
        )}
      </div>
    </section>
  );
};

export default EventsSection;
