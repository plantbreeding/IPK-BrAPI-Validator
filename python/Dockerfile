# Build from the repository root:  docker build -f python/Dockerfile -t brava .
FROM docker.io/library/python:3.14-slim-trixie AS build
COPY --from=ghcr.io/astral-sh/uv:0.12 /uv /uvx /bin/
ENV UV_COMPILE_BYTECODE=1 UV_LINK_MODE=copy UV_PYTHON_DOWNLOADS=never
WORKDIR /app
COPY python/pyproject.toml python/uv.lock python/README.md ./
RUN uv sync --locked --no-dev --no-install-project --extra oracle --extra postgres --extra mysql
COPY python/src ./src
RUN uv sync --locked --no-dev --extra oracle --extra postgres --extra mysql

FROM docker.io/library/python:3.14-slim-trixie
RUN useradd --create-home --uid 1000 brava
WORKDIR /app
COPY --from=build /app /app
COPY src/main/resources/collections /data/resources/collections
COPY src/main/resources/schemas /data/resources/schemas
COPY src/main/webapp/advanced /data/frontend
ENV PATH="/app/.venv/bin:$PATH" \
    BRAVA_RESOURCES_DIR=/data/resources \
    BRAVA_FRONTEND_DIR=/data/frontend \
    BRAVA_DATABASE_URL=sqlite+aiosqlite:////data/db/brava.db
RUN mkdir -p /data/db && chown brava /data/db
USER brava
EXPOSE 8000
CMD ["brava", "serve", "--host", "0.0.0.0", "--port", "8000"]
