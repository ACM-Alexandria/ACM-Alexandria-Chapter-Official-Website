import logo from "../../assets/logo/acm-logo-no-bg.png";

const Navbar = () => {
  const navLinkClass =
    "no-underline text-white text-[22px] font-semibold hover:opacity-80";
  const navItems = [
    { href: "#about", label: "About Us" },
    { href: "#services", label: "Services" },
    { href: "#programs", label: "Programs" },
    { href: "#events", label: "Events" },
    { href: "#clubs", label: "Clubs" },
  ];

  return (
    <nav className="w-full h-[70px] bg-gradient-to-r from-[#4B98C8] to-[#205E85] flex items-center justify-between px-10 text-white fixed top-0 z-10 gap-12">
      <img src={logo} className="h-20" alt="ACM Logo" />

      <ul className="list-none flex gap-12 ml-auto">
        {navItems.map((item) => (
          <li key={item.href}>
            <a href={item.href} className={navLinkClass}>
              {item.label}
            </a>
          </li>
        ))}
      </ul>

      <a
        className="bg-white text-[#2c4a72] py-2 px-[18px] rounded-md no-underline font-bold hover:bg-gray-200"
        href="/login"
      >
        Sign In
      </a>
    </nav>
  );
};

export default Navbar;
