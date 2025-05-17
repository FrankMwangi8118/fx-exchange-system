# Load environment variables from .env
if [ -f .env ]; then
  set -o allexport
    source .env
  set +o allexport
else
  echo ".env file not found!"
  exit 1
fi
NAME="$APPLICATION_NAME"
IMAGE="$IMAGE_NAME"
PORT="$SERVER_PORT"
docker build -t "$IMAGE" .
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=$ACTIVE_PROFILE"
docker run -d \
  -p "$PORT:$PORT" \
  -e SERVER_PORT="$SERVER_PORT"\
  -e JAVA_OPTS="$JAVA_OPTS" \
  -e ACTIVE_PROFILE="$ACTIVE_PROFILE" \
  -e POSTGRES_PASSWORD="$POSTGRES_PASSWORD" \
  -e POSTGRES_USERNAME="$POSTGRES_USERNAME" \
  -e POSTGRES_PORT="$POSTGRES_PORT" \
  -e POSTGRES_DB="$POSTGRES_DB" \
  -e POSTGRES_HOST="$POSTGRES_HOST" \
  -e POSTGRES_PROVIDER="$POSTGRES_PROVIDER" \
  -e DRIVER_CLASS_NAME="$DRIVER_CLASS_NAME" \
  -e API_KEY="$API_KEY" \
  -e API_PASSPHRASE="$API_PASSPHRASE" \
  -e RATE_SERVICE_API_KEY="$RATE_SERVICE_API_KEY" \
  -e RATE_SERVICE_API_PASSPHRASE="$RATE_SERVICE_API_PASSPHRASE" \
  -e JAVA_OPTS="$JAVA_OPTS"\
    --name "$NAME" \
        "$IMAGE"
  docker logs -f "$NAME"