/** Shared display-formatting helpers used by both surfaces. */

export function formatPoints(points: number): string {
  return new Intl.NumberFormat("en-US").format(points);
}

export function formatDate(iso: string): string {
  return new Intl.DateTimeFormat("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
  }).format(new Date(iso));
}
