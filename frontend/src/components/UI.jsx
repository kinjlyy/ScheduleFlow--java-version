// src/components/UI.jsx
// Shared, reusable UI primitives

import React from 'react';
import styles from './UI.module.css';

// ── Button ────────────────────────────────────────────────────────────────
export function Button({ children, variant = 'primary', size = 'md', full = false, onClick, disabled, type = 'button', className = '' }) {
  const cls = [
    styles.btn,
    styles[`btn_${variant}`],
    styles[`btn_${size}`],
    full ? styles.btn_full : '',
    className,
  ].filter(Boolean).join(' ');
  return (
    <button className={cls} onClick={onClick} disabled={disabled} type={type}>
      {children}
    </button>
  );
}

// ── Input ─────────────────────────────────────────────────────────────────
export function Input({ value, onChange, placeholder, type = 'text', min, max, style, onKeyDown }) {
  return (
    <input
      className={styles.input}
      type={type} value={value} onChange={onChange}
      placeholder={placeholder} min={min} max={max}
      style={style} onKeyDown={onKeyDown}
    />
  );
}

// ── Select ────────────────────────────────────────────────────────────────
export function Select({ value, onChange, options, placeholder }) {
  return (
    <select className={styles.input} value={value} onChange={onChange}>
      {placeholder && <option value="">{placeholder}</option>}
      {options.map(o => (
        <option key={o.value} value={o.value}>{o.label}</option>
      ))}
    </select>
  );
}

// ── Tag ───────────────────────────────────────────────────────────────────
export function Tag({ label, onRemove }) {
  return (
    <span className={styles.tag}>
      {label}
      {onRemove && (
        <button className={styles.tag_remove} onClick={onRemove} type="button" title="Remove">✕</button>
      )}
    </span>
  );
}

// ── Card ──────────────────────────────────────────────────────────────────
export function Card({ children, style, className = '' }) {
  return <div className={`${styles.card} ${className}`} style={style}>{children}</div>;
}

// ── Alert ─────────────────────────────────────────────────────────────────
export function Alert({ type = 'info', children }) {
  return <div className={`${styles.alert} ${styles[`alert_${type}`]}`}>{children}</div>;
}

// ── Divider ───────────────────────────────────────────────────────────────
export function Divider() {
  return <hr className={styles.divider} />;
}

// ── FieldLabel ────────────────────────────────────────────────────────────
export function FieldLabel({ children }) {
  return <span className={styles.field_label}>{children}</span>;
}

// ── StatCard ──────────────────────────────────────────────────────────────
export function StatCard({ value, label, color }) {
  return (
    <div className={styles.stat_card}>
      <div className={styles.stat_val} style={color ? { color } : {}}>{value}</div>
      <div className={styles.stat_label}>{label}</div>
    </div>
  );
}

// ── Spinner ───────────────────────────────────────────────────────────────
export function Spinner() {
  return <div className={styles.spinner} />;
}

// ── SectionTitle ──────────────────────────────────────────────────────────
export function SectionTitle({ children }) {
  return <h2 className={styles.section_title}>{children}</h2>;
}
