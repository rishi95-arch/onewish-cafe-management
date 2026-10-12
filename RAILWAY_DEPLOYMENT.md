# Onewish: GitHub and Railway launch checklist

No GitHub repository or Railway services are configured yet. These steps deploy the existing Java/JSP WAR from this folder; they do not replace it with a static site.

## 1. Create a private GitHub repository

1. On [github.com/new](https://github.com/new), create a **private** repository named `onewish-cafe`. Do not initialize it with a README, license, or `.gitignore` because this project already has those files.
2. In Terminal, run these commands from the existing project directory. Replace the URL with the repository URL GitHub shows you:

   ```sh
   cd /Users/rishirishi/Documents/Codex/2026-10-12/food-item-menu-for-cafe-project
   git init -b main
   git add .
   git status --short
   git commit -m "Prepare Onewish cafe for Railway deployment"
   git remote add origin https://github.com/YOUR_GITHUB_NAME/onewish-cafe.git
   git push -u origin main
   ```

   If Git asks for an author identity, configure your own `user.name` and `user.email`. Sign in with GitHub's browser flow or credential manager; do not paste a token into chat or a source file. Check that `.env` is not staged before committing.

## 2. Create Railway services

1. Sign in to [Railway](https://railway.com/) and create a project from the GitHub repository. Authorize Railway to read only the repository you created.
2. Add a **MySQL** service from Railway's MySQL database template. Keep it in the same Railway project/environment as the app so private service variables are available.
3. Add a service from the same GitHub repository. Railway should detect the root `Dockerfile`. The Docker build runs Maven tests and builds the WAR; the runtime image runs Tomcat 10.1.
4. In the app service's **Variables** page, add these reference variables, using the exact MySQL service name as the reference namespace (shown here as `MySQL`):

   ```text
   MYSQLHOST=${{MySQL.MYSQLHOST}}
   MYSQLPORT=${{MySQL.MYSQLPORT}}
   MYSQLDATABASE=${{MySQL.MYSQLDATABASE}}
   MYSQLUSER=${{MySQL.MYSQLUSER}}
   MYSQLPASSWORD=${{MySQL.MYSQLPASSWORD}}
   SESSION_COOKIE_SECURE=true
   ```

   The application converts these Railway MySQL variables to a JDBC URL. `DB_URL`, `DB_USER`, and `DB_PASSWORD` are also supported and take precedence if you prefer to provide a JDBC URL yourself. Do not set a guessed database name. The configured `MYSQLDATABASE` is the database the schema scripts will use.

5. Railway provides `PORT` for the web service. The container entrypoint configures Tomcat to listen on that port and `0.0.0.0`; do not hard-code a different `PORT`.
6. In app service **Settings → Deploy**, set **Healthcheck Path** to `/health`. This checks both the HTTP process and a live `SELECT 1` against MySQL; Railway will not route traffic until it returns 200.

Railway supports MySQL service references, private service networking, Dockerfile builds, and SSH commands into a deployed service. Railway currently provides a 30-day trial with a one-time $5 credit, then the Free plan has $1 of monthly resource credit. Trial volumes are deleted 30 days after the trial credit expires. Hobby is $5/month and includes $5 of usage; use above that is billed extra. Two continuously running services (app and MySQL) may exceed Free credits, so check estimated usage and choose a paid plan only if you authorize the cost. Review [current Railway pricing](https://docs.railway.com/pricing/plans) before creating services.

## 3. Initialize schema and sample menu

Wait until the MySQL service is healthy. Install/login to Railway CLI, open Terminal in the project folder, and pipe each SQL file over Railway's SSH connection to the MySQL container. Replace `MySQL` if you renamed that service:

```sh
railway ssh --service MySQL -- sh -lc 'mysql -u"$MYSQLUSER" -p"$MYSQLPASSWORD" "$MYSQLDATABASE"' < database/schema.sql
railway ssh --service MySQL -- sh -lc 'mysql -u"$MYSQLUSER" -p"$MYSQLPASSWORD" "$MYSQLDATABASE"' < database/seed.sql
```

The SQL files create tables and seed 25 items in the database selected by the MySQL client; they do not assume its name is `onewish_db`. The seed is safe to rerun and will not reset menu availability. Keep the MySQL service private; there is no need to open a public TCP proxy for initialization. Railway SSH requires an account login and an SSH key registered with Railway.

## 4. Create the initial administrator

After the app deployment is healthy and Tomcat has unpacked `ROOT.war`:

1. In the app service's **Variables**, temporarily set `APP_ADMIN_EMAIL` and `APP_ADMIN_PASSWORD`. Use an email you control and a unique password of at least 12 characters. Do not commit these values.
2. Wait for the redeployment to become healthy, then run:

   ```sh
   railway ssh --service Onewish -- java -cp '/usr/local/tomcat/webapps/ROOT/WEB-INF/classes:/usr/local/tomcat/webapps/ROOT/WEB-INF/lib/*' in.onewish.config.AdminBootstrap
   ```

   Replace `Onewish` with the exact app service name. The helper stores a BCrypt hash, prints only whether it created or found the account, and does not change an existing account with that email.
3. Remove `APP_ADMIN_EMAIL` and `APP_ADMIN_PASSWORD` from Railway Variables and wait for the redeployment. The created admin account remains in MySQL. Sign in at `https://YOUR-RAILWAY-DOMAIN/admin/login`.

## 5. Add the public HTTPS domain and verify

1. In the app service's **Settings → Networking → Public Networking**, generate a Railway domain. Railway provisions HTTPS for its public domain.
2. Confirm the `https://...` page opens. Check the health endpoint:

   ```sh
   curl -i https://YOUR-RAILWAY-DOMAIN/health
   ```

   It should return `HTTP 200` and `ok`. Then check `/`, `/menu`, `/cart`, `/track`, `/contact`, and `/admin/login`.
3. Sign in to the admin page; add a menu item and verify it appears on `/menu`. Place a test guest order using pay-at-counter, reload its `/track?ref=...` URL, and update its status in the admin dashboard.
4. Review Railway build/deploy logs and service logs. Confirm `SESSION_COOKIE_SECURE=true`, that DB settings are reference variables (not copied secret values), and that an order remains after a redeploy. Remove test orders/customer data if required by your project policy.
5. Keep database backups enabled and test a restore. Do not treat a healthy deployment or the unit tests as proof that every live workflow works; complete the checks above against the public URL.

## Remaining verification limits

The checked-in Dockerfile and both base-image tags were reviewed, but this computer has no Docker or Podman engine, so an actual local image build/start test could not be run here. Maven compiles and tests the Java code, but it does not compile JSPs. JSP compilation, Railway's injected-port behavior, database service references, SSH initialization, and the public workflows must be verified by the first Railway deployment and checklist above.
