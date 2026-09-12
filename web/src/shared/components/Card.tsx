import type { ReactNode } from "react";
import "./Card.css";

interface CardProps {
  title?: string;
  children: ReactNode;
}

/** Minimal shared layout primitive used by both the dashboard and customer surfaces. */
export function Card({ title, children }: CardProps) {
  return (
    <div className="card">
      {title && <h3 className="card-title">{title}</h3>}
      <div className="card-body">{children}</div>
    </div>
  );
}
