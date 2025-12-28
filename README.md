# Nutrilog (Web Platform)

Nutrilog is a comprehensive web-based platform designed for nutrition specialists to manage patients, create dietary
plans, and monitor progress within the **Nutrilog** ecosystem.

It consists of two main components: a public-facing **Landing Page** that showcases the service, and a secure **Admin
Panel** restricted to authorized nutritionists for day-to-day operations.

## Key Features

* **🥗 Advanced Diet Creator:** Build personalized nutrition plans using a custom database of recipes or create meals
  from scratch.
* **💾 Template System:** Save effective diet plans as templates to speed up workflow for future patients.
* **🛒 Smart Shopping Lists:** Algorithms that automatically categorize ingredients from diet plans into logical groups (
  e.g., Dairy, Produce) for the patient's mobile app.
* **🔐 Secure Admin Panel:** Protected access for professionals to manage their workspace, accessible only after
  authentication.
* **👥 Patient & Diet Management:** Full control over patient profiles, assigned diets, and historical data.
* **📊 Analytics Dashboard:** Visual statistics regarding application usage and patient engagement (powered by Recharts).
* **🌐 Public Landing Page:** An informative marketing page explaining the ecosystem's benefits to potential clients.

## Architecture & Tech Stack

The project is built as a modern monolith with a clear separation between the REST API backend and the SPA frontend.

### Backend (API)

* **Language:** Java 21
* **Framework:** Spring Boot 3.2.3
* **Security:** Spring Security + Firebase Admin SDK
* **Database:** PostgreSQL + Flyway (Migrations)
* **Caching:** Caffeine Cache

### Frontend (Admin Panel & Landing)

* **Framework:** [React](https://react.dev/) + TypeScript
* **Build Tool:** Vite
* **Styling:** Tailwind CSS + Headless UI / Radix UI
* **State Management:** Zustand + React Query (@tanstack/react-query)
* **Form Handling:** React Hook Form
* **Visualization:** Recharts (Charts & Stats)
* **Notifications:** Sonner

## Prerequisites & Setup

### Requirements

* Java 21 JDK
* Node.js 18 or newer
* PostgreSQL 16+
* Firebase Project (Service Account JSON)

### Configuration

1. **Backend Configuration:**
   Ensure you have a `key.properties` file or valid environment variables set up for database
   connections (`POSTGRES_HOST`, `POSTGRES_USER`, etc.) and email services.

2. **Frontend Configuration:**
   Create a `.env` file in the `frontend` directory with your API endpoints and Firebase keys.

### Running the Application

**Backend (Spring Boot):**

```bash
./gradlew bootRun
```

## Deployment

While local development relies on standard build tools (Gradle wrapper & npm), the production environment is fully
**containerized using Docker**.

The application is deployed on a **VPS (Virtual Private Server)**, where the backend, frontend, and database services
operate within isolated containers to ensure reliability and environment consistency.

## CI/CD & DevOps

The project utilizes **GitHub Actions** to enforce code quality standards and automate the delivery pipeline.

* **☕ Backend Validation (CI):**
    * Triggers on every Pull Request or Push to the `main` branch.
    * Sets up a virtual machine with **Java 21**.
    * Executes unit tests (`./gradlew test`) to prevent regression.
    * Verifies build integrity (`./gradlew build`).
* **⚛️ Frontend Validation (CI):**
    * Sets up a **Node.js** environment.
    * Installs dependencies (`npm ci`) and runs static code analysis (`npm run lint`) to catch syntax errors.
    * Simulates a production build (`npm run build`) to ensure the application is deployable.
* **🐳 Continuous Delivery (CD):**
    * Automatically builds a Docker image for the backend upon successful merge to `main`.
    * Pushes the image to **Docker Hub** registry (utilizing GitHub Secrets for security).

## License

This project is proprietary software. All rights reserved.
