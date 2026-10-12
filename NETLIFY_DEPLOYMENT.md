# Onewish Cafe: Netlify frontend + Java API + MySQL

The app remains the existing Java 17 / Tomcat 10.1 / JSP / JDBC / MySQL project. Netlify serves the customer site as static HTML, CSS and vanilla JavaScript; it does **not** run the WAR or host this MySQL database. The existing JSP URLs remain in the WAR for local use. The Netlify site talks to the new `/api/*` JSON routes.

No external account, GitHub repository, host, database, domain, or public URL has been created. The `netlify/public/` directory is generated during the Netlify build and is not source code.

## Hosting choice and costs checked 12 October 2026

- **Frontend: Netlify Free** is suitable to begin with: $0/month, static CDN hosting, custom domains with SSL, and 300 monthly usage credits. New sites on current credit-based accounts are private until published; when the Free credit limit is reached, sites pause until the next cycle. Keep auto-recharge off. See [Netlify pricing](https://www.netlify.com/pricing/) and [Netlify build settings](https://docs.netlify.com/build/configure-builds/overview/).
- **Java/Tomcat API: Render Docker Web Service.** Render supports Docker for Java/JVM services, health checks, public HTTPS, and private networking. Its free web service has 512 MB RAM, sleeps after 15 minutes idle, and does not support persistent disks/SSH; that is useful for a temporary smoke test but not a reliable always-on ordering API. Paid web compute starts at about $7/month for the 0.5 CPU / 512 MB plan; this app may need a larger plan if Tomcat or the JVM runs short of memory. Check the live [Render pricing](https://render.com/pricing/) before creating it. See [Docker services](https://render.com/docs/docker), [web services](https://render.com/docs/web-services), and [free service limits](https://render.com/docs/free).
- **MySQL: Render's MySQL 8 Docker template**, on a Render private service in the same region as the API, with a persistent disk mounted at `/var/lib/mysql`. Render explicitly documents this custom MySQL setup and private hostname connection. The MySQL container needs a paid compute plan for its persistent disk; disks cost $0.25/GB/month. A 10 GB disk therefore adds about $2.50/month, plus the database service's compute (starting at about $7/month for the smallest paid service). A 512 MB database plan may be tight; review the actual checkout estimate and increase RAM if needed. This is self-managed MySQL: keep the service private and make/test `mysqldump` backups. Render daily disk snapshots are not a replacement for MySQL logical backups. See [Render MySQL deployment](https://render.com/docs/deploy-mysql), [persistent disks](https://render.com/docs/disks), and [pricing](https://render.com/pricing/).

The minimum always-on Render app plus MySQL setup is roughly $16.50/month using the smallest paid plans and a 10 GB disk, before taxes/other metered services; choose plans only after reviewing the current dashboard estimate. The API and MySQL can instead be hosted by a provider of your choice that supports Docker/Tomcat, Java 17, HTTPS, private MySQL connectivity, persistent database storage, and shell/SSH or another secure SQL import path. Netlify stays the frontend host.

## 1. Create and push the GitHub repository

1. Create a **private** repository at [github.com/new](https://github.com/new), e.g. `onewish-cafe`. Do not add a starter README, license, or `.gitignore`.
2. In Terminal, from this exact folder, initialize and push the existing project (this folder is currently not a Git repository):

   ```sh
   cd /Users/rishirishi/Documents/Codex/2026-10-12/food-item-menu-for-cafe-project
   git init -b main
   git add .
   git status --short
   git commit -m "Prepare Onewish for Netlify and Java API deployment"
   git remote add origin https://github.com/YOUR_GITHUB_NAME/onewish-cafe.git
   git push -u origin main
   ```

   Review the staged file list before committing. `.env`, generated `netlify/public/`, `target/`, and real credentials must not be committed. Authenticate through GitHub's browser/credential-manager flow; do not paste a token into chat or a source file.

## 2. Deploy the backend and MySQL on Render

1. Create a Render account and connect the private GitHub repository. These actions happen in your account; no account is connected from this workspace.
2. Create the MySQL service using Render's documented [MySQL template](https://render.com/docs/deploy-mysql). It creates a private service, not a managed database. Choose MySQL 8, set `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, and `MYSQL_ROOT_PASSWORD` in Render's private service settings, and attach a disk at `/var/lib/mysql`. Choose the paid compute plan and disk size only after reviewing the displayed charges. Keep the root password private.
3. Create a **Web Service** from the same Onewish repository. Set Runtime to **Docker** and use the repository-root `Dockerfile`. Pick the same region as MySQL. Enable paid shell/SSH access (free web services cannot be accessed with a shell). Add the health check path `/health`.
4. In the API service variables, configure:

   ```text
   DB_URL=jdbc:mysql://MYSQL_INTERNAL_HOST:3306/MYSQL_DATABASE?serverTimezone=UTC&connectTimeout=5000&socketTimeout=5000
   DB_USER=MYSQL_USER
   DB_PASSWORD=MYSQL_PASSWORD
   SESSION_COOKIE_SECURE=true
   SESSION_COOKIE_SAME_SITE=Lax
   CORS_ALLOWED_ORIGIN=https://YOUR-SITE-NAME.netlify.app
   ```

   Replace placeholders with the values shown in your own Render/Netlify dashboards; use the MySQL **internal** hostname shown by Render (same region). Enter `DB_PASSWORD` in the host secret field, never in source. `PORT` is supplied by Render and the Docker entrypoint binds Tomcat to it. Render's MySQL template uses `MYSQL_DATABASE`, `MYSQL_USER`, and `MYSQL_PASSWORD`; the Java app reads `DB_URL`, `DB_USER`, and `DB_PASSWORD`.

5. Wait for `/health` to return 200. The endpoint executes `SELECT 1`, so it confirms both Tomcat and its database connection.

## 3. Initialize schema and sample data

Because the Render MySQL service is private, do not expose port 3306 publicly. Instead, use SSH port forwarding through the paid API service:

1. Add your Render SSH public key in Render account settings and install/login to Render CLI or use the SSH command from the API service's **Connect** menu. See [Render SSH and Shell](https://render.com/docs/ssh).
2. Copy the database service's internal host (for example `mysql-...:3306`) and the API service's SSH endpoint/region from the dashboard. Open a local tunnel (keep this terminal open):

   ```sh
   ssh -L 3307:MYSQL_INTERNAL_HOST:3306 YOUR_API_SERVICE@ssh.YOUR_REGION.render.com -N
   ```

3. In a second terminal, use a local MySQL command-line client. It prompts for the database password, so it will not be stored in shell history:

   ```sh
   cd /Users/rishirishi/Documents/Codex/2026-10-12/food-item-menu-for-cafe-project
   mysql --protocol=TCP -h 127.0.0.1 -P 3307 -u MYSQL_USER -p MYSQL_DATABASE < database/schema.sql
   mysql --protocol=TCP -h 127.0.0.1 -P 3307 -u MYSQL_USER -p MYSQL_DATABASE < database/seed.sql
   ```

   Replace the placeholders with values from the private Render service. Schema comes first; seed inserts the 25 sample menu items and is safe to rerun. If the local `mysql` command is missing, install a MySQL client locally or use another secure SQL client over this SSH tunnel. Do not make the database public just to import SQL.

## 4. Create the first admin account

1. Temporarily set `APP_ADMIN_EMAIL` and a unique `APP_ADMIN_PASSWORD` of at least 12 characters in the Render API service variables. Do not put either in GitHub, Netlify, or chat.
2. In the API service's Render Shell/SSH, run:

   ```sh
   java -cp '/usr/local/tomcat/webapps/ROOT/WEB-INF/classes:/usr/local/tomcat/webapps/ROOT/WEB-INF/lib/*' in.onewish.config.AdminBootstrap
   ```

   It inserts a BCrypt password hash; it will not print the password or replace an existing account with that email. If the WAR has not unpacked yet, wait for the deployment to finish.
3. Remove both temporary `APP_ADMIN_*` variables from Render and let the service redeploy. The account remains in MySQL. The login page is `https://YOUR-API.onrender.com/admin/login` for the JSP version; the Netlify team login is `https://YOUR-SITE.netlify.app/admin/login`.

## 5. Deploy the static storefront on Netlify

1. In Netlify, **Add new project → Import an existing project**, connect GitHub, and select `onewish-cafe`.
2. The checked-in [netlify.toml](netlify.toml) sets build command `node netlify/build.mjs` and publish directory `netlify/public`. No React/Vite/Angular framework or dependency install is needed. The build copies the existing Onewish CSS and writes a public `assets/config.js` plus a Netlify `/api/*` proxy rule.
3. In Netlify site environment variables, add:

   ```text
   BACKEND_URL=https://YOUR-API.onrender.com
   API_BASE_URL=/api
   ```

   `BACKEND_URL` must be HTTPS. It is a public API address, not a secret. The frontend uses same-origin `/api/...` calls through Netlify's HTTPS proxy; that keeps the session cookie first-party in browsers. It also avoids exposing MySQL credentials. Keep `SESSION_COOKIE_SAME_SITE=Lax` in this proxy mode. If you prefer direct browser-to-API calls, set `API_BASE_URL=https://YOUR-API.onrender.com` instead and set `SESSION_COOKIE_SAME_SITE=None`; test cross-site cookie behavior on your target browsers. The backend `CORS_ALLOWED_ORIGIN` must exactly equal the deployed site's origin (scheme + host, no path or trailing slash). After first Netlify deploy, copy the actual `https://...netlify.app` URL into Render's `CORS_ALLOWED_ORIGIN`, save it, and wait for the API redeploy.
4. Deploy the site, then publish it from Netlify if the new project starts private. Do not enable a paid plan or auto-recharge unless you choose to.

Netlify's proxy rewrite is generated before the single-page route rewrite and forwards `/api/...` to the HTTPS backend. Netlify documents that a 200 rewrite proxies the request without changing the browser URL; see [Netlify rewrites and proxies](https://docs.netlify.com/manage/routing/redirects/rewrites-proxies/). The exact production CORS origin is still set on the backend for direct API access and explicit origin checks.

## 6. Environment variables summary

| Service | Variable | Value |
| --- | --- | --- |
| Netlify build | `BACKEND_URL` | Public Java API HTTPS origin, e.g. `https://onewish-api.onrender.com` |
| Netlify build | `API_BASE_URL` | `/api` for same-origin Netlify proxy; or HTTPS API origin for direct calls |
| Render API | `DB_URL` | JDBC URL using the MySQL Render private hostname and database name |
| Render API | `DB_USER`, `DB_PASSWORD` | MySQL app username/password; password is a host secret |
| Render API | `CORS_ALLOWED_ORIGIN` | Exact Netlify production origin, e.g. `https://onewish-cafe.netlify.app` |
| Render API | `SESSION_COOKIE_SECURE` | `true` |
| Render API | `SESSION_COOKIE_SAME_SITE` | `Lax` for the recommended same-origin proxy; use `None` only for direct cross-origin API requests |
| Render API | `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD` | Temporary only, remove after one-time bootstrap |
| Render API | `PORT` | Supplied by Render; do not set a guessed value |
| Render MySQL | `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD` | Set on the private database service; never commit |

The API supports menu/filter/category reads, cart session get/add/change/remove, checkout with server-side price/availability rechecks, order tracking by private reference, contact submissions, admin login/logout, order status updates, and admin menu create/edit/availability. `GET /api/session` obtains a session and CSRF token; state-changing API calls must send `X-CSRF-Token`. Admin API reads/writes require the admin session. Browser requests include credentials. CORS does not use `*` and allows only the configured exact origin.

## 7. Public URL and workflow verification

Once both hosts are deployed, verify these in order:

1. `https://YOUR-API.onrender.com/health` returns HTTP 200 and `ok`.
2. `https://YOUR-SITE.netlify.app/` and `/menu` load with menu entries; search, category, and price sort work.
3. Add an item, change/remove quantity in `/cart`, refresh the page, then place a pickup test order and a delivery test order. Confirm each returns a reference.
4. Open `/track?ref=OW-...` and verify it reports the order state, amount, and fulfilment method.
5. Sign in at `/admin/login`, update that test order's status, add/edit/deactivate a menu item, and confirm the public menu reflects changes. Sign out and verify admin access requires login.
6. Submit a contact note and verify it appears in the `contact_messages` table. Check browser devtools for mixed-content, CORS, cookie, or failed API requests.
7. Restart/redeploy the API and database services, then confirm the menu and prior test order remain. Configure scheduled `mysqldump` backups and test a restore before using real customer data.

This repository has no Netlify, Render, GitHub, production MySQL, or public HTTPS credentials. Therefore the production URLs, session persistence, SQL initialization, admin login, checkout and public workflows cannot yet be verified here. The app is **not live** until those account-side steps and checks have succeeded.

## Remaining production considerations

- Order tracking uses a high-entropy order reference as a bearer secret and has no customer ownership/login system. Keep references private; for real sensitive operations, add ownership/authentication and rate limiting.
- The API has CSRF tokens, secure/HttpOnly session cookies, and exact-origin CORS. Production needs workflow-level review in deployed browsers, especially third-party-cookie behavior if direct cross-origin API mode is used.
- Existing JSP errors are mapped to generic error pages; API errors return generic JSON without SQL details. Inspect host logs for diagnostics and do not expose them publicly.
- External Unsplash images and Google Fonts still require those vendors to be reachable. The current CSS/brand and core workflows are retained.
