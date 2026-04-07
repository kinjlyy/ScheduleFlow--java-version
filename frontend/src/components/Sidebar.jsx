// src/components/Sidebar.jsx
import React from 'react';
import styles from './Sidebar.module.css';
import { Button } from './UI.jsx';

const NAV_ITEMS = [
  { id: 'constraints', label: 'Constraints',    icon: '🔒', step: 1 },
  { id: 'setup',       label: 'Setup Sections', icon: '⚙️', step: 2 },
  { id: 'review',      label: 'Review All',     icon: '📋', step: 3 },
  { id: 'result',      label: 'Timetable',      icon: '📅', step: 4 },
];

export default function Sidebar({
  activePage, onNavigate, sections,
  onAddSection, hasValidationErrors, onDashboard,
}) {
  const sectionNames = sections.map(s => ({ id: s.id, label: s.name || `Section ${s.id}` }));

  return (
    <aside className={styles.sidebar}>
      {/* Back to dashboard */}
      {onDashboard && (
        <button className={styles.dash_back} onClick={onDashboard}>
          ← Dashboard
        </button>
      )}

      {/* Actions */}
      <div className={styles.group}>
        <span className={styles.group_label}>Actions</span>
        <Button variant="primary" full onClick={onAddSection}>+ Add Section</Button>
      </div>

      {/* Steps */}
      <div className={styles.group}>
        <span className={styles.group_label}>Steps</span>
        {NAV_ITEMS.map(item => {
          const isActive = activePage === item.id;
          const showError = item.id === 'setup' && hasValidationErrors;
          return (
            <button
              key={item.id}
              className={`${styles.nav_item} ${isActive ? styles.active : ''}`}
              onClick={() => onNavigate(item.id)}
            >
              <span className={styles.step_num}>{item.step}</span>
              <span className={styles.nav_icon}>{item.icon}</span>
              <span className={styles.nav_label}>{item.label}</span>
              {showError && <span className={styles.error_dot} title="Violations" />}
            </button>
          );
        })}
      </div>

      {sectionNames.length > 0 && (
        <div className={styles.group}>
          <span className={styles.group_label}>Sections</span>
          {sectionNames.map(s => (
            <button key={s.id} className={styles.sec_item} onClick={() => onNavigate('setup')}>
              <span className={styles.sec_dot} />
              {s.label}
            </button>
          ))}
        </div>
      )}
    </aside>
  );
}
