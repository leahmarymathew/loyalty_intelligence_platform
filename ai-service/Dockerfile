FROM python:3.12-slim AS build
WORKDIR /build
COPY pyproject.toml .
COPY src ./src
RUN pip install --no-cache-dir --prefix=/install .

FROM python:3.12-slim
WORKDIR /app
COPY --from=build /install /usr/local
COPY src ./src
EXPOSE 8000
ENTRYPOINT ["uvicorn", "ai_service.main:app", "--app-dir", "src", "--host", "0.0.0.0", "--port", "8000"]
