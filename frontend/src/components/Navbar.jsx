// src/components/Navbar.jsx
import React from 'react';
import styles from './Navbar.module.css';

export default function Navbar({ userName, onDashboard, onLogout }) {
  return (
    <nav className={styles.nav}>
      <button className={styles.logo} onClick={onDashboard}>
        <div className={styles.logo_wrap}>
          <div className={styles.logo_icon}>📅</div>
          <h1 className={styles.logo_text}>ScheduleFlow</h1>
        </div>
      </button>
      <div className={styles.user}>
        <div className={styles.avatar}>{userName?.[0]?.toUpperCase() || 'U'}</div>
        <span className={styles.welcome}>Welcome ! {userName || 'User'}</span>
        {onLogout && (
          <button className={styles.logout_btn} onClick={onLogout} title="Logout">↪</button>
        )}
      </div>
    </nav>
  );
}
