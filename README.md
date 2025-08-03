# Online Auction System

Re-creation of an online auction platform built with **Java**, **Tomcat**, and **SQL**, plus an advanced layer integrating **JavaScript** and **cookies** for dynamic UX and session features. Course project for **Politecnico di Milano – Web Technologies**.

---

🔍 Introduction
This project enables:

Server-side rendering with Java/HTML on Tomcat
User, item, auction, and bid management persisted in SQL
Authentication and session handling
Progressive enhancement with JavaScript for dynamic updates
Cookie usage for “remember me” and UI preferences
Everything runs locally on Tomcat, with no cloud dependencies.

---

🛠️ Features

✔ **Base (Java + HTML + SQL on Tomcat)** – Servlets/JSP (or server-rendered HTML) with JDBC CRUD for users, items, auctions, and bids
✔ **Auth & Sessions** – Login/registration, server-side session tracking, optional roles (user/admin)
✔ **Auctions & Bids** – Create auctions, view details, maintain bid history, validate minimum bid > current price
✔ **Relational DB** – Core tables with constraints and indexes for integrity and performance
✔ **Server-Rendered UI** – Clean HTML pages with pagination and basic filters
✔ **Advanced (JavaScript + Cookies)** – Client-side validation, partial UI updates (fetch/polling for latest bids), cookies for remember-me and non-sensitive preferences

---

⚙️ How It Works?

1️⃣ **Authentication & Session**
Users sign up or log in; the server creates a session tied to the user. Cookies may store a “remember-me” token and UI preferences (e.g., list vs grid).

2️⃣ **Auction Management**
Authorized users create auctions (title, description, base price, closing date). Server-rendered pages list auctions with filters and details.

3️⃣ **Bidding**
When a bid is placed, the servlet validates input (amount > current, auction still open), updates the DB, and appends to the bid log. Concurrency is handled transactionally.

4️⃣ **Dynamic Frontend (Advanced Part)**
JavaScript improves UX with real-time validation and lightweight updates of the current price/time left via `fetch`/polling—no full page reload required.

5️⃣ **Cookies & Preferences**
Secure cookies (`HttpOnly`, `SameSite`) are used for remember-me and preference storage; sensitive data remains server-side in the session. A consent banner can be included if needed.

---

🔧 Tech Stack

* **Backend:** Java (Servlets/JSP or controllers on Tomcat)
* **Server:** Apache Tomcat 9+
* **Database:** SQL (e.g., MySQL/PostgreSQL) via JDBC
* **Frontend (base):** HTML/CSS, server-rendered
* **Frontend (advanced):** JavaScript (fetch/polling, client-side validation)
* **Sessions & Cookies:** Server-side session, cookies for remember-me/preferences

---

🚦 Typical Workflow

* Register/Login → session created
* Browse auctions → filter/search
* View auction → place incremental bids with validation
* Advanced JS → live refresh of current price / remaining time
* Auction close → winner determined by highest valid bid & timestamp

---

🔒 Benefits

✅ **Complete & Educational** – Covers a classic web stack: Java, Tomcat, SQL, JS
✅ **Clear Separation of Concerns** – Solid server-rendered core + optional JS enhancements
✅ **Baseline Security** – Input validation, session handling, secure cookie usage
✅ **Local-First** – Easy to run and test without external services

---

NOTE:

* Academic/demonstration project: **no real payment gateway** included.
* SQL DDL/DML scripts can target different RDBMS (MySQL/PostgreSQL).
* The advanced JS layer is optional but recommended for UX (live bids/time remaining, client-side checks).

---

📧 Contact
Questions or contributions? Open an **Issue** or reach out on GitHub! 🚀
