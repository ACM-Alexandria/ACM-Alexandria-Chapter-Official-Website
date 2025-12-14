import React from "react";
import logo from "../../assets/acm-logo.png";
import "./Navbar.css";

const Navbar = () => {
  return (
    <nav className="navbar">
      <div className="nav-left">
        <img src={logo} className="logo" alt="ACM Logo" />

        <ul className="nav-links">
          <li><a href="#about">About Us</a></li>
          <li><a href="#clubs">Clubs</a></li>
          <li><a href="#events">Events</a></li>
          <li><a href="#programs">Programs</a></li>
          <li><a href="#services">Services</a></li>
        </ul>
      </div>

      <a className="login-btn" href="/login">Sign In</a>
    </nav>
  );
};

export default Navbar;
