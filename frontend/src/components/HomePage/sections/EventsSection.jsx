import EventCard from "../cards/EventCard";
import ExploreMoreButton from "./ExploreMoreButton";

const EventsSection = ({ events }) => {
  // Display only first 2 events
  const displayedEvents = events && events.length > 0 ? events.slice(0, 2) : [];

  return (
    <section id="events" className="w-full py-20 px-10 flex items-center">
      <div className="max-w-6xl mx-auto w-full" data-aos="slide-right">
        {/* Header Section */}
        <div className="text-center mb-12">
          <h2
            className="text-4xl font-bold text-gray-800 mb-4"
            data-aos="fade-down"
          >
            Upcoming Events
          </h2>
          <p
            className="text-gray-600 text-lg max-w-2xl mx-auto"
            data-aos="fade-up"
          >
            Stay tuned for exciting events where you can learn, network, and
            grow with our community
          </p>
        </div>

        {/* Events Grid - Show only first 2 */}
        {displayedEvents && displayedEvents.length > 0 ? (
          <>
            <div
              className={`grid gap-8 mb-12 ${
                displayedEvents.length === 1
                  ? "grid-cols-1 max-w-4xl mx-auto"
                  : "grid-cols-1 md:grid-cols-2 max-w-6xl mx-auto"
              }`}
            >
              {displayedEvents.map((event) => (
                <EventCard key={event.id} event={event} />
              ))}
            </div>

            {/* Explore All Events Button */}
            {events && events.length > 2 && (
              <ExploreMoreButton text="Explore All Events" />
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
