import { useState, useEffect } from "react";
import AOS from "aos";
import "aos/dist/aos.css";
import Navbar from "../components/HomePage/Navbar";
import Footer from "../components/HomePage/Footer";
import EventCard from "../components/HomePage/cards/EventCard";
import EventDetailsSidebar from "../components/HomePage/EventDetailsSidebar";
import Pagination from "../components/HomePage/Pagination";
import { fetchEvents } from "../services/homePageService";

const EventsPage = () => {
    const [events, setEvents] = useState([]);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showPageContent, setShowPageContent] = useState(false);
    const [selectedEventId, setSelectedEventId] = useState(null);
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);

    const handleShowEventDetails = (eventId) => {
        setSelectedEventId(eventId);
        setIsSidebarOpen(true);
    };

    const handleCloseSidebar = () => {
        setIsSidebarOpen(false);
    };

    useEffect(() => {
        AOS.init({ duration: 800, once: true, offset: 80 });
    }, []);

    useEffect(() => {
        const load = async () => {
            setLoading(true);
            setShowPageContent(false);
            setError(null);
            try {
                const data = await fetchEvents(currentPage);
                // Handle Spring Page response shape
                if (data && data.content !== undefined) {
                    setEvents(data.content);
                    setTotalPages(data.totalPages ?? 1);
                } else if (Array.isArray(data)) {
                    // Fallback: plain array (no pagination)
                    setEvents(data);
                    setTotalPages(1);
                }
            } catch (err) {
                console.error("Failed to load events:", err);
                setError("Failed to load events. Please try again later.");
            } finally {
                setLoading(false);
                requestAnimationFrame(() => setShowPageContent(true));
            }
        };
        load();
    }, [currentPage]);

    const handlePageChange = (page) => {
        if (page === currentPage) return;
        setCurrentPage(page);
    };

    return (
        <div className="min-h-screen flex flex-col bg-gray-50">
            <Navbar />

            <main className="flex-1 pt-[70px]">
                {/* Page Header */}
                <div className="bg-gradient-to-r from-[#4B98C8] to-[#205E85] py-16 px-6 text-center">
                    <h1
                        className="text-4xl md:text-5xl font-bold text-white mb-3"
                        data-aos="fade-down"
                    >
                        Our Events
                    </h1>
                    <div
                        className="w-20 h-1 bg-white opacity-50 mx-auto mb-4"
                        data-aos="fade-up"
                    />
                    <p
                        className="text-blue-100 text-lg max-w-xl mx-auto"
                        data-aos="fade-up"
                    >
                        Browse all of our past and upcoming events
                    </p>
                </div>

                {/* Content */}
                <div className="max-w-6xl mx-auto px-6 py-14">
                    {loading ? (
                        // Skeleton grid
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
                            {Array.from({ length: 6 }).map((_, i) => (
                                <div
                                    key={i}
                                    className="bg-white rounded-xl shadow-md overflow-hidden animate-pulse"
                                >
                                    <div className="bg-gray-200 h-52" />
                                    <div className="p-5 space-y-3">
                                        <div className="h-5 bg-gray-200 rounded w-3/4" />
                                        <div className="h-5 bg-gray-200 rounded w-1/2" />
                                        <div className="mt-4 h-10 bg-gray-200 rounded" />
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div
                            className={`transition-all duration-400 ease-out ${showPageContent ? "opacity-100 translate-y-0" : "opacity-0 translate-y-2"}`}
                        >
                            {error ? (
                                <div className="text-center py-20">
                                    <p className="text-red-500 text-lg">{error}</p>
                                </div>
                            ) : events.length === 0 ? (
                                <div className="text-center py-20">
                                    <p className="text-gray-500 text-lg">No events available yet.</p>
                                </div>
                            ) : (
                                <>
                                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
                                        {events.map((event) => (
                                            <EventCard
                                                key={event.id}
                                                event={event}
                                                onShowDetails={handleShowEventDetails}
                                            />
                                        ))}
                                    </div>

                                    <Pagination
                                        currentPage={currentPage}
                                        totalPages={totalPages}
                                        onPageChange={handlePageChange}
                                    />
                                </>
                            )}
                        </div>
                    )}
                </div>
            </main>

            <Footer />

            <EventDetailsSidebar
                eventId={selectedEventId}
                isOpen={isSidebarOpen}
                onClose={handleCloseSidebar}
            />
        </div>
    );
};

export default EventsPage;
