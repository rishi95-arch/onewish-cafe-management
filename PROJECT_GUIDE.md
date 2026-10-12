# Onewish Project Guide

## What each part does

- **HTML/JSP** describes page structure. JSP lets Java-served pages display database results.
- **CSS** controls responsive layout, color, typography and visual hierarchy.
- **Vanilla JavaScript** handles small browser interactions; core order rules stay on the server.
- **Java Servlets** receive HTTP requests and coordinate page, cart, checkout and admin actions.
- **JDBC** connects Java to MySQL using prepared SQL statements.
- **MySQL** stores categories, menu, orders and order-item price/name snapshots.
- **Maven** resolves libraries and packages the application as a WAR.
- **Tomcat** runs the Servlet/JSP application and accepts browser requests.

## Request flow

The local Tomcat application still supports `Browser → Servlet → DAO/JDBC → MySQL → JSP → Browser`. The Netlify storefront uses `Browser → static HTML/CSS/JavaScript → JSON API servlet → DAO/JDBC → MySQL → JSON → Browser`.

The browser submits item IDs and quantities. The server checks each item again in MySQL, reads its current price, computes the total, then writes the order and item snapshots in one database transaction. Browser-supplied prices are never used.

## Folders

- `src/main/java/in/onewish/config`: database connection and initial admin bootstrap.
- `src/main/java/in/onewish/model`: small domain records.
- `src/main/java/in/onewish/dao`: prepared JDBC queries and menu persistence.
- `src/main/java/in/onewish/web`: servlet endpoints, filters and request helpers.
- `src/main/webapp/WEB-INF/views`: JSP pages not directly exposed as static files.
- `src/main/webapp/assets`: CSS and browser JavaScript.
- `netlify/site`: plain HTML and vanilla JavaScript for the static Netlify customer and admin pages; the build reuses the existing Onewish CSS.
- `netlify/build.mjs`, `netlify.toml`: create the configurable API URL and Netlify publish output without adding a frontend framework.
- `database`: reproducible schema and sample data.

## Build/deploy concepts

Maven packages Java classes, JSPs and assets into `target/onewish.war`. Tomcat deploys the WAR. The included Dockerfile builds that WAR and runs it in Tomcat 10.1. Netlify hosts only the static site; a Java-capable container host serves the `/api/*` routes and provides private network access to MySQL. Production hosts supply DB settings through environment variables; keep them in their secret managers. Schedule database backups and test restoration. See [NETLIFY_DEPLOYMENT.md](NETLIFY_DEPLOYMENT.md).

## Viva quick notes

**Why Servlet/JSP?** Servlets handle request logic in Java; JSP renders server data as HTML. **Why JDBC?** It connects Java to MySQL with parameterized statements. **Why a transaction?** An order header and all its item rows must save together; an error rolls the whole checkout back. **Why recompute prices?** A browser is controlled by the user and cannot be trusted for price or availability. **Why BCrypt?** It stores a slow salted password hash instead of the original password.

Current academic-project limits include guest ordering, no customer registration, and no online payment integration. Admin can add/edit/deactivate menu items and filter orders by status/date. Automated and live integration tests are still pending. A live service and production database have not been provisioned or verified.
