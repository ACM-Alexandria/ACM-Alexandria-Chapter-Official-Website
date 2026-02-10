const ExploreMoreButton = ({ text = "Explore All", onClick }) => {
  return (
    <button
      onClick={onClick}
      className="bg-gradient-to-r from-[#4B98C8] to-[#205E85] hover:opacity-90 text-white font-semibold py-3 px-8 rounded-md transition-all duration-300 flex items-center justify-center gap-2 mx-auto"
      data-aos="fade-up"
    >
      {text}
      <svg
        className="w-5 h-5"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M13 7l5 5m0 0l-5 5m5-5H6"
        />
      </svg>
    </button>
  );
};

export default ExploreMoreButton;
