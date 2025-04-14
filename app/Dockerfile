FROM openjdk:21-slim

# Install GUI + file watching dependencies
RUN apt-get update && apt-get install -y \
    x11-apps curl inotify-tools \
    libxrender1 libxtst6 libxi6 libxext6 libx11-6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Download PostgreSQL JDBC driver
RUN curl -o postgresql.jar https://jdbc.postgresql.org/download/postgresql-42.7.1.jar

# Copy files and make watcher script executable
COPY watch.sh .
RUN chmod +x watch.sh

CMD ["./watch.sh"]
