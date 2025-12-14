import React from "react";
import "./Hero.css";

const Hero = () => {
  const scrollToAbout = () => {
    document.querySelector("#about").scrollIntoView({
      behavior: "smooth"
    });
  };

  return (
    <section className="hero">
      <div className="hero-content">
        <h1>Alexandria ACM Student Chapter</h1>
        <p>
          Empowering students through technology, collaboration, & community-driven learning.
        </p>

        <button className="learn-btn" onClick={scrollToAbout}>
          Learn More
        </button>
      </div>
    </section>
  );
};

export default Hero;
