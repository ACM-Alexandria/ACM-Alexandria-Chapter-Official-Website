import { useState, useEffect, useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import Navbar from "../components/HomePage/Navbar";
import Footer from "../components/HomePage/Footer";
import ProgramCard from "../components/HomePage/cards/ProgramCard";
import ProgramDetailsSidebar from "../components/HomePage/ProgramDetailsSidebar";
import { fetchPrograms } from "../services/homePageService";
import { FiChevronLeft, FiChevronRight } from "react-icons/fi";

const ProgramsPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [programsPage, setProgramsPage] = useState({ content: [], totalPages: 1, number: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [selectedProgramId, setSelectedProgramId] = useState(null);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  const currentPage = programsPage.number || 0;
  const totalPages = programsPage.totalPages || 1;

  const loadPrograms = useCallback(async (page = 0) => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchPrograms(page);
      setProgramsPage(data);
    } catch (err) {
      console.error("Failed to fetch programs:", err);
      setError("Unable to load programs right now. Please try again.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadPrograms(0);
  }, [loadPrograms]);

  // Handle openProgramId URL param (from login redirect)
  useEffect(() => {
    const openProgramId = searchParams.get("openProgramId");
    if (openProgramId) {
      setSelectedProgramId(Number(openProgramId));
      setIsSidebarOpen(true);
      const newParams = new URLSearchParams(searchParams);
      newParams.delete("openProgramId");
      setSearchParams(newParams, { replace: true });
    }
  }, [searchParams]);

  const handleShowDetails = (programId) => {
    setSelectedProgramId(programId);
    setIsSidebarOpen(true);
  };

  const handleCloseSidebar = () => {
    setIsSidebarOpen(false);
  };

  const handlePageChange = (newPage) => {
    loadPrograms(newPage);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const filteredPrograms = programsPage.content || [];

  return (
    <div className="min-h-screen flex flex-col bg-white">
      <Navbar activeSection="" />

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
              Our Programs
            </h2>
            <h1
              className="text-5xl md:text-6xl font-extrabold text-slate-900 tracking-tight mb-8 leading-[1.1]"
              style={{ animation: "slideLeft 0.8s cubic-bezier(0.22,1,0.36,1) 0.2s both" }}
            >
              Advance Your <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#4B98C8] to-[#205E85]">Skills & Knowledge</span>
            </h1>
            <p
              className="text-xl text-slate-500 font-medium max-w-2xl mx-auto leading-relaxed"
              style={{ animation: "fadeIn 1s ease 0.6s both" }}
            >
              Specialized academic and professional programs designed to take your computing knowledge to the next level.
            </p>
          </div>
        </div>

        {/* Content */}
        <div className="max-w-7xl mx-auto px-6 py-20">

          {/* Error State */}
          {error && !loading && (
            <div className="flex flex-col items-center justify-center py-24 text-center">
              <div className="w-20 h-20 bg-red-50 rounded-full flex items-center justify-center text-red-500 mb-6">
                <span className="text-3xl">!</span>
              </div>
              <p className="text-xl font-extrabold text-slate-900 mb-2">Something went wrong</p>
              <p className="text-slate-400 text-sm mb-6">{error}</p>
              <button
                onClick={() => loadPrograms(currentPage)}
                className="px-6 py-3 bg-[#4B98C8] text-white font-bold rounded-2xl hover:bg-[#205E85] transition-colors"
              >
                Try Again
              </button>
            </div>
          )}

          {/* Loading Skeleton */}
          {loading && (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 max-w-7xl mx-auto">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="animate-pulse flex flex-col md:flex-row bg-white rounded-[2.5rem] border border-slate-100 overflow-hidden h-72">
                  <div className="w-full md:w-2/5 bg-slate-200" />
                  <div className="flex-1 p-10 space-y-4">
                    <div className="h-7 bg-slate-200 rounded-full w-3/4" />
                    <div className="h-4 bg-slate-100 rounded-full w-full" />
                    <div className="h-4 bg-slate-100 rounded-full w-5/6" />
                    <div className="h-4 bg-slate-100 rounded-full w-4/6" />
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Programs Grid */}
          {!loading && !error && (
            <>
              {filteredPrograms.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-24 text-center">
                  <div className="w-20 h-20 bg-slate-50 rounded-full flex items-center justify-center text-slate-300 mb-6 text-4xl font-black">
                    P
                  </div>
                  <p className="text-xl font-extrabold text-slate-900 mb-2">No programs found</p>
                  <p className="text-slate-400 text-sm">
                    Check back later for upcoming programs.
                  </p>
                </div>
              ) : (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 max-w-7xl mx-auto">
                  {filteredPrograms.map((program, index) => (
                    <ProgramCard
                      key={program.id}
                      program={program}
                      index={index}
                      onShowDetails={handleShowDetails}
                    />
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

      <ProgramDetailsSidebar
        programId={selectedProgramId}
        isOpen={isSidebarOpen}
        onClose={handleCloseSidebar}
      />
    </div>
  );
};

export default ProgramsPage;
