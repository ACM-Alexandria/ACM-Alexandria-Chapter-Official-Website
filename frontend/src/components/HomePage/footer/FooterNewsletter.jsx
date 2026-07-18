import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../contexts/AuthContext";
import UnsubscribeConfirmModal from "../../UnsubscribeConfirmModal";
import {
  subscribeToNews,
  unsubscribeFromNews,
  fetchNewsSubscriptionStatus,
} from "../../../services/homePageService";

const FooterNewsletter = ({ onHelpUsGrowClick }) => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [subscribed, setSubscribed] = useState(false);
  const [loading, setLoading] = useState(false);
  const [statusLoading, setStatusLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);

  useEffect(() => {
    const checkStatus = async () => {
      if (!isAuthenticated) return;
      setStatusLoading(true);
      try {
        const data = await fetchNewsSubscriptionStatus();
        setSubscribed(data.subscribed);
      } catch (err) {
        console.error("Error fetching news status:", err);
      } finally {
        setStatusLoading(false);
      }
    };
    checkStatus();
  }, [isAuthenticated]);

  const handleToggleSubscribe = async () => {
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }
    setModalOpen(true);
  };

  const handleConfirmToggle = async () => {
    setLoading(true);
    try {
      if (subscribed) {
        await unsubscribeFromNews();
        setSubscribed(false);
      } else {
        await subscribeToNews();
        setSubscribed(true);
      }
      setModalOpen(false);
    } catch (err) {
      console.error("Error toggling news subscription:", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-8 text-center md:text-left">
      <h4 className="text-sm font-black uppercase tracking-[0.15em] text-white">
        Newsletter
      </h4>
      <div className="space-y-5">
        <p className="text-blue-100/80 text-sm font-medium leading-relaxed">
          {isAuthenticated
            ? "Stay updated with our latest news and upcoming technical events."
            : "Login to stay updated with our latest news and upcoming technical events."}
        </p>
        <div className="space-y-3">
          {isAuthenticated ? (
            <button
              onClick={handleToggleSubscribe}
              disabled={loading || statusLoading}
              className={`w-full font-bold py-3 rounded-xl shadow-lg shadow-black/20 uppercase text-[10px] tracking-[0.2em] transition-all cursor-pointer ${
                subscribed
                  ? "bg-[#ef4444]/80 hover:bg-[#ef4444] text-white"
                  : "bg-[#4B98C8] hover:bg-[#4B98C8]/80 text-white"
              }`}
            >
              {loading || statusLoading
                ? "Processing..."
                : subscribed
                ? "Unsubscribe from News"
                : "Subscribe to News"}
            </button>
          ) : (
            <button
              onClick={handleToggleSubscribe}
              className="w-full bg-[#4B98C8] hover:bg-[#4B98C8]/80 text-white font-bold py-3 rounded-xl shadow-lg shadow-black/20 uppercase text-[10px] tracking-[0.2em] cursor-pointer transition-all"
            >
              Login to Subscribe
            </button>
          )}
          <button
            type="button"
            onClick={onHelpUsGrowClick}
            className="w-full bg-white/10 hover:bg-white/20 text-white font-bold py-3 rounded-xl border border-white/10 uppercase text-[10px] tracking-[0.2em] cursor-pointer transition-all flex items-center justify-center gap-2 mt-3"
          >
            Help Us Grow
          </button>
          <button
            type="button"
            onClick={() => {
              const email = import.meta.env.VITE_CONTACT_EMAIL || "omarzydan610@gmail.com";
              window.open(`https://mail.google.com/mail/?view=cm&fs=1&to=${email}`, "_blank");
            }}
            className="w-full bg-white/10 hover:bg-white/20 text-white font-bold py-3 rounded-xl border border-white/10 uppercase text-[10px] tracking-[0.2em] cursor-pointer transition-all flex items-center justify-center gap-2 mt-3"
          >
            Contact Us
          </button>
        </div>
      </div>

      <UnsubscribeConfirmModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onConfirm={handleConfirmToggle}
        topicName="ACM Newsletter Updates"
        loading={loading}
        isSubscribe={!subscribed}
      />
    </div>
  );
};

export default FooterNewsletter;
