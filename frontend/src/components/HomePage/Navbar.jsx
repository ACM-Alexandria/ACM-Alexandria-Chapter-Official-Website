import { Link } from "react-router-dom";
import logo from "../../assets/acm-logo.png";

const Navbar = () => {
  return (
    <nav className="w-full h-[70px] bg-[#205E85] flex items-center justify-between px-10 text-white fixed top-0 z-10">
      <div className="flex items-center gap-10">
        <img src={logo} className="h-10" alt="ACM Logo" />

        <ul className="list-none flex gap-6">
          <li>
            <a
              href="#about"
              className="no-underline text-white text-[15px] hover:opacity-80">
              About Us
            </a>
          </li>
          <li>
            <a
              href="#clubs"
              className="no-underline text-white text-[15px] hover:opacity-80">
              Clubs
            </a>
          </li>
          <li>
            <a
              href="#events"
              className="no-underline text-white text-[15px] hover:opacity-80">
              Events
            </a>
          </li>
          <li>
            <a
              href="#programs"
              className="no-underline text-white text-[15px] hover:opacity-80">
              Programs
            </a>
          </li>
          <li>
            <a
              href="#services"
              className="no-underline text-white text-[15px] hover:opacity-80">
              Services
            </a>
          </li>
        </ul>
      </div>

      <Link
        className="bg-white text-[#2c4a72] py-2 px-[18px] rounded-md no-underline font-bold hover:bg-gray-200"
        to="/login">
        Sign In
      </Link>
    </nav>
  );
};

export default Navbar;
