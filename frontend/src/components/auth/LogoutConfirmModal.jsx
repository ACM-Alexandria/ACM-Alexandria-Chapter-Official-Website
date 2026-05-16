import { useEffect, useState } from "react";
import { HiOutlineLogout } from "react-icons/hi";

const LogoutConfirmModal = ({ open, onCancel, onConfirm }) => {
  const [animate, setAnimate] = useState(false);

  // Dynamic transition scheduler to drive smooth fade & scale-in animations
  useEffect(() => {
    if (open) {
      const raf = requestAnimationFrame(() => setAnimate(true));
      return () => cancelAnimationFrame(raf);
    } else {
      setAnimate(false);
    }
  }, [open]);

  if (!open) return null;

  return (
    <div 
      className={`fixed inset-0 z-[999] flex items-center justify-center px-4 transition-all duration-500 ${
        animate ? "bg-slate-950/40 opacity-100" : "bg-transparent opacity-0"
      }`}
      onClick={onCancel} // Enables clicking backdrop mask to close safely
      role="dialog"
      aria-modal="true"
    >
      <div
        onClick={(e) => e.stopPropagation()} // Blocks closing event bubbling inside card
        className={`w-full max-w-sm bg-white rounded-[2.5rem] shadow-2xl border border-slate-100 p-8 text-center transition-all duration-500 transform ease-[cubic-bezier(0.34,1.56,0.64,1)] ${
          animate ? "scale-100 translate-y-0" : "scale-90 translate-y-8"
        }`}
      >
        {/* Elegant Logout Identity Graphic */}
        <div className="w-16 h-16 rounded-2xl bg-rose-50 text-rose-500 flex items-center justify-center mx-auto mb-5 animate-[pulse_2s_ease-in-out_infinite]">
          <HiOutlineLogout className="w-8 h-8" />
        </div>

        <h2 className="text-xl font-black text-slate-900 tracking-tight">
          Sign Out?
        </h2>
        <p className="mt-2 text-sm font-medium text-slate-500 px-3 leading-relaxed">
          Are you sure you want to securely terminate your current session?
        </p>

        {/* Visual Grid Controls */}
        <div className="mt-8 flex gap-3">
          <button
            type="button"
            className="flex-1 py-3.5 border border-slate-200 bg-slate-50 text-slate-600 font-bold rounded-2xl text-xs uppercase tracking-wider hover:bg-white hover:text-slate-900 hover:border-slate-300 transition-all active:scale-95"
            onClick={onCancel}
          >
            No, Stay
          </button>
          <button
            type="button"
            className="flex-1 py-3.5 bg-gradient-to-r from-[#205E85] to-slate-900 text-white font-bold rounded-2xl text-xs uppercase tracking-wider shadow-lg shadow-slate-200 hover:shadow-xl hover:-translate-y-0.5 transition-all active:scale-95"
            onClick={onConfirm}
          >
            Yes, Log Out
          </button>
        </div>
      </div>
    </div>
  );
};

export default LogoutConfirmModal;