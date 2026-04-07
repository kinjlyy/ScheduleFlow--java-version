// src/pages/public/LandingPage.jsx
import React, { useState } from 'react';
import styles from './LandingPage.module.css';

export default function LandingPage({ onGetStarted }) {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <div className={styles.root}>
      {/* ── Navbar ── */}
      <header className={styles.nav}>
        <span className={styles.nav_logo}>ScheduleFlow</span>
        <nav className={styles.nav_links}>
          <a href="#home"    className={styles.nav_link + ' ' + styles.nav_active}>Home</a>
          <a href="#about"   className={styles.nav_link}>About</a>
          <a href="#pricing" className={styles.nav_link}>Pricing</a>
          <a href="#team"    className={styles.nav_link}>Team</a>
          <a href="#contact" className={styles.nav_link}>Contact</a>
        </nav>
        <button className={styles.get_started_btn} onClick={onGetStarted}>
          Get Started
        </button>
      </header>

      {/* ── Hero ── */}
      <section className={styles.hero} id="home">
        <div className={styles.hero_content}>
          <h1 className={styles.hero_title}>
            Unified Timetable and College<br />Solutions Powered by AI
          </h1>
          <p className={styles.hero_sub}>
            Smart TimeTable Mapping, Effortless Event Management<br />
            All Synced in One Platform.
          </p>
          <div className={styles.hero_btns}>
            <button className={styles.btn_watch}>Watch Video ▶</button>
            <button className={styles.btn_learn} onClick={onGetStarted}>Learn More →</button>
          </div>
        </div>

        {/* Feature Cards */}
        <div className={styles.features}>
          <div className={styles.feature_card}>
            <div className={styles.feature_icon}>🚀</div>
            <h3>Smart Scheduling</h3>
            <p>Our algorithms distribute lectures based on your availability and constraints instantly.</p>
          </div>
          <div className={styles.feature_card}>
            <div className={styles.feature_icon}>📅</div>
            <h3>Constraint Management</h3>
            <p>Set weekly caps for teachers and schedule dimensions with ease.</p>
          </div>
          <div className={styles.feature_card}>
            <div className={styles.feature_icon}>👩‍🏫</div>
            <h3>Real-time Validation</h3>
            <p>See violations as you type, ensuring a conflict-free timetable every time.</p>
          </div>
        </div>

        {/* Dashboard preview mockup */}
        <div className={styles.mockup_wrap}>
          <div className={styles.mockup}>
            <div className={styles.mockup_bar}>
              <span className={styles.mockup_logo}>Logo</span>
              <div className={styles.mockup_dots}>
                <span /><span /><span />
              </div>
            </div>
            <div className={styles.mockup_body}>
              <div className={styles.mockup_sidebar}>
                <div className={styles.mockup_sidebar_item + ' ' + styles.mockup_sidebar_active}>
                  🏠 Dashboard
                </div>
                <div className={styles.mockup_sidebar_item}>📅 My timetable</div>
                <div className={styles.mockup_sidebar_item}>👥 Users</div>
                <div className={styles.mockup_sidebar_item}>⚙️ Settings</div>
              </div>
              <div className={styles.mockup_main}>
                <div className={styles.mockup_greeting}>Hello Name — Dashboard</div>
                <div className={styles.mockup_stats}>
                  <div className={styles.mockup_stat}>
                    <span className={styles.mockup_stat_icon} style={{background:'#0ABFBC'}}>📊</span>
                    <div><div className={styles.mockup_stat_val}>300</div><div className={styles.mockup_stat_label}>Total Statistics</div></div>
                  </div>
                  <div className={styles.mockup_stat}>
                    <span className={styles.mockup_stat_icon} style={{background:'#0ABFBC'}}>📤</span>
                    <div><div className={styles.mockup_stat_val}>15</div><div className={styles.mockup_stat_label}>Published Items</div></div>
                  </div>
                  <div className={styles.mockup_stat}>
                    <span className={styles.mockup_stat_icon} style={{background:'#e05252'}}>📄</span>
                    <div><div className={styles.mockup_stat_val}>7</div><div className={styles.mockup_stat_label}>Drafts</div></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ── Features ── */}
      <section className={styles.features} id="about">
        <h2 className={styles.section_title}>Everything you need</h2>
        <p className={styles.section_sub}>Powerful tools built for modern educational institutions</p>
        <div className={styles.features_grid}>
          {[
            { icon: '⚡', title: 'AI-Powered Scheduling', desc: 'DSatur graph coloring algorithm generates conflict-free timetables instantly.' },
            { icon: '📅', title: 'Event Management', desc: 'Organize college events, generate QR codes, and track registrations in one place.' },
            { icon: '🔒', title: 'Smart Constraints', desc: 'Set teacher caps, period limits, and day constraints — enforced automatically.' },
            { icon: '📊', title: 'Real-time Analytics', desc: 'Track teacher workload, section utilization, and scheduling efficiency at a glance.' },
            { icon: '📤', title: 'Export Anywhere', desc: 'Download timetables as CSV, share with staff, or publish to students instantly.' },
            { icon: '👥', title: 'Multi-Section Support', desc: 'Manage unlimited sections simultaneously with zero scheduling conflicts.' },
          ].map(f => (
            <div key={f.title} className={styles.feature_card}>
              <span className={styles.feature_icon}>{f.icon}</span>
              <div className={styles.feature_title}>{f.title}</div>
              <div className={styles.feature_desc}>{f.desc}</div>
            </div>
          ))}
        </div>
      </section>

      {/* ── Pricing ── */}
      <section className={styles.pricing} id="pricing">
        <h2 className={styles.section_title}>Simple Pricing</h2>
        <p className={styles.section_sub}>Choose the plan that fits your institution</p>
        <div className={styles.pricing_grid}>
          {[
            { name: 'Starter', price: 'Free', features: ['2 Sections', '5 Teachers', 'Basic Timetable', 'CSV Export'], highlight: false },
            { name: 'Pro', price: '₹999/mo', features: ['Unlimited Sections', 'All Teachers', 'AI Optimization', 'Event Management', 'Priority Support'], highlight: true },
            { name: 'Enterprise', price: 'Custom', features: ['Everything in Pro', 'Custom Integrations', 'Dedicated Support', 'SLA Guarantee'], highlight: false },
          ].map(p => (
            <div key={p.name} className={`${styles.price_card} ${p.highlight ? styles.price_highlight : ''}`}>
              <div className={styles.price_name}>{p.name}</div>
              <div className={styles.price_val}>{p.price}</div>
              <ul className={styles.price_features}>
                {p.features.map(f => <li key={f}>✓ {f}</li>)}
              </ul>
              <button
                className={p.highlight ? styles.price_btn_primary : styles.price_btn}
                onClick={onGetStarted}
              >
                Get Started
              </button>
            </div>
          ))}
        </div>
      </section>

      {/* ── Team ── */}
      <section className={styles.team} id="team">
        <h2 className={styles.section_title}>Meet the Team</h2>
        <div className={styles.team_grid}>
          {['Alice', 'Bob', 'Carol', 'Dave'].map((name, i) => (
            <div key={name} className={styles.team_card}>
              <div className={styles.team_avatar}>{name[0]}</div>
              <div className={styles.team_name}>{name}</div>
              <div className={styles.team_role}>{['CEO','CTO','Designer','Engineer'][i]}</div>
            </div>
          ))}
        </div>
      </section>

      {/* ── Footer ── */}
      <footer className={styles.footer} id="contact">
        <div className={styles.footer_logo}>ScheduleFlow</div>
        <p className={styles.footer_tag}>Smart TimeTable Mapping · Effortless Event Management</p>
        <p className={styles.footer_copy}>© 2025 ScheduleFlow. All rights reserved.</p>
      </footer>
    </div>
  );
}
