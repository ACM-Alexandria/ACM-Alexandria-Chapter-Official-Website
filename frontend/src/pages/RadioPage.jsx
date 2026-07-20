import { useSearchParams, useNavigate, Link } from "react-router-dom";
import Navbar from "../components/HomePage/Navbar";
import Footer from "../components/HomePage/Footer";
import SeasonCard from "../components/HomePage/cards/SeasonCard";
import { fetchSeasons } from "../services/homePageService";
import { FiChevronLeft, FiChevronRight, FiMic, FiArrowLeft } from "react-icons/fi";
import { useState, useEffect, useCallback } from "react";

const RadioPage = () => {
  const [searchParams] = useSearchParams();
  const [seasonsPage, setSeasonsPage] = useState({ content: [], totalPages: 1, number: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const urlPage = Number(searchParams.get("page") || 1);
  const currentPage = Math.max(0, urlPage - 1);
  const totalPages = seasonsPage.totalPages || 1;

  const loadSeasons = useCallback(async (page = 0) => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchSeasons(page);
      setSeasonsPage(data);
    } catch (err) {
      console.error("Failed to fetch seasons:", err);
      setError("Unable to load radio seasons right now. Please try again.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSeasons(currentPage);
  }, [loadSeasons, currentPage]);

  const handlePageChange = (pageIndex) => {
    if (pageIndex === currentPage) return;
    window.location.href = `/radio?page=${pageIndex + 1}`;
  };

  const seasonsList = seasonsPage.content || [];

  return (
    <div className="min-h-screen flex flex-col bg-white">
      <Navbar activeSection="" />

      <main className="flex-1 pt-[74px]">
        {/* Page Header */}
        <div className="relative py-24 px-6 overflow-hidden bg-white">
          {/* Back to Home Button */}
          <div className="absolute top-6 left-6 z-20">
            <Link 
              to="/" 
              className="inline-flex items-center gap-2 px-4 py-2 rounded-2xl bg-slate-50 border border-slate-200 text-slate-600 hover:text-[#4B98C8] hover:border-[#4B98C8]/30 hover:bg-[#4B98C8]/5 hover:shadow-lg hover:shadow-blue-50 transition-all duration-300 font-bold text-sm group"
            >
              <FiArrowLeft className="w-4 h-4 transition-transform group-hover:-translate-x-1" />
              Back to Home
            </Link>
          </div>

          {/* Background Blobs */}
          <div className="absolute top-0 left-0 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-[#4B98C8]/5 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute bottom-0 right-0 translate-x-1/4 translate-y-1/4 w-[400px] h-[400px] bg-[#205E85]/5 rounded-full blur-3xl pointer-events-none" />
          
          <div className="relative z-10 max-w-4xl mx-auto text-center">
            <h2 className="text-lg font-bold text-[#4B98C8] uppercase tracking-[0.2em] mb-4">
              ACM Podcast
            </h2>
            <h1 className="text-5xl md:text-6xl font-extrabold text-slate-900 tracking-tight mb-8 leading-[1.1]">
              Explore Our <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#4B98C8] to-[#205E85]">Radio Seasons</span>
            </h1>
            <p className="text-xl text-slate-500 font-medium max-w-2xl mx-auto leading-relaxed">
              Listen to episodes containing deep-dive technical insights, career guidance, and academic reviews hosted by ACM Alexandria.
            </p>
          </div>
        </div>

        {/* Content */}
        <div className="max-w-6xl mx-auto px-6 py-12">
          {/* Error State */}
          {error && !loading && (
            <div className="flex flex-col items-center justify-center py-24 text-center">
              <div className="w-20 h-20 bg-red-50 rounded-full flex items-center justify-center text-red-500 mb-6 font-bold text-3xl">
                !
              </div>
              <p className="text-xl font-extrabold text-slate-900 mb-2">Something went wrong</p>
              <p className="text-slate-400 text-sm mb-6">{error}</p>
              <button
                onClick={() => loadSeasons(currentPage)}
                className="px-6 py-3 bg-[#4B98C8] text-white font-bold rounded-2xl hover:bg-[#205E85] transition-colors"
              >
                Try Again
              </button>
            </div>
          )}

          {/* Loading Skeleton */}
          {loading && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-10 max-w-5xl mx-auto">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="animate-pulse bg-slate-50 rounded-[2.5rem] border border-slate-100 overflow-hidden h-96" />
              ))}
            </div>
          )}

          {/* Seasons Grid */}
          {!loading && !error && (
            <>
              {seasonsList.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-24 text-center">
                  <div className="w-20 h-20 bg-slate-50 rounded-full flex items-center justify-center text-slate-300 mb-6">
                    <FiMic className="w-10 h-10" />
                  </div>
                  <p className="text-xl font-extrabold text-slate-900 mb-2">No seasons found</p>
                  <p className="text-slate-400 text-sm">
                    Our podcast radio is currently in production. Stay tuned!
                  </p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-10 max-w-5xl mx-auto">
                  {seasonsList.map((season, index) => (
                    <SeasonCard key={season.id} season={season} index={index} />
                  ))}
                </div>
              )}

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex items-center justify-center gap-4 mt-16">
                  <button
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={currentPage === 0}
                    className="p-3 bg-white border border-slate-200 text-slate-600 hover:text-[#4B98C8] hover:border-[#4B98C8] disabled:opacity-40 rounded-2xl transition-all shadow-sm active:scale-95"
                  >
                    <FiChevronLeft className="w-5 h-5" />
                  </button>

                  <div className="flex items-center gap-2">
                    {Array.from({ length: totalPages }).map((_, i) => (
                      <button
                        key={i}
                        onClick={() => handlePageChange(i)}
                        className={`w-10 h-10 rounded-xl text-sm font-extrabold transition-all ${
                          i === currentPage
                            ? "bg-[#4B98C8] text-white shadow-lg shadow-blue-200"
                            : "bg-white text-slate-500 hover:text-[#4B98C8] border border-slate-200"
                        }`}
                      >
                        {i + 1}
                      </button>
                    ))}
                  </div>

                  <button
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={currentPage >= totalPages - 1}
                    className="p-3 bg-white border border-slate-200 text-slate-600 hover:text-[#4B98C8] hover:border-[#4B98C8] disabled:opacity-40 rounded-2xl transition-all shadow-sm active:scale-95"
                  >
                    <FiChevronRight className="w-5 h-5" />
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </main>

      <Footer />
    </div>
  );
};

export default RadioPage;
