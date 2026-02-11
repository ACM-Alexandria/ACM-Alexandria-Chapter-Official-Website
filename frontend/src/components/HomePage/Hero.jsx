const Hero = () => {
  const scrollToAbout = () => {
    document.querySelector("#about").scrollIntoView({
      behavior: "smooth",
    });
  };

  return (
    <section className="h-screen w-full bg-[#eef3f8] flex justify-center items-center pt-[70px]">
      <div className="text-center w-[60%]">
        <h1 className="text-[42px] text-[#205E85] mb-5">
          Alexandria ACM Student Chapter
        </h1>
        <p className="text-lg text-gray-800 mb-8">
          Empowering students through technology, collaboration, &
          community-driven learning.
        </p>

        <button
          className="py-3 px-8 bg-[#205E85] text-white border-none rounded-md text-base cursor-pointer hover:bg-[#1A4763]"
          onClick={scrollToAbout}
        >
          Learn More
        </button>
      </div>
    </section>
  );
};

export default Hero;
