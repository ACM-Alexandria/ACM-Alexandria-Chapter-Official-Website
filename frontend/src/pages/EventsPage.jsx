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
                if (data && data.content !== undefined) {
                    setEvents(data.content);
                    setTotalPages(data.totalPages ?? 1);
                } else if (Array.isArray(data)) {
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
        <div className="min-h-screen flex flex-col bg-white">
            <Navbar />

            <main className="flex-1 pt-[74px]">
                {/* Page Header */}
                <div className="relative py-24 px-6 overflow-hidden bg-white">
                    {/* Background Blobs */}
                    <div className="absolute top-0 left-0 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-[#4B98C8]/5 rounded-full blur-3xl pointer-events-none" />
                    <div className="absolute bottom-0 right-0 translate-x-1/4 translate-y-1/4 w-[400px] h-[400px] bg-[#205E85]/5 rounded-full blur-3xl pointer-events-none" />
                    
                    <div className="relative z-10 max-w-4xl mx-auto text-center">
                        <h2
                            className="text-lg font-bold text-[#4B98C8] uppercase tracking-[0.2em] mb-4"
                            style={{ animation: "slideLeft 0.8s cubic-bezier(0.22,1,0.36,1) both" }}
                        >
                            Our Events
                        </h2>
                        <h1
                            className="text-5xl md:text-6xl font-extrabold text-slate-900 tracking-tight mb-8 leading-[1.1]"
                            style={{ animation: "slideLeft 0.8s cubic-bezier(0.22,1,0.36,1) 0.2s both" }}
                        >
                            Explore <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#4B98C8] to-[#205E85]">Excellence</span> in Computing
                        </h1>
                        <p
                            className="text-xl text-slate-500 font-medium max-w-2xl mx-auto leading-relaxed"
                            style={{ animation: "fadeIn 1s ease 0.6s both" }}
                        >
                            Stay updated with our latest workshops, technical sessions, and community events. 
                            Where learning meets innovation.
                        </p>
                    </div>
                </div>

                {/* Content */}
                <div className="max-w-7xl mx-auto px-6 py-20">
                    {loading ? (
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-10">
                            {Array.from({ length: 6 }).map((_, i) => (
                                <div
                                    key={i}
                                    className="bg-white rounded-[2.5rem] shadow-xl shadow-slate-200/50 overflow-hidden border border-slate-100 animate-pulse"
                                >
                                    <div className="bg-slate-100 h-64" />
                                    <div className="p-8 space-y-4">
                                        <div className="h-6 bg-slate-100 rounded-full w-3/4 mx-auto" />
                                        <div className="h-10 bg-slate-100 rounded-full w-12 mx-auto" />
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div
                            className={`transition-all duration-700 ease-out ${showPageContent ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}`}
                        >
                            {error ? (
                                <div className="text-center py-24 bg-red-50 rounded-[3rem] border border-red-100">
                                    <p className="text-red-500 font-bold text-lg">{error}</p>
                                </div>
                            ) : events.length === 0 ? (
                                <div className="text-center py-24 bg-slate-50 rounded-[3rem] border border-dashed border-slate-200">
                                    <p className="text-slate-400 font-medium text-lg">No events available yet.</p>
                                </div>
                            ) : (
                                <>
                                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-10">
                                        {events.map((event, index) => (
                                            <EventCard
                                                key={event.id}
                                                event={event}
                                                index={index}
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
