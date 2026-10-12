#!/bin/sh
set -eu

PORT="${PORT:-8080}"
case "$PORT" in
  ''|*[!0-9]*) echo "PORT must be a number" >&2; exit 1 ;;
esac
if [ "$PORT" -lt 1 ] || [ "$PORT" -gt 65535 ]; then
  echo "PORT must be between 1 and 65535" >&2
  exit 1
fi

sed -i "s/<Connector port=\"8080\" protocol=/<Connector address=\"0.0.0.0\" port=\"${PORT}\" protocol=/" /usr/local/tomcat/conf/server.xml
if ! grep -q "<Connector address=\"0.0.0.0\" port=\"${PORT}\" protocol=" /usr/local/tomcat/conf/server.xml; then
  echo "Could not configure Tomcat to listen on PORT=${PORT}" >&2
  exit 1
fi
exec /usr/local/tomcat/bin/catalina.sh run
