import { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import Navbar from "../components/HomePage/Navbar";
import Footer from "../components/HomePage/Footer";
import { fetchSeasonById } from "../services/homePageService";
import { FiArrowLeft, FiPlay, FiMic, FiUser, FiExternalLink, FiLoader } from "react-icons/fi";

const SeasonDetailPage = () => {
  const { id } = useParams();
  const [season, setSeason] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadSeasonDetails = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await fetchSeasonById(id);
        setSeason(data);
      } catch (err) {
        console.error("Error loading season details:", err);
        setError("Could not load season details. Please try again later.");
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      loadSeasonDetails();
    }
  }, [id]);

  const episodes = season?.episodes 
    ? [...season.episodes].sort((a, b) => a.episodeNumber - b.episodeNumber) 
    : [];

  return (
    <div className="min-h-screen flex flex-col bg-slate-50">
      <Navbar activeSection="" />

      <main className="flex-1 pt-[74px]">
        {/* Page Header */}
        <div className="bg-white border-b border-slate-100 py-16 px-6 relative overflow-hidden">
          {/* Decorative gradients */}
          <div className="absolute top-0 right-0 w-96 h-96 bg-[#4B98C8]/5 rounded-full blur-[100px] pointer-events-none" />
          <div className="absolute bottom-0 left-0 w-96 h-96 bg-[#205E85]/5 rounded-full blur-[100px] pointer-events-none" />

          <div className="max-w-7xl mx-auto w-full relative z-10">
            {/* Back to Radio Page link */}
            <Link
              to="/radio"
              className="inline-flex items-center gap-2 text-slate-500 hover:text-[#4B98C8] text-xs font-bold uppercase tracking-wider transition-colors mb-6 group"
            >
              <FiArrowLeft className="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
              Back to Seasons
            </Link>

            {loading ? (
              <div className="h-20 animate-pulse bg-slate-100 rounded-2xl max-w-xl" />
            ) : season ? (
              <div className="flex flex-col md:flex-row items-center md:items-start gap-8">
                {/* Season Cover Image */}
                <div className="w-48 h-48 rounded-[2rem] overflow-hidden bg-slate-100 border border-slate-200/50 shadow-md shrink-0">
                  <img
                    src={season.imageUrl}
                    alt={`Season ${season.seasonNumber}`}
                    className="w-full h-full object-cover"
                  />
                </div>

                <div className="text-center md:text-left">
                  <span className="px-3 py-1 bg-sky-50 text-[#4B98C8] rounded-full text-[10px] font-extrabold uppercase tracking-wider border border-sky-100">
                    Podcast Season
                  </span>
                  <h1 className="text-4xl md:text-5xl font-extrabold text-slate-900 tracking-tight mt-3 mb-4">
                    Season {season.seasonNumber}
                  </h1>
                  <p className="text-slate-500 font-medium text-base max-w-2xl">
                    Listen to all {episodes.length} episodes of this season. Dive into our exclusive conversations and technical insights below.
                  </p>
                </div>
              </div>
            ) : null}
          </div>
        </div>

        {/* Content Section */}
        <div className="max-w-7xl mx-auto px-6 py-16">
          {loading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="animate-pulse flex flex-col bg-white rounded-3xl overflow-hidden border border-slate-100 h-80" />
              ))}
            </div>
          ) : error ? (
            <div className="text-center py-20 bg-white rounded-[2.5rem] border border-slate-200 max-w-md mx-auto">
              <p className="text-red-500 font-bold text-sm mb-4">{error}</p>
              <Link to="/radio" className="px-5 py-2.5 bg-[#4B98C8] text-white text-xs font-bold uppercase tracking-wider rounded-xl hover:bg-[#205E85] transition-colors">
                Go back
              </Link>
            </div>
          ) : episodes.length === 0 ? (
            <div className="text-center py-24 bg-white rounded-[3.5rem] border border-dashed border-slate-200 max-w-lg mx-auto">
              <FiMic className="w-12 h-12 text-slate-300 mx-auto mb-4" />
              <p className="text-slate-800 font-extrabold text-lg mb-2">No episodes published yet</p>
              <p className="text-slate-400 text-sm mb-6">This season is currently in preparation. Stay tuned!</p>
              <Link to="/radio" className="px-5 py-2.5 bg-slate-100 text-slate-600 text-xs font-bold uppercase tracking-wider rounded-xl hover:bg-slate-200 transition-colors">
                Back to Seasons
              </Link>
            </div>
          ) : (
            /* YouTube Thumbnail Style Grid - 4 columns on desktop */
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8">
              {episodes.map((ep, index) => (
                <div
                  key={ep.id}
                  className="group bg-white rounded-3xl overflow-hidden border border-slate-100 shadow-lg shadow-slate-100/50 hover:shadow-xl hover:shadow-slate-200/55 transition-all duration-300 flex flex-col h-full"
                  data-aos="fade-up"
                  data-aos-delay={index * 50}
                >
                  {/* YouTube style thumbnail container */}
                  <a
                    href={ep.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="relative block aspect-[16/9] w-full overflow-hidden bg-slate-100 shrink-0 border-b border-slate-50 cursor-pointer"
                  >
                    {/* Hover Play Overlay */}
                    <div className="absolute inset-0 bg-slate-900/40 opacity-0 group-hover:opacity-100 transition-opacity duration-300 z-10 flex items-center justify-center">
                      <div className="w-12 h-12 rounded-full bg-[#4B98C8] text-white flex items-center justify-center shadow-lg transform scale-90 group-hover:scale-100 transition-transform duration-300">
                        <FiPlay className="w-5 h-5 fill-white ml-0.5" />
                      </div>
                    </div>

                    <img
                      src={ep.imageUrl}
                      alt={ep.title}
                      className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                    />

                    {/* Episode Number badge overlay */}
                    <div className="absolute bottom-3 right-3 z-20">
                      <div className="px-2 py-0.5 bg-slate-900/75 backdrop-blur-sm text-white rounded text-[9px] font-extrabold tracking-widest uppercase">
                        Ep. {ep.episodeNumber}
                      </div>
                    </div>
                  </a>

                  {/* Thumbnail Info Block */}
                  <div className="p-5 flex flex-col flex-1">
                    {/* Title */}
                    <a
                      href={ep.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="block hover:text-[#4B98C8] transition-colors"
                    >
                      <h4 className="text-sm font-extrabold text-slate-800 line-clamp-2 leading-snug mb-3">
                        {ep.title}
                      </h4>
                    </a>

                    {/* Host & Guest Meta details */}
                    <div className="mt-auto space-y-1.5 pt-3 border-t border-slate-50 text-[11px] text-slate-500 font-bold">
                      <div className="flex items-center gap-1.5">
                        <FiUser className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                        <span>
                          Host: <span className="text-slate-700 font-extrabold">{ep.host || "N/A"}</span>
                        </span>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <FiMic className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                        <span>
                          Guest: <span className="text-slate-700 font-extrabold">{ep.guest || "N/A"}</span>
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>

      <Footer />
    </div>
  );
};

export default SeasonDetailPage;
