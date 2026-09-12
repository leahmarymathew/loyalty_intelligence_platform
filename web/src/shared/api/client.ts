/**
 * API client stub.
 *
 * core-api has no REST layer yet (root README: "Tier engine, REST layer, and
 * campaign worker not yet built"), so there is nothing live to call. This
 * module exists so page/component code has a stable import to code against
 * now, and a single place to wire up `fetch`/base URL/auth headers once the
 * REST layer exists, instead of scattering `fetch` calls through components.
 *
 * Every tenant-scoped core-api endpoint will require the caller to be
 * resolved to a tenant (see docs/ARCHITECTURE.md "Known limitations" — the
 * web-layer piece that reads an authenticated request into `TenantContext`
 * lands with the REST layer). `tenantSlug` is threaded through here as a
 * placeholder for whatever that ends up being (subdomain, header, JWT claim).
 */

export interface ApiClientConfig {
  baseUrl: string;
  tenantSlug?: string;
}

const defaultConfig: ApiClientConfig = {
  baseUrl: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
};

export class NotImplementedError extends Error {
  constructor(path: string) {
    super(
      `core-api REST layer is not built yet (requested ${path}). ` +
        `This call is a stub — see src/shared/api/client.ts.`,
    );
    this.name = "NotImplementedError";
  }
}

/**
 * Placeholder API client. Every method currently rejects with
 * `NotImplementedError`; pages should fall back to `src/shared/api/stubData.ts`
 * until core-api's REST layer exists.
 */
export class ApiClient {
  private readonly config: ApiClientConfig;

  constructor(config: ApiClientConfig = defaultConfig) {
    this.config = config;
  }

  async get<T>(path: string): Promise<T> {
    throw new NotImplementedError(`${this.config.baseUrl}${path}`);
  }

  async post<T>(path: string, _body: unknown): Promise<T> {
    throw new NotImplementedError(`${this.config.baseUrl}${path}`);
  }
}

export const apiClient = new ApiClient();
