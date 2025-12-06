export const Footer = () => {
  return (
    <footer className="bg-[#205E85] text-white w-full py-10">
      <div className="container mx-auto px-4 md:px-16 lg:px-28">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 md:gap-4 text-center md:text-left">
          {/*Connect with us */}
          <div className="flex flex-col items-center md:items-start space-y-4">
            <h3 className="text-lg font-medium">Connect with us</h3>
            <div className="flex space-x-3">
              {[1, 2, 3, 4].map((item) => (
                <a
                  key={item}
                  href="#"
                  className="w-8 h-8 bg-[#1a1a1a] hover:bg-gray-800 transition-colors duration-200"
                  aria-label="Social Link"></a>
              ))}
            </div>
            <a
              href="/"
              target="_blank"
              rel="noopener noreferrer"
              className="text-white inline-block border bg-[#1A4763] border-white px-6 py-2 text-sm font-semibold hover:bg-white hover:text-[#205E85] transition-colors duration-300">
              LinkTree
            </a>
          </div>

          {/*Mailing List & Contact */}
          <div className="flex flex-col items-center space-y-6">
            {/* Mailing List Button */}
            <button
              className="border bg-[#1A4763] border-white px-6 py-3 text-sm font-semibold hover:bg-white hover:text-[#205E85] transition-colors duration-300"
              onClick={() => console.log("Open modal")}>
              Join our Mailing List
            </button>
            {/* Contact Info */}
            <div className="flex flex-col items-center space-y-1">
              <span className="text-md font-bold ">Reach us at</span>
              <a
                href="mailto:contact@acmucsd.com"
                className="font-medium hover:underline">
                Lorem ipsum dolor sit amet
              </a>
            </div>
          </div>

          {/* Links (Right Aligned on Desktop) */}
          <div className="flex flex-col md:flex-row md:justify-end items-center md:items-start gap-15 pt-2">
            <a href="/about" className="hover:text-gray-300 text-lg font-bold">
              About us
            </a>
            <a
              href="/sponsors"
              className="hover:text-gray-300 text-lg font-bold">
              Our Sponsors
            </a>
            <a
              href="/partners"
              className="hover:text-gray-300 text-lg font-bold">
              Our Partners
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
};
