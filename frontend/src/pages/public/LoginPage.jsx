// src/pages/public/LoginPage.jsx
import React, { useState } from 'react';
import styles from './LoginPage.module.css';

export default function LoginPage({ onLogin, onBack }) {
  const [tab, setTab]       = useState('login'); // 'login' | 'register'
  const [email, setEmail]   = useState('');
  const [pass, setPass]     = useState('');
  const [name, setName]     = useState('');
  const [error, setError]   = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    if (!email || !pass) { setError('Please fill in all fields.'); return; }
    if (tab === 'register' && !name) { setError('Please enter your name.'); return; }

    const url = tab === 'login' ? '/api/auth/login' : '/api/auth/register';
    const payload = tab === 'login' ? { email, password: pass } : { name, email, password: pass };

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const errText = await response.text();
        throw new Error(errText || 'Authentication failed');
      }

      const data = await response.json();
      localStorage.setItem('token', data.token);
      localStorage.setItem('userName', data.name);
      onLogin(data.name || email.split('@')[0]);
    } catch (err) {
      setError(err.message);
    }
  }
  return (
    <div className={styles.root}>
      {/* Left panel */}
      <div className={styles.left}>
        <button className={styles.back_btn} onClick={onBack}>← Back to Home</button>
        <div className={styles.brand}>
          <span className={styles.brand_logo}>ScheduleFlow</span>
          <p className={styles.brand_tag}>Smart TimeTable Mapping, Effortless Event Management</p>
        </div>
        <div className={styles.left_illo}>
          <div className={styles.illo_card}>
            <div className={styles.illo_row}>
              <span className={styles.illo_dot} style={{background:'#0ABFBC'}} />
              <span className={styles.illo_bar} style={{width:'60%'}} />
            </div>
            <div className={styles.illo_row}>
              <span className={styles.illo_dot} style={{background:'#c9956a'}} />
              <span className={styles.illo_bar} style={{width:'80%'}} />
            </div>
            <div className={styles.illo_row}>
              <span className={styles.illo_dot} style={{background:'#059669'}} />
              <span className={styles.illo_bar} style={{width:'45%'}} />
            </div>
            <div className={styles.illo_label}>Conflict-free Timetable Generated ✓</div>
          </div>
        </div>
      </div>

      {/* Right panel — form */}
      <div className={styles.right}>
        <div className={styles.form_wrap}>
          <h1 className={styles.form_title}>
            {tab === 'login' ? 'Welcome back' : 'Create account'}
          </h1>
          <p className={styles.form_sub}>
            {tab === 'login'
              ? 'Sign in to manage your timetables'
              : 'Join ScheduleFlow today'}
          </p>

          {/* Tabs */}
          <div className={styles.tabs}>
            <button
              className={`${styles.tab} ${tab === 'login' ? styles.tab_active : ''}`}
              onClick={() => setTab('login')}
            >Sign In</button>
            <button
              className={`${styles.tab} ${tab === 'register' ? styles.tab_active : ''}`}
              onClick={() => setTab('register')}
            >Register</button>
          </div>

          <form className={styles.form} onSubmit={handleSubmit}>
            {tab === 'register' && (
              <div className={styles.field}>
                <label className={styles.label}>Full Name</label>
                <input
                  className={styles.input}
                  type="text"
                  placeholder="Your name"
                  value={name}
                  onChange={e => setName(e.target.value)}
                />
              </div>
            )}
            <div className={styles.field}>
              <label className={styles.label}>Email Address</label>
              <input
                className={styles.input}
                type="email"
                placeholder="you@institution.edu"
                value={email}
                onChange={e => setEmail(e.target.value)}
              />
            </div>
            <div className={styles.field}>
              <label className={styles.label}>Password</label>
              <input
                className={styles.input}
                type="password"
                placeholder="••••••••"
                value={pass}
                onChange={e => setPass(e.target.value)}
              />
            </div>

            {error && <div className={styles.error}>{error}</div>}

            {tab === 'login' && (
              <div className={styles.forgot_row}>
                <a href="#" className={styles.forgot}>Forgot password?</a>
              </div>
            )}

            <button className={styles.submit_btn} type="submit">
              {tab === 'login' ? 'Sign In →' : 'Create Account →'}
            </button>
          </form>

          <p className={styles.switch_text}>
            {tab === 'login' ? "Don't have an account? " : 'Already have an account? '}
            <button
              className={styles.switch_link}
              onClick={() => setTab(tab === 'login' ? 'register' : 'login')}
            >
              {tab === 'login' ? 'Register' : 'Sign in'}
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}
