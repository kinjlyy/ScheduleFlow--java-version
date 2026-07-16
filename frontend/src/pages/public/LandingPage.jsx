// src/pages/public/LandingPage.jsx
import React, { useState, useEffect, useRef } from 'react';
import styles from './LandingPage.module.css';

const TECH_HIGHLIGHTS = [
  { tag: 'Constraint Engine', title: 'Rules that actually stick', desc: 'Handles teacher availability, classroom capacity, subject requirements and academic rules automatically. No constraint is ignored.' },
  { tag: 'Scheduling Algorithm', title: 'Graph-based optimization', desc: 'Uses DSatur graph coloring and intelligent scheduling logic to generate clash-free timetables across all sections simultaneously.' },
  { tag: 'Real-Time Validation', title: 'Catch conflicts before they happen', desc: 'Detect scheduling violations as you configure data — not after publishing to students and faculty.' },
  { tag: 'Institution Management', title: 'One platform, any scale', desc: 'Manage multiple departments, classes, sections and academic schedules from a single dashboard — from small schools to large universities.' },
];

function useReveal() {
  const ref = useRef(null);
  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const observer = new IntersectionObserver(
      ([entry]) => { if (entry.isIntersecting) { el.classList.add(styles.revealed); observer.disconnect(); } },
      { threshold: 0.12 }
    );
    observer.observe(el);
    return () => observer.disconnect();
  }, []);
  return ref;
}

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

      {/* ── Problem / Solution ── */}
      <section className={styles.ps_section} id="about">
        <div className={styles.ps_header}>
          <h2 className={styles.ps_heading}>
            From manual scheduling chaos to<br />perfectly organized timetables
          </h2>
          <p className={styles.ps_sub}>
            Schools and colleges spend countless hours creating timetables while balancing teachers, classrooms,
            subjects, sections and academic constraints. ScheduleFlow automates this entire process.
          </p>
        </div>

        <div className={styles.ps_split}>
          {/* BEFORE */}
          <div className={styles.ps_col}>
            <div className={styles.ps_label_before}>Before ScheduleFlow</div>
            <ul className={styles.ps_problems}>
              {[
                'Multiple Excel sheets across departments',
                'Manual teacher allocation every semester',
                'Classroom double-bookings discovered last minute',
                'Section clashes nobody caught until day one',
                'Last-minute timetable changes cascade into chaos',
                'Hours of back-and-forth adjusting schedules',
              ].map(p => (
                <li key={p} className={styles.ps_problem_item}>
                  <span className={styles.ps_x}>×</span>
                  {p}
                </li>
              ))}
            </ul>
            {/* Messy spreadsheet visual */}
            <div className={styles.messy_sheet}>
              <div className={styles.sheet_header}>
                <span className={styles.sheet_tab_active}>Sheet1</span>
                <span className={styles.sheet_tab}>Sheet2</span>
                <span className={styles.sheet_tab}>Sheet3 (copy)</span>
                <span className={styles.sheet_tab}>FINAL_v3</span>
              </div>
              <div className={styles.sheet_grid}>
                <div className={styles.sheet_row + ' ' + styles.sheet_row_head}>
                  {['', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri'].map(d => <div key={d} className={styles.sheet_cell_head}>{d}</div>)}
                </div>
                {[
                  { period: 'P1', slots: ['Maths-A','—','Maths-A','Phys-B','—'] },
                  { period: 'P2', slots: ['Eng-C','Maths-A','—','Maths-A','Chem-A'] },
                  { period: 'P3', slots: ['—','Eng-C','Bio-D','—','Maths-A'] },
                  { period: 'P4', slots: ['Phys-B','—','Eng-C','Bio-D','—'] },
                ].map((row, ri) => (
                  <div key={ri} className={styles.sheet_row}>
                    <div className={styles.sheet_cell_period}>{row.period}</div>
                    {row.slots.map((s, si) => (
                      <div
                        key={si}
                        className={`${styles.sheet_cell} ${
                          s === 'Maths-A' && (ri > 0 || si > 0) ? styles.sheet_conflict : ''
                        } ${s === '—' ? styles.sheet_empty : ''}`}
                      >
                        {s}
                      </div>
                    ))}
                  </div>
                ))}
              </div>
              <div className={styles.sheet_warning}>⚠ Conflict detected: Mr. Sharma — Mon P1 &amp; Tue P2</div>
            </div>
          </div>

          {/* Arrow */}
          <div className={styles.ps_arrow}>
            <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
              <path d="M8 20h24M24 12l8 8-8 8" stroke="#c9956a" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>

          {/* AFTER */}
          <div className={styles.ps_col}>
            <div className={styles.ps_label_after}>With ScheduleFlow</div>
            <ul className={styles.ps_solutions}>
              {[
                'Timetable generated automatically in one click',
                'Conflict-free scheduling across all sections',
                'Teacher workload balanced within weekly caps',
                'Export timetables to CSV or Google Sheets instantly',
                'Section-wise timetable with instant publish',
                'Constraint violations caught before generation',
              ].map(s => (
                <li key={s} className={styles.ps_solution_item}>
                  <span className={styles.ps_tick}>✓</span>
                  {s}
                </li>
              ))}
            </ul>
            {/* Clean dashboard visual */}
            <div className={styles.clean_dash}>
              <div className={styles.dash_topbar}>
                <span className={styles.dash_brand}>ScheduleFlow</span>
                <span className={styles.dash_status}>All clear — 0 conflicts</span>
              </div>
              <div className={styles.dash_body}>
                <div className={styles.dash_meta}>
                  <span className={styles.dash_meta_item}><span className={styles.dash_dot_teal} />Sec A — Yr 2</span>
                  <span className={styles.dash_meta_item}><span className={styles.dash_dot_amber} />Generated just now</span>
                </div>
                <div className={styles.dash_tt_grid}>
                  <div className={styles.dash_col_head} />
                  {['Mon', 'Tue', 'Wed', 'Thu', 'Fri'].map(d => (
                    <div key={d} className={styles.dash_col_head}>{d}</div>
                  ))}
                  {[
                    { period: '9:00', slots: ['Mathematics', 'Physics', 'English', 'Chemistry', 'Mathematics'] },
                    { period: '10:00', slots: ['Physics', 'English', 'Mathematics', 'English', 'Biology'] },
                    { period: '11:00', slots: ['English', 'Mathematics', 'Physics', 'Biology', 'Chemistry'] },
                    { period: '12:00', slots: ['Chemistry', 'Biology', 'Chemistry', 'Mathematics', 'Physics'] },
                  ].map((row, ri) => (
                    <React.Fragment key={ri}>
                      <div className={styles.dash_period}>{row.period}</div>
                      {row.slots.map((subj, si) => (
                        <div
                          key={si}
                          className={styles.dash_cell}
                          style={{ animationDelay: `${(ri * 5 + si) * 60}ms` }}
                        >
                          {subj}
                        </div>
                      ))}
                    </React.Fragment>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* ── Tech Highlights ── */}
        <div className={styles.tech_grid}>
          {TECH_HIGHLIGHTS.map((t, i) => (
            <div key={t.tag} className={styles.tech_card} style={{ animationDelay: `${i * 80}ms` }}>
              <div className={styles.tech_tag}>{t.tag}</div>
              <div className={styles.tech_title}>{t.title}</div>
              <p className={styles.tech_desc}>{t.desc}</p>
            </div>
          ))}
        </div>

        {/* ── Trust Strip ── */}
        <div className={styles.trust_strip}>
          <div className={styles.trust_heading}>Built for modern education systems</div>
          <div className={styles.trust_types}>
            {['Schools', 'Colleges', 'Universities', 'Training Institutes'].map(t => (
              <div key={t} className={styles.trust_type}>{t}</div>
            ))}
          </div>
          <p className={styles.trust_desc}>
            Whether managing a small school timetable or a large college with multiple departments,
            ScheduleFlow adapts to your academic structure.
          </p>
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

      {/* ── Developer Section ── */}
      <DeveloperSection />

      {/* ── CTA + Footer ── */}
      <CtaFooter onGetStarted={onGetStarted} />
    </div>
  );
}

/* ─────────────────────────────────────────────
   Developer / Founder Section  (compact, beige)
───────────────────────────────────────────── */
const DEV_SNIPPETS = [
  { text: '{\n  conflict: 0\n}', delay: '0s', duration: '8s', top: '12%', left: '-8%' },
  { text: 'graph.solve()', delay: '1s', duration: '9s', top: '35%', left: '84%' },
  { text: 'build(timetable)', delay: '2.5s', duration: '10s', top: '72%', left: '74%' },
  { text: '@SpringBoot', delay: '0.8s', duration: '7.5s', top: '68%', left: '-12%' },
  { text: 'optimize()', delay: '1.8s', duration: '11s', top: '88%', left: '32%' },
  { text: 'O(n log n)', delay: '3s', duration: '8.5s', top: '48%', left: '-18%' },
  { text: 'SELECT slots', delay: '1.2s', duration: '9.5s', top: '8%', left: '68%' },
  { text: 'useEffect()', delay: '2.2s', duration: '10.5s', top: '40%', left: '-10%' },
];

function DeveloperSection() {
  const frameRef = useRef(null);
  const [coords, setCoords] = React.useState({ x: 0, y: 0 });
  const [isHovered, setIsHovered] = React.useState(false);

  const handleMouseMove = (e) => {
    const el = frameRef.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();
    const x = (e.clientX - rect.left) / rect.width - 0.5; // -0.5 to 0.5
    const y = (e.clientY - rect.top) / rect.height - 0.5;  // -0.5 to 0.5
    setCoords({ x, y });
  };

  const handleMouseLeave = () => {
    setIsHovered(false);
    setCoords({ x: 0, y: 0 });
  };

  // 3D rotation values
  const rotateX = coords.y * -24; // tilt up/down
  const rotateY = coords.x * 24;  // tilt left/right
  const glareX = (coords.x + 0.5) * 100;
  const glareY = (coords.y + 0.5) * 100;

  return (
    <section className={styles.dev_section} id="team">
      {/* Warm beige gradient background */}
      <div className={styles.dev_bg} aria-hidden="true" />

      <div className={styles.dev_inner}>

        {/* ── LEFT: Interactive 3D Avatar ── */}
        <div className={styles.dev_photo_col}>

          {/* Floating glassmorphism code tags */}
          {DEV_SNIPPETS.map((s, i) => (
            <span
              key={i}
              className={styles.dev_glass_tag}
              style={{
                top: s.top,
                left: s.left,
                animationDelay: s.delay,
                animationDuration: s.duration,
              }}
              aria-hidden="true"
            >
              <pre>{s.text}</pre>
            </span>
          ))}

          {/* 3D Photo card wrapper */}
          <div
            ref={frameRef}
            className={`${styles.dev_photo_3d_wrapper} ${isHovered ? styles.dev_photo_active : ''}`}
            onMouseMove={handleMouseMove}
            onMouseEnter={() => setIsHovered(true)}
            onMouseLeave={handleMouseLeave}
            style={{
              transform: `perspective(800px) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`,
            }}
          >
            {/* Ambient gold/teal glow behind photo */}
            <div className={styles.dev_glow_ring} />

            {/* Photo layer */}
            <div className={styles.dev_photo_container}>
              <img
                src="/kinjal.jpg"
                alt="Kinjal Gupta — Developer of ScheduleFlow"
                className={styles.dev_actual_photo}
              />
              {/* Dynamic glare/shimmer based on mouse position */}
              {isHovered && (
                <div
                  className={styles.dev_glare}
                  style={{
                    background: `radial-gradient(circle at ${glareX}% ${glareY}%, rgba(255, 255, 255, 0.45) 0%, transparent 60%)`,
                  }}
                />
              )}
            </div>

            {/* Simulated blink/reflection scanlines */}
            <div className={styles.dev_scanline} />
          </div>

          {/* Status badge below photo */}
          <div className={styles.dev_badge_pill}>
            <span className={styles.dev_badge_status_dot} />
            <span>Kinjal Gupta · Developer</span>
          </div>
        </div>

        {/* ── RIGHT: Copy, Mission and Tags ── */}
        <div className={styles.dev_content}>
          <div className={styles.dev_eyebrow}>Meet the Developer</div>
          <h2 className={styles.dev_heading}>Hi, I'm Kinjal</h2>

          <p className={styles.dev_intro}>
            I am a Computer Science student and the developer behind ScheduleFlow.
            I build technology solutions that transform real-world challenges into scalable
            software products. ScheduleFlow was created to simplify academic scheduling
            using algorithms, automation, and modern engineering.
          </p>

          {/* Mission */}
          <div className={styles.dev_mission_card}>
            <div className={styles.dev_mission_header}>My Mission</div>
            <p className={styles.dev_mission_quote}>
              "My mission is to transform real-world challenges into impactful technology
              solutions by combining engineering, innovation, and creative problem-solving."
            </p>
          </div>

          {/* Tags */}
          <div className={styles.dev_tags_row}>
            {['Software Engineer', 'Full Stack Developer', 'Problem Solver'].map(t => (
              <span key={t} className={styles.dev_tag_chip}>{t}</span>
            ))}
          </div>
        </div>

      </div>
    </section>
  );
}




/* ─────────────────────────────────────────────
   Separated component so it can manage its own
   mouse-interaction state cleanly
───────────────────────────────────────────── */
function CtaFooter({ onGetStarted }) {
  const sectionRef = useRef(null);
  const [mouse, setMouse] = React.useState({ x: 0.5, y: 0.5 });

  useEffect(() => {
    const el = sectionRef.current;
    if (!el) return;
    const handleMove = (e) => {
      const rect = el.getBoundingClientRect();
      setMouse({
        x: (e.clientX - rect.left) / rect.width,
        y: (e.clientY - rect.top)  / rect.height,
      });
    };
    el.addEventListener('mousemove', handleMove);
    return () => el.removeEventListener('mousemove', handleMove);
  }, []);

  const px = (mouse.x - 0.5) * 28;
  const py = (mouse.y - 0.5) * 18;

  const SUBJECTS = ['Mathematics', 'Physics', 'English', 'Chemistry', 'Biology', 'Computer Sc.'];
  const COLORS   = ['#0ABFBC', '#c9956a', '#059669', '#6366f1', '#f59e0b', '#ec4899'];
  const DAYS     = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'];
  const PERIODS  = ['9:00', '10:00', '11:00', '12:00', '2:00'];

  // deterministic cell data
  const cell = (r, c) => ({ subj: SUBJECTS[(r * 3 + c * 2) % SUBJECTS.length], color: COLORS[(r + c) % COLORS.length] });

  return (
    <>
      {/* ── CTA Section ── */}
      <section className={styles.cta_section} ref={sectionRef} id="contact">

        {/* Animated grid background */}
        <div className={styles.cta_grid_bg} aria-hidden="true">
          {Array.from({ length: 120 }).map((_, i) => (
            <div key={i} className={styles.cta_grid_cell} />
          ))}
        </div>

        {/* Floating blobs reacting to mouse */}
        <div
          className={styles.blob1}
          style={{ transform: `translate(${px * 0.6}px, ${py * 0.5}px)` }}
          aria-hidden="true"
        />
        <div
          className={styles.blob2}
          style={{ transform: `translate(${-px * 0.4}px, ${-py * 0.3}px)` }}
          aria-hidden="true"
        />

        <div className={styles.cta_inner}>
          {/* Text */}
          <div className={styles.cta_text}>
            <div className={styles.cta_eyebrow}>Scheduling infrastructure for education</div>
            <h2 className={styles.cta_heading}>
              Ready to transform the way your<br />institution manages schedules?
            </h2>
            <p className={styles.cta_sub}>
              From schools to universities, ScheduleFlow helps create optimized timetables
              without hours of manual effort.
            </p>
            <div className={styles.cta_actions}>
              <button className={styles.cta_btn_primary} onClick={onGetStarted}>
                Start Building Your Timetable →
              </button>
              <a href="#how" className={styles.cta_btn_ghost}>
                Explore How It Works
              </a>
            </div>
          </div>

          {/* Floating dashboard product preview */}
          <div
            className={styles.cta_preview}
            style={{ transform: `translate(${px * 0.35}px, ${py * 0.25}px)` }}
          >
            {/* Analytics bar */}
            <div className={styles.prev_stats}>
              <div className={styles.prev_stat}>
                <div className={styles.prev_stat_val}>24</div>
                <div className={styles.prev_stat_lbl}>Sections</div>
              </div>
              <div className={styles.prev_stat}>
                <div className={styles.prev_stat_val}>62</div>
                <div className={styles.prev_stat_lbl}>Teachers</div>
              </div>
              <div className={styles.prev_stat_ok}>
                <span className={styles.prev_ok_dot} />
                0 Conflicts
              </div>
            </div>

            {/* Timetable grid */}
            <div className={styles.prev_grid}>
              {/* header row */}
              <div className={styles.prev_th} />
              {DAYS.map(d => <div key={d} className={styles.prev_th}>{d}</div>)}

              {/* data rows */}
              {PERIODS.map((p, ri) => (
                <React.Fragment key={p}>
                  <div className={styles.prev_period}>{p}</div>
                  {DAYS.map((_, ci) => {
                    const c = cell(ri, ci);
                    return (
                      <div
                        key={ci}
                        className={styles.prev_cell}
                        style={{
                          borderLeftColor: c.color,
                          animationDelay: `${(ri * 5 + ci) * 50}ms`,
                        }}
                      >
                        <span className={styles.prev_cell_subj}>{c.subj}</span>
                      </div>
                    );
                  })}
                </React.Fragment>
              ))}
            </div>

            {/* Teacher allocation row */}
            <div className={styles.prev_teachers}>
              <span className={styles.prev_teachers_label}>Allocated —</span>
              {['Mr. Sharma', 'Ms. Patel', 'Dr. Rao'].map(t => (
                <span key={t} className={styles.prev_teacher_chip}>{t}</span>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* ── Footer ── */}
      <footer className={styles.footer} id="footer">
        <div className={styles.footer_main}>
          {/* Brand */}
          <div className={styles.footer_brand}>
            <div className={styles.footer_logo}>ScheduleFlow</div>
            <p className={styles.footer_tagline}>
              Smart scheduling infrastructure for schools,<br />colleges and universities.
            </p>
          </div>

          {/* Product links */}
          <div className={styles.footer_col}>
            <div className={styles.footer_col_title}>Product</div>
            <a href="#about" className={styles.footer_link}>Features</a>
            <a href="#how"   className={styles.footer_link}>How it Works</a>
            <a href="#team"  className={styles.footer_link}>Team</a>
          </div>

          {/* Connect links */}
          <div className={styles.footer_col}>
            <div className={styles.footer_col_title}>Connect</div>
            <a href="https://github.com/kinjlyy/ScheduleFlow--java-version" target="_blank" rel="noreferrer" className={styles.footer_link}>GitHub</a>
            <a href="https://linkedin.com" target="_blank" rel="noreferrer" className={styles.footer_link}>LinkedIn</a>
            <a href="mailto:contact@scheduleflow.in" className={styles.footer_link}>Contact</a>
          </div>
        </div>

        <div className={styles.footer_bottom}>
          <p className={styles.footer_built}>
            Built with passion by a developer solving real academic scheduling problems.
          </p>
          <p className={styles.footer_copy}>© 2025 ScheduleFlow. All rights reserved.</p>
        </div>
      </footer>
    </>
  );
}
