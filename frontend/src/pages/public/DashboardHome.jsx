// src/pages/public/DashboardHome.jsx
import React from 'react';
import styles from './DashboardHome.module.css';

export default function DashboardHome({ userName, onNewTimetable, onLogout }) {
  return (
    <div className={styles.root}>
      {/* ── Sidebar ── */}
      <aside className={styles.sidebar}>
        <div className={styles.sidebar_logo}>ScheduleFlow</div>
        <nav className={styles.sidebar_nav}>
          {[
            { icon: '⊞', label: 'Dashboard',         active: true,  onClick: null },
            { icon: '📅', label: 'Current Time Table', active: false, onClick: null },
            { icon: '👥', label: 'Users',              active: false, onClick: null },
            { icon: '⚙️', label: 'Settings',           active: false, onClick: null },
          ].map(item => (
            <button
              key={item.label}
              className={`${styles.nav_item} ${item.active ? styles.nav_active : ''}`}
              onClick={item.onClick}
            >
              <span className={styles.nav_icon}>{item.icon}</span>
              {item.label}
            </button>
          ))}

          {/* Advance Classroom badge */}
          <div className={styles.advance_row}>
            <span className={styles.advance_label}>Advance Classroom</span>
            <span className={styles.advance_badge}>Enabled Later</span>
          </div>

          <button className={styles.logout_btn} onClick={onLogout}>
            <span>↪</span> Logout
          </button>
        </nav>
      </aside>

      {/* ── Main area ── */}
      <main className={styles.main}>
        {/* Nav bar */}
        <header className={styles.topbar}>
          <span />
          <div className={styles.topbar_user}>
            <div className={styles.topbar_avatar}>{userName?.[0]?.toUpperCase() || 'U'}</div>
            <span className={styles.topbar_name}>Welcome ! {userName || 'User'}</span>
          </div>
        </header>

        <div className={styles.content}>
          {/* Welcome banner */}
          <div className={styles.welcome_card}>
            <h1 className={styles.welcome_title}>Welcome Admin</h1>
            <p className={styles.welcome_sub}>Manage your timetables and events with ease</p>
          </div>

          {/* Action cards */}
          <div className={styles.action_grid}>
            {/* New Timetable */}
            <div className={styles.action_card} onClick={onNewTimetable}>
              <div className={styles.action_icon_wrap}>
                <span className={styles.action_icon}>📅</span>
              </div>
              <h2 className={styles.action_title}>New Time Table</h2>
              <p className={styles.action_desc}>
                Create and manage comprehensive timetables for your institution
              </p>
              <button className={styles.action_btn}>Get Started →</button>
            </div>

            {/* Manage Events */}
            <div className={styles.action_card}>
              <div className={styles.action_icon_wrap} style={{background:'#e8eaf6'}}>
                <span className={styles.action_icon}>🎪</span>
              </div>
              <h2 className={styles.action_title}>Manage Events</h2>
              <p className={styles.action_desc}>
                Organize events, generate QR codes, and manage registrations
              </p>
              <button className={styles.action_btn} style={{background:'#6366f1'}}>
                View Events →
              </button>
            </div>

            {/* My Timetables */}
            <div className={styles.action_card}>
              <div className={styles.action_icon_wrap} style={{background:'#fef3c7'}}>
                <span className={styles.action_icon}>📋</span>
              </div>
              <h2 className={styles.action_title}>My Timetables</h2>
              <p className={styles.action_desc}>
                View and edit previously generated timetables for all sections
              </p>
              <button className={styles.action_btn} style={{background:'#d97706'}}>
                View All →
              </button>
            </div>
          </div>

          {/* Stats row */}
          <div className={styles.stats_row}>
            {[
              { icon: '📊', val: '12',  label: 'Total Timetables', color: '#0ABFBC' },
              { icon: '📤', val: '5',   label: 'Published',        color: '#0ABFBC' },
              { icon: '📄', val: '3',   label: 'Drafts',           color: '#e05252' },
              { icon: '👥', val: '48',  label: 'Teachers',         color: '#6366f1' },
            ].map(s => (
              <div key={s.label} className={styles.stat_card}>
                <div className={styles.stat_icon_wrap} style={{background: s.color + '22'}}>
                  <span style={{fontSize:'1.2rem'}}>{s.icon}</span>
                </div>
                <div className={styles.stat_body}>
                  <div className={styles.stat_val}>{s.val}</div>
                  <div className={styles.stat_label}>{s.label}</div>
                </div>
              </div>
            ))}
          </div>

          {/* Latest Events */}
          <div className={styles.events_card}>
            <h3 className={styles.events_title}>Latest Events</h3>
            <p className={styles.events_empty}>No events registered.</p>
          </div>
        </div>
      </main>

      {/* Chat bot button */}
      <button className={styles.chat_btn} title="Chat Support">💬</button>
    </div>
  );
}
