const Footer = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-gradient-to-br from-[#205E85] to-[#1a4563] text-white w-full">
      {/* Main Footer Content */}
      <div className="container mx-auto px-4 md:px-8 lg:px-16 py-16">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-12">
          {/* Brand Section */}
          <div className="space-y-4">
            <h3 className="text-xl font-bold">
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#4B98C8] to-[#90B8D4]">
                ACM Alexandria
              </span>
            </h3>
            <p className="text-blue-100 text-sm leading-relaxed">
              Empowering students through technology, innovation, and
              professional growth.
            </p>
            {/* Social Links */}
            <div className="flex space-x-4 pt-4">
              <a
                href="#"
                className="w-10 h-10 rounded-full bg-[#4B98C8] hover:bg-[#6BA8D8] flex items-center justify-center transition-colors duration-300"
                aria-label="Facebook"
              >
                <span className="text-lg">f</span>
              </a>
              <a
                href="#"
                className="w-10 h-10 rounded-full bg-[#4B98C8] hover:bg-[#6BA8D8] flex items-center justify-center transition-colors duration-300"
                aria-label="LinkedIn"
              >
                <span className="text-lg">in</span>
              </a>
              <a
                href="#"
                className="w-10 h-10 rounded-full bg-[#4B98C8] hover:bg-[#6BA8D8] flex items-center justify-center transition-colors duration-300"
                aria-label="Instagram"
              >
                <span className="text-lg">ig</span>
              </a>
              <a
                href="#"
                className="w-10 h-10 rounded-full bg-[#4B98C8] hover:bg-[#6BA8D8] flex items-center justify-center transition-colors duration-300"
                aria-label="Twitter"
              >
                <span className="text-lg">x</span>
              </a>
            </div>
          </div>

          {/* Quick Links */}
          <div className="space-y-4">
            <h4 className="text-lg font-semibold text-[#4B98C8]">
              Quick Links
            </h4>
            <ul className="space-y-3 text-sm">
              <li>
                <a
                  href="#about"
                  className="text-blue-100 hover:text-white transition-colors duration-300"
                >
                  About Us
                </a>
              </li>
              <li>
                <a
                  href="#clubs"
                  className="text-blue-100 hover:text-white transition-colors duration-300"
                >
                  Our Clubs
                </a>
              </li>
              <li>
                <a
                  href="#events"
                  className="text-blue-100 hover:text-white transition-colors duration-300"
                >
                  Events
                </a>
              </li>
              <li>
                <a
                  href="#programs"
                  className="text-blue-100 hover:text-white transition-colors duration-300"
                >
                  Programs
                </a>
              </li>
              <li>
                <a
                  href="#services"
                  className="text-blue-100 hover:text-white transition-colors duration-300"
                >
                  Services
                </a>
              </li>
            </ul>
          </div>

          {/* Resources */}
          <div className="space-y-4">
            <h4 className="text-lg font-semibold text-[#4B98C8]">Resources</h4>
            <ul className="space-y-3 text-sm">
              <li>
                <a
                  href="#"
                  className="text-blue-100 hover:text-white transition-colors duration-300"
                >
                  Join Community
                </a>
              </li>
              <li>
                <a
                  href="/sponsors"
                  className="text-blue-100 hover:text-white transition-colors duration-300"
                >
                  Our Sponsors
                </a>
              </li>
              <li>
                <a
                  href="/partners"
                  className="text-blue-100 hover:text-white transition-colors duration-300"
                >
                  Partners
                </a>
              </li>
              <li>
                <a
                  href="#"
                  className="text-blue-100 hover:text-white transition-colors duration-300"
                >
                  Contact
                </a>
              </li>
            </ul>
          </div>

          {/* Contact Section */}
          <div className="space-y-4">
            <h4 className="text-lg font-semibold text-[#4B98C8]">
              Get in Touch
            </h4>
            <div className="space-y-3 text-sm">
              <div>
                <p className="text-blue-100 mb-1">Email</p>
                <a
                  href="mailto:alexandria.acm.chapter@gmail.com"
                  className="text-[#90B8D4] hover:text-white transition-colors duration-300"
                >
                  alexandria.acm.chapter@gmail.com
                </a>
              </div>
              <button
                className="w-full bg-gradient-to-r from-[#4B98C8] to-[#205E85] hover:shadow-lg text-white font-semibold py-2 px-4 rounded-lg transition-all duration-300"
                onClick={() => console.log("Open mailing list modal")}
              >
                Join Our Mailing List
              </button>
            </div>
          </div>
        </div>

        {/* Divider */}
        <div className="border-t border-[#4B98C8] border-opacity-30 my-12"></div>

        {/* Footer Bottom */}
        <div className="flex flex-col md:flex-row items-center justify-between text-sm text-blue-100">
          <p>© {currentYear} ACM Alexandria Chapter. All rights reserved.</p>
          <div className="flex space-x-6 mt-4 md:mt-0">
            <a
              href="#"
              className="hover:text-white transition-colors duration-300"
            >
              Privacy Policy
            </a>
            <a
              href="#"
              className="hover:text-white transition-colors duration-300"
            >
              Terms of Service
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
