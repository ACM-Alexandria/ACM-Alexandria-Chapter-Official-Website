# ACM Alexandria Chapter — Official Website

[![Release Version](https://img.shields.io/badge/version-1.2.0-blue.svg)](package.json)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Vite](https://img.shields.io/badge/Vite-7.x-purple.svg)](frontend/package.json)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](backend/acmwebsite/pom.xml)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-orange.svg)](CONTRIBUTING.md)

Welcome to the official repository for the **Alexandria University ACM Student Chapter** official website. This platform showcases our chapter’s vision, promotes student engagement, highlights technical achievements, and hosts registrations for our events, clubs, and training programs.

---

## 🚀 Key Features

* **Event Management & Registration**: Interactive schedule of chapter events, hackathons, and webinars with seamless attendee registration.
* **Club Chapters & Programs**: Exploration of various specialized training programs (e.g., Software Engineering Club) with dedicated application tracks.
* **Hierarchical Committee Visualizer**: Interactive rendering of high board members and committee structures showing our chapter organization.
* **Admin Dashboard**: A secure control panel for chapter administrators to manage content, events, registrations, and exclusive forms.
* **Google Sheets & Drive Integration**: Automatically exports attendee registrations headlessly to Google Sheets in designated Google Drive folders.
* **Gmail SMTP Notifications**: Automated transactional emails (e.g., registration confirmations, password resets) built using Spring Boot's mail sender.
* **Cloudinary Media Hosting**: Handles image and media uploads securely in the cloud via integration with Cloudinary's media API.
* **ACM Radio & Podcasts**: Integrated media center for sharing and listening to chapter radio shows and podcast episodes.
* **Newsletter Subscription & Socials**: Direct links and newsletters to keep our student community up-to-date.

---

## 🛠️ Tech Stack

### Frontend
* **Core**: React 19, JavaScript (ES6+), Vite 7
* **Styling**: Tailwind CSS v4, Animate on Scroll (AOS)
* **Routing & State**: React Router DOM v7, Context API
* **Icons & Visualization**: React Icons, React Organizational Chart
* **HTTP Client**: Axios

### Backend & Database
* **Framework**: Spring Boot 3.5.7, Spring Security
* **Authentication**: JSON Web Tokens (JWT)
* **Data Access**: Spring Data JPA, Hibernate, MySQL Connector
* **Mapping & Helpers**: MapStruct, Lombok
* **External Integrations**:
  * Google API Client (Sheets & Drive APIs)
  * Cloudinary HTTP client library
  * Spring Boot Mail Sender (Gmail SMTP)

---

## 📁 Repository Structure

```text
├── backend/
│   └── acmwebsite/             # Spring Boot backend source code, configurations, & tests
│       ├── pom.xml             # Maven dependencies configuration (version 1.2.0)
│       └── src/                # Backend application code & database scripts
├── frontend/
│   ├── package.json            # React-Vite project dependencies (version 1.2.0)
│   ├── vite.config.js          # Vite server and build configurations
│   ├── public/                 # Static assets
│   └── src/                    # Frontend source code (pages, components, services)
├── docs/                       # Developer configuration and local setup documentation
│   ├── cloudinary.md           # Cloudinary media account creation and setup guide
│   ├── gmail-smtp.md           # Gmail SMTP App Password creation guide
│   └── google-sheets-setup.md  # Google Cloud Developer Console setup & refresh token guide
├── CONTRIBUTING.md             # Guidelines for contributing, issue assignments, & PRs
└── README.md                   # This project index landing page
```

---

## ⚙️ Getting Started & Local Setup

To run this project locally, you need to set up both the backend and frontend servers, and configure credentials for external services (Gmail, Cloudinary, Google APIs).

### Prerequisites
* **Java Development Kit (JDK)**: Version 21
* **Node.js**: Version 20.x or higher
* **MySQL Server**: Local instance running

### 1. Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend/acmwebsite
   ```
2. Configure environment parameters:
   * Copy `src/main/resources/application.properties.example` into a new file named `application.properties` in the same directory.
   * Fill in your database username, password, and the required API credentials.
3. Follow the detailed configuration setup guides:
   * [Gmail SMTP Setup](docs/gmail-smtp.md)
   * [Cloudinary Credentials Setup](docs/cloudinary.md)
   * [Google Sheets Integration Guide](docs/google-sheets-setup.md)
4. Start the backend server using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
5. Populate the database snapshot:
   Run the following command to import the default database snapshot (replace `{DB_USER_NAME}` with your MySQL username):
   ```bash
   cd src/main/resources
   mysql -u {DB_USER_NAME} -p acm_db < snapshot.sql
   ```
   Then enter your MySQL password when prompted.

### 2. Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Configure environment variables:
   * Copy `.env.example` into `.env`.
   * Update the backend base URL if it differs from default.
4. Launch the local Vite development server:
   ```bash
   npm run dev
   ```

---

## 🤝 Contributing

We welcome contributions from chapter members and the open-source community! 

Please read our [Contributing Guidelines](CONTRIBUTING.md) to understand:
* How to find and request issue assignments (FCFS policy).
* Branch naming conventions and pull request rules.
* Code review and catch-up meeting processes.

---

## 📞 Mentors & Core Team

For help, questions, or guidance, feel free to reach out to our project mentors:
* **Omar Zydan**
* **Seif Shaheen**
