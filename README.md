# Onewish Cafe

A Java 17 compatible, Maven WAR project for Apache Tomcat 10.1, JSP, Jakarta Servlets, JDBC and MySQL 8. It includes a responsive cafe storefront, MySQL menu, session shopping bag, checkout that recalculates prices on the server, order tracking and a protected admin area.

## Current project status

The existing application source and deployment configuration are preserved in this repository. A GitHub repository has been created, but the application has not yet been deployed; there is no production database or verified public HTTPS endpoint yet. Orders currently use pay-at-counter / cash on delivery only; no online payment is taken. Customer registration is not included; the current checkout is guest checkout. The contact form stores messages in MySQL.

## Run locally

Requirements: JDK 17 or later, Maven 3.9+, MySQL 8, and Tomcat 10.1. The app targets Java 17 bytecode and uses Jakarta Servlet 6 / JSP 3.1, so use Tomcat 10.1 rather than Tomcat 9. `SESSION_COOKIE_SECURE=true` enables the secure cookie flag after HTTPS is configured; the app also sets HttpOnly and SameSite=Lax.

1. Create the local database with `mysql -u root -p -e "CREATE DATABASE onewish_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"`. Then import with `mysql -u root -p onewish_db < database/schema.sql` and `mysql -u root -p onewish_db < database/seed.sql`.
2. Run `cp .env.example .env`, edit the local values, then load them with `set -a; source .env; set +a`. Never commit `.env` or real credentials.
3. Build: `mvn clean package`.
4. Copy `target/onewish.war` into Tomcat's `webapps/` directory and start Tomcat. Browse to `http://localhost:8080/onewish/`.

JDBC settings use `DB_URL`, `DB_USER`, and `DB_PASSWORD` when supplied. Otherwise, the application reads Railway's `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, and `MYSQLPASSWORD` variables. A database connection is required for menu and ordering pages.

## Initialize the first administrator

Set `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD` (at least 12 characters), plus the three `DB_*` values in your shell. No admin password is shipped in the example environment file. Run the one-time helper after building:

```sh
mvn -q -Dexec.mainClass=in.onewish.config.AdminBootstrap -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

It stores a BCrypt hash and does not print the password. Re-running with an existing email does not replace that account. Create administrator credentials privately; never put them in Git or chat. Admin URL: `/admin/login`.

## Database and seed data

`database/schema.sql` creates related users, categories, menu items, orders, order items, and contact messages with foreign keys, indexes and constraints in the database selected by the connection. `database/seed.sql` inserts 25 named sample items and is safe to rerun by category/name. Prices use INR and `DECIMAL`; seed records contain no customer data. Run schema first, then seed. Back up with `mysqldump --single-transaction onewish_db > onewish-backup.sql`; restore with `mysql onewish_db < onewish-backup.sql`.

## Features and current limits

- Menu cards and category/search/sorting DAO support read available MySQL rows. Items are added to a session bag; checkout validates input and quantity and locks/rechecks availability and price in a transaction before saving order snapshots.
- Order references can be looked up at `/track?ref=OW-<32 hexadecimal characters>`. Treat the reference as private: guest orders currently have no customer account or ownership login.
- `/admin/*` routes except sign-in require an admin session. Admin can view orders, change their status, add and edit menu items, and deactivate them by unchecking availability.
- CSRF tokens are included on forms, SQL input uses prepared statements, password hashes use BCrypt, and basic response headers are set. Before production, add rate limiting, automated CSRF coverage, a Content Security Policy, and a customer ownership model for private tracking. Set `SESSION_COOKIE_SECURE=true` behind HTTPS; `HttpOnly` and `SameSite=Lax` are applied by the session-cookie listener.
- Food images and typography use external Unsplash and Google Fonts URLs; internet access is needed for those visuals/fonts. Replace or self-host assets if external image availability is unsuitable.

## Build and verification

Use `mvn clean package` to compile, run unit tests, and package the WAR. Unit tests cover precise line totals and quantity/price bounds. Importing SQL, connecting to MySQL, browser workflows and live deployment must be tested in an environment with a MySQL service and Tomcat/hosting account. Do not describe those integration checks as passed until run there.

## Hosting and live launch

The Java WAR and MySQL database need application and database hosting. See [RAILWAY_DEPLOYMENT.md](RAILWAY_DEPLOYMENT.md) for the Railway deployment path. The optional static storefront prepared for Netlify cannot run the Java backend or provide MySQL; see [NETLIFY_DEPLOYMENT.md](NETLIFY_DEPLOYMENT.md) if you choose a separate static frontend later. No production app/database has been deployed yet.

## Updating

Make changes on a Git branch, run `mvn clean package`, review the WAR and database migration impact, then push and redeploy through the host. Back up MySQL before schema changes. Keep `DB_*` secrets in the host's secret settings, not in repository files.
