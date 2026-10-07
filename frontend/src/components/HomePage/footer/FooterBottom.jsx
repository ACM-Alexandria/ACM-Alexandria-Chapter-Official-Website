import { Link } from "react-router-dom";

const FooterBottom = ({ currentYear, onNavigate }) => {
  return (
    <div className="flex flex-col md:flex-row items-center justify-between text-[10px] font-bold uppercase tracking-[0.2em] text-white/30 dark:text-white/40 gap-6 text-center">
      <p>© {currentYear} ACM Alexandria Student Chapter.</p>
      <div className="flex flex-wrap justify-center items-center gap-6 md:gap-8">
        <Link
          to="/rules"
          className="hover:text-white dark:hover:text-blue-100 transition-colors text-[10px] font-bold uppercase tracking-[0.2em] text-white/40 dark:text-white/50"
        >
          Rules
        </Link>
        {['Privacy', 'Terms', 'Cookies'].map((item) => (
          <button 
            key={item}
            type="button" 
            className="bg-transparent border-none p-0 cursor-pointer hover:text-white dark:hover:text-blue-100 transition-colors text-[10px] font-bold uppercase tracking-[0.2em] text-white/30 dark:text-white/40"
          >
            {item}
          </button>
        ))}
      </div>
    </div>
  );
};

export default FooterBottom;
