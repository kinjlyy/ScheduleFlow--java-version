// src/pages/public/LandingPage.jsx
import React, { useState, useEffect } from 'react';
import styles from './LandingPage.module.css';

const FEATURES = [
  { icon: '⚡', title: 'AI-Powered Scheduling', desc: 'DSatur graph coloring algorithm generates conflict-free timetables in seconds — no manual trial and error.' },
  { icon: '📅', title: 'Constraint Management', desc: 'Set teacher weekly caps, period limits, and day-off constraints — enforced automatically at generation time.' },
  { icon: '🔒', title: 'Real-time Validation', desc: 'See violations as you configure, so you catch conflicts before the algorithm even runs.' },
  { icon: '📊', title: 'Workload Analytics', desc: 'Track teacher workload, section utilization, and scheduling efficiency from a live dashboard.' },
  { icon: '📤', title: 'Export Anywhere', desc: 'Download timetables as CSV, push to Google Sheets, or share a direct link with your staff.' },
  { icon: '👥', title: 'Multi-Section Support', desc: 'Manage unlimited sections and departments simultaneously with zero scheduling conflicts.' },
];

const HOW_IT_WORKS = [
  { step: '01', title: 'Add your data', desc: 'Enter teachers, subjects, sections, and rooms. Import from CSV or fill the guided form.' },
  { step: '02', title: 'Set constraints', desc: 'Define weekly lecture caps, preferred slots, and unavailability. ScheduleFlow enforces every rule.' },
  { step: '03', title: 'Generate & export', desc: 'One click generates a fully conflict-free timetable. Export to CSV or Google Sheets in seconds.' },
];

const TEAM = [
  { name: 'Kinjal', role: 'Founder & Full-Stack Engineer', init: 'K' },
  { name: 'Aryan', role: 'Backend & Algorithms', init: 'A' },
  { name: 'Priya', role: 'UI/UX Design', init: 'P' },
];

export default function LandingPage({ onGetStarted }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 10);
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  return (
    <div className={styles.root}>

      {/* ── Navbar ── */}
      <header className={`${styles.nav} ${scrolled ? styles.nav_scrolled : ''}`}>
        <span className={styles.nav_logo}>ScheduleFlow</span>
        <nav className={styles.nav_links}>
          <a href="#home"    className={styles.nav_link + ' ' + styles.nav_active}>Home</a>
          <a href="#about"   className={styles.nav_link}>Features</a>
          <a href="#how"     className={styles.nav_link}>How it Works</a>
          <a href="#pricing" className={styles.nav_link}>Pricing</a>
          <a href="#team"    className={styles.nav_link}>Team</a>
        </nav>
        <button className={styles.get_started_btn} onClick={onGetStarted}>
          Get Started →
        </button>
      </header>

      {/* ── Hero ── */}
      <section className={styles.hero} id="home">
        <div className={styles.hero_content}>
          <h1 className={styles.hero_title}>
            Unified Timetable and College<br />Solutions Powered by AI
          </h1>
          <p className={styles.hero_sub}>
            Generate optimized, conflict-free college timetables in seconds.
            Define constraints, run the algorithm, and export — no spreadsheet juggling needed.
          </p>
          <div className={styles.hero_btns}>
            <button className={styles.btn_primary} onClick={onGetStarted}>
              Get Started — It's Free
            </button>
            <a href="#how" className={styles.btn_secondary}>
              See How It Works ↓
            </a>
          </div>
          <p className={styles.hero_social_proof}>
            Trusted by scheduling teams at colleges across India
          </p>
        </div>

        {/* Dashboard preview mockup */}
        <div className={styles.mockup_wrap}>
          <div className={styles.mockup}>
            <div className={styles.mockup_bar}>
              <div className={styles.mockup_traffic}>
                <span className={styles.dot_red} />
                <span className={styles.dot_yellow} />
                <span className={styles.dot_green} />
              </div>
              <span className={styles.mockup_url}>app.scheduleflow.in/dashboard</span>
              <div />
            </div>
            <div className={styles.mockup_body}>
              <div className={styles.mockup_sidebar}>
                <div className={styles.mockup_sidebar_brand}>SF</div>
                <div className={styles.mockup_sidebar_item + ' ' + styles.mockup_sidebar_active}>🏠 Dashboard</div>
                <div className={styles.mockup_sidebar_item}>📅 Timetable</div>
                <div className={styles.mockup_sidebar_item}>👥 Teachers</div>
                <div className={styles.mockup_sidebar_item}>⚙️ Settings</div>
              </div>
              <div className={styles.mockup_main}>
                <div className={styles.mockup_greeting}>
                  Good morning — here's your schedule overview
                </div>
                <div className={styles.mockup_stats}>
                  <div className={styles.mockup_stat}>
                    <span className={styles.mockup_stat_icon} style={{ background: '#0ABFBC' }}>📊</span>
                    <div>
                      <div className={styles.mockup_stat_val}>24</div>
                      <div className={styles.mockup_stat_label}>Sections</div>
                    </div>
                  </div>
                  <div className={styles.mockup_stat}>
                    <span className={styles.mockup_stat_icon} style={{ background: '#c9956a' }}>👩‍🏫</span>
                    <div>
                      <div className={styles.mockup_stat_val}>62</div>
                      <div className={styles.mockup_stat_label}>Teachers</div>
                    </div>
                  </div>
                  <div className={styles.mockup_stat}>
                    <span className={styles.mockup_stat_icon} style={{ background: '#059669' }}>✅</span>
                    <div>
                      <div className={styles.mockup_stat_val}>0</div>
                      <div className={styles.mockup_stat_label}>Conflicts</div>
                    </div>
                  </div>
                </div>
                <div className={styles.mockup_timetable_preview}>
                  {['Mon', 'Tue', 'Wed', 'Thu', 'Fri'].map((day, di) => (
                    <div key={day} className={styles.mockup_col}>
                      <div className={styles.mockup_day}>{day}</div>
                      {[0, 1, 2].map(i => (
                        <div
                          key={i}
                          className={styles.mockup_slot}
                          style={{ opacity: 0.4 + (di + i) * 0.12 }}
                        />
                      ))}
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
          <div className={styles.mockup_glow} />
        </div>
      </section>

      {/* ── Features ── */}
      <section className={styles.features_section} id="about">
        <div className={styles.section_header}>
          <h2 className={styles.section_title}>Everything you need</h2>
          <p className={styles.section_sub}>Powerful tools built for modern educational institutions</p>
        </div>
        <div className={styles.features_grid}>
          {FEATURES.map(f => (
            <div key={f.title} className={styles.feature_card}>
              <span className={styles.feature_icon}>{f.icon}</span>
              <div className={styles.feature_title}>{f.title}</div>
              <div className={styles.feature_desc}>{f.desc}</div>
            </div>
          ))}
        </div>
      </section>

      {/* ── How It Works ── */}
      <section className={styles.how_section} id="how">
        <div className={styles.section_header}>
          <h2 className={styles.section_title}>Up and running in 3 steps</h2>
          <p className={styles.section_sub}>No complex setup. No training required. Just results.</p>
        </div>
        <div className={styles.how_grid}>
          {HOW_IT_WORKS.map((h, i) => (
            <div key={h.step} className={styles.how_card}>
              <div className={styles.how_step}>{h.step}</div>
              <div className={styles.how_connector} style={{ display: i === HOW_IT_WORKS.length - 1 ? 'none' : undefined }} />
              <h3 className={styles.how_title}>{h.title}</h3>
              <p className={styles.how_desc}>{h.desc}</p>
            </div>
          ))}
        </div>
        <div className={styles.how_cta}>
          <button className={styles.btn_primary} onClick={onGetStarted}>
            Try it now — free
          </button>
        </div>
      </section>

      {/* ── Pricing ── */}
      <section className={styles.pricing} id="pricing">
        <div className={styles.section_header}>
          <h2 className={styles.section_title}>Simple, transparent pricing</h2>
          <p className={styles.section_sub}>Start free. Upgrade when you need more.</p>
        </div>
        <div className={styles.pricing_grid}>
          {[
            { name: 'Starter', price: 'Free', sub: 'Perfect to get started', features: ['2 Sections', '5 Teachers', 'Basic Timetable', 'CSV Export'], highlight: false },
            { name: 'Pro', price: '₹999/mo', sub: 'For growing institutions', features: ['Unlimited Sections', 'All Teachers', 'AI Optimization', 'Event Management', 'Google Sheets Export', 'Priority Support'], highlight: true },
            { name: 'Enterprise', price: 'Custom', sub: 'For large universities', features: ['Everything in Pro', 'Custom Integrations', 'Dedicated Support', 'SLA Guarantee', 'Onboarding Call'], highlight: false },
          ].map(p => (
            <div key={p.name} className={`${styles.price_card} ${p.highlight ? styles.price_highlight : ''}`}>
              {p.highlight && <div className={styles.price_badge}>Most Popular</div>}
              <div className={styles.price_name}>{p.name}</div>
              <div className={styles.price_val}>{p.price}</div>
              <div className={styles.price_sub}>{p.sub}</div>
              <hr className={styles.price_divider} />
              <ul className={styles.price_features}>
                {p.features.map(f => <li key={f}><span className={styles.check}>✓</span> {f}</li>)}
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
        <div className={styles.section_header}>
          <h2 className={styles.section_title}>Built by developers, for institutions</h2>
          <p className={styles.section_sub}>A small team obsessed with making scheduling frictionless</p>
        </div>
        <div className={styles.team_grid}>
          {TEAM.map((member) => (
            <div key={member.name} className={styles.team_card}>
              <div className={styles.team_avatar}>{member.init}</div>
              <div className={styles.team_name}>{member.name}</div>
              <div className={styles.team_role}>{member.role}</div>
            </div>
          ))}
        </div>
      </section>

      {/* ── CTA Banner ── */}
      <section className={styles.cta_banner}>
        <h2 className={styles.cta_title}>Ready to eliminate scheduling chaos?</h2>
        <p className={styles.cta_sub}>Join institutions already running conflict-free timetables with ScheduleFlow.</p>
        <button className={styles.cta_btn} onClick={onGetStarted}>
          Get Started for Free →
        </button>
      </section>

      {/* ── Footer ── */}
      <footer className={styles.footer} id="contact">
        <div className={styles.footer_top}>
          <div>
            <div className={styles.footer_logo}>ScheduleFlow</div>
            <p className={styles.footer_tag}>Smart TimeTable Mapping · Effortless Event Management</p>
          </div>
          <div className={styles.footer_links}>
            <a href="#about" className={styles.footer_link}>Features</a>
            <a href="#how" className={styles.footer_link}>How it Works</a>
            <a href="#pricing" className={styles.footer_link}>Pricing</a>
            <a href="#team" className={styles.footer_link}>Team</a>
          </div>
        </div>
        <div className={styles.footer_bottom}>
          <p className={styles.footer_copy}>© 2025 ScheduleFlow. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}
