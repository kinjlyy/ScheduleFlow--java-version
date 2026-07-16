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
   Developer / Founder Section
───────────────────────────────────────────── */
const CODE_CHARS = ['0','1','{','}','<','>','/','=',';','(',')','.','#','&','|','!','~','$','%','+','-','*','@','[',']'];
const FLOAT_SNIPPETS = [
  'const timetable = generate();',
  'if (conflict) resolve();',
  'Graph.shortestPath()',
  'db.query(schedule)',
  'O(n log n)',
  'Spring @RestController',
  'useEffect(() => {})',
  'SELECT * FROM slots',
  'class Scheduler {',
  'optimize(constraints)',
];

function DeveloperSection() {
  const canvasRef  = useRef(null);
  const sectionRef = useRef(null);
  const [mouse, setMouse] = React.useState({ x: 0.5, y: 0.5 });
  const [loaded, setLoaded] = React.useState(false);

  // Mouse tracking for portrait parallax
  useEffect(() => {
    const el = sectionRef.current;
    if (!el) return;
    const onMove = (e) => {
      const r = el.getBoundingClientRect();
      setMouse({ x: (e.clientX - r.left) / r.width, y: (e.clientY - r.top) / r.height });
    };
    el.addEventListener('mousemove', onMove);
    return () => el.removeEventListener('mousemove', onMove);
  }, []);

  // ASCII canvas portrait effect
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.src = '/kinjal.jpg';
    img.onload = () => {
      setLoaded(true);
      const W = canvas.width  = canvas.offsetWidth;
      const H = canvas.height = canvas.offsetHeight;

      // Draw image to offscreen canvas to sample pixels
      const off = document.createElement('canvas');
      // Scale to fit portrait with proper aspect
      const imgAspect = img.naturalWidth / img.naturalHeight;
      const canAspect = W / H;
      let drawW, drawH, drawX, drawY;
      if (imgAspect > canAspect) { drawH = H; drawW = H * imgAspect; } 
      else { drawW = W; drawH = W / imgAspect; }
      drawX = (W - drawW) / 2; drawY = (H - drawH) / 2;
      off.width = W; off.height = H;
      const oc = off.getContext('2d');
      oc.drawImage(img, drawX, drawY, drawW, drawH);

      const COLS = 72, ROWS = 90;
      const cw = W / COLS, ch = H / ROWS;

      ctx.clearRect(0, 0, W, H);
      ctx.font = `${Math.max(6, cw * 0.8)}px monospace`;
      ctx.textAlign = 'center';

      for (let row = 0; row < ROWS; row++) {
        for (let col = 0; col < COLS; col++) {
          const px = Math.floor((col / COLS) * W);
          const py = Math.floor((row / ROWS) * H);
          const pxData = oc.getImageData(px, py, 1, 1).data;
          const [r, g, b, a] = pxData;
          if (a < 30) continue;

          // Brightness → character density
          const bright = (r * 0.299 + g * 0.587 + b * 0.114) / 255;
          const charIdx = Math.floor(bright * (CODE_CHARS.length - 1));
          const char = CODE_CHARS[charIdx];

          // Slightly desaturate & tint toward brand colours
          const alpha = 0.55 + bright * 0.45;
          ctx.fillStyle = `rgba(${r},${g},${b},${alpha.toFixed(2)})`;
          ctx.fillText(char, col * cw + cw / 2, row * ch + ch);
        }
      }
    };
    img.onerror = () => setLoaded(true); // fallback: just show img
  }, []);

  const px = (mouse.x - 0.5) * 22;
  const py = (mouse.y - 0.5) * 14;

  const TECH = ['React', 'Spring Boot', 'PostgreSQL', 'Graph Algorithms', 'Optimization Techniques'];

  return (
    <section className={styles.dev_section} id="team" ref={sectionRef}>
      {/* bg grid */}
      <div className={styles.dev_bg_grid} aria-hidden="true" />

      {/* floating code snippets */}
      {FLOAT_SNIPPETS.map((s, i) => (
        <div
          key={i}
          className={styles.float_snippet}
          style={{
            top:  `${8 + (i * 8.5) % 80}%`,
            left: i % 2 === 0 ? `${2 + (i * 3) % 8}%` : `${78 + (i * 2) % 18}%`,
            animationDelay: `${i * 0.7}s`,
            animationDuration: `${14 + (i % 4) * 3}s`,
          }}
          aria-hidden="true"
        >{s}</div>
      ))}

      <div className={styles.dev_inner}>
        {/* ── LEFT: Portrait ── */}
        <div
          className={styles.dev_portrait_wrap}
          style={{ transform: `translate(${px * 0.4}px, ${py * 0.3}px)` }}
        >
          {/* Glow ring */}
          <div className={styles.portrait_glow} aria-hidden="true" />

          {/* Canvas ASCII portrait */}
          <canvas ref={canvasRef} className={styles.portrait_canvas} />

          {/* Fallback photo shown until canvas renders */}
          {!loaded && (
            <img src="/kinjal.jpg" alt="Kinjal Gupta" className={styles.portrait_fallback} />
          )}

          {/* Name badge */}
          <div className={styles.portrait_badge}>
            <span className={styles.portrait_badge_dot} />
            <span>Kinjal Gupta · CS Student & Developer</span>
          </div>
        </div>

        {/* ── RIGHT: Story card ── */}
        <div className={styles.dev_card}>
          <div className={styles.dev_eyebrow}>Meet the Developer</div>
          <h2 className={styles.dev_heading}>Hi, I'm Kinjal</h2>

          <div className={styles.dev_story}>
            <p>
              I am a Computer Science student and the developer behind ScheduleFlow.
            </p>
            <p>
              While working with academic scheduling problems, I noticed how much time
              schools and colleges spend manually managing teachers, classrooms, subjects
              and constraints.
            </p>
            <p>
              Creating timetables should not require endless Excel sheets and manual
              adjustments. I built ScheduleFlow to solve this problem using optimization
              algorithms and modern software engineering.
            </p>
          </div>

          {/* Role */}
          <div className={styles.dev_role_row}>
            <span className={styles.dev_role_pill}>Software Engineer</span>
            <span className={styles.dev_role_pill}>Full Stack Developer</span>
          </div>

          {/* Built with */}
          <div className={styles.dev_tech_label}>Built with</div>
          <div className={styles.dev_tech_row}>
            {TECH.map(t => (
              <span key={t} className={styles.dev_tech_chip}>{t}</span>
            ))}
          </div>

          {/* Mission */}
          <blockquote className={styles.dev_mission}>
            "My mission is to transform real-world challenges into impactful technology
            solutions by combining engineering, innovation, and creative problem-solving.
            I strive to build scalable products that simplify complex processes, improve
            experiences, and create meaningful impact through technology."
          </blockquote>
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
