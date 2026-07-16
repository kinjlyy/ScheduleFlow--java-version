// src/pages/ResultPage.jsx
import React, { useState } from 'react';
import { Button, Alert, StatCard, Spinner, SectionTitle, Card, FieldLabel } from '../components/UI.jsx';
import TimetableGrid from '../components/TimetableGrid.jsx';
import StepFooter from '../components/StepFooter.jsx';
import styles from './ResultPage.module.css';

const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

export default function ResultPage({
  sections,
  daysPerWeek,
  periodsPerDay,
  onGenerate,
  loading,
  result,
  error,
  onPrev,
}) {
  const [filterSectionId, setFilterSectionId] = useState('');
  const [exporting, setExporting] = useState(false);

  const GAS_URL = 'https://script.google.com/macros/s/AKfycbwuLYQe4ZsmbhLT34_KPisIwumh1V-HB71jiT-WgDlv7BFR3o51LtUJD660528WdTV2/exec';

  async function handleExportToGoogleSheets() {
    if (!result?.timetable) return;
    setExporting(true);
    try {
      const payload = {
        sections: sections.map(s => ({ id: s.id, name: s.name })),
        timetable: result.timetable,
        days: DAYS.slice(0, daysPerWeek),
        periods: periodsPerDay
      };

      const response = await fetch(GAS_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain;charset=utf-8' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) throw new Error("Network response was not ok");

      const resText = await response.text();
      let resJson;
      try {
        resJson = JSON.parse(resText);
      } catch (e) {
        throw new Error("Invalid response from script. Make sure you deployed as 'Anyone'.");
      }

      if (resJson.url) {
        const win = window.open(resJson.url, '_blank');
        if (!win) {
          alert("Your sheet is ready! But your browser blocked the popup. Please click this link: " + resJson.url);
        }
      } else if (resJson.error) {
        alert("Error from script: " + resJson.error);
      }
    } catch (err) {
      console.error(err);
      alert("Export failed: " + err.message);
    } finally {
      setExporting(false);
    }
  }

  function handleDownloadCSV() {
    if (!result?.timetable) return;
    const days = DAYS.slice(0, daysPerWeek);
    const periods = periodsPerDay;

    let csv = 'Section,Day,' + Array.from({ length: periods }, (_, i) => `Period ${i + 1}`).join(',') + '\n';

    sections.forEach(sec => {
      const secGrid = result.timetable[sec.id] || {};
      days.forEach(day => {
        const row = secGrid[day] || [];
        const cells = Array.from({ length: periods }, (_, p) => {
          const cell = row[p];
          if (!cell || cell.free || cell.subject === 'FREE') return 'FREE';
          return `${cell.subject} (${cell.teacher})`;
        });
        csv += `${sec.name || sec.id},${day},${cells.join(',')}\n`;
      });
    });

    const blob = new Blob([csv], { type: 'text/csv' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'timetable.csv';
    a.click();
    URL.revokeObjectURL(a.href);
  }

  return (
    <div className={styles.page}>
      <div className={styles.step_badge}>Step 4 of 4</div>

      <div className={styles.top_bar}>
        <SectionTitle>📅 Generated Timetable</SectionTitle>
        <div className={styles.top_actions}>
          {sections.length > 0 && (
            <select
              className={styles.filter_select}
              value={filterSectionId}
              onChange={e => setFilterSectionId(e.target.value)}
            >
              <option value="">All Sections</option>
              {sections.map(s => (
                <option key={s.id} value={s.id}>{s.name || s.id}</option>
              ))}
            </select>
          )}
          <Button
            variant="primary"
            onClick={onGenerate}
            disabled={loading || sections.length === 0}
          >
            {loading ? 'Generating...' : 'Re-generate'}
          </Button>
          {result?.timetable && (
            <>
              <Button
                variant="outline"
                onClick={handleExportToGoogleSheets}
                disabled={exporting}
              >
                {exporting ? 'Exporting...' : 'Export to Google Sheets'}
              </Button>
              <Button variant="outline" onClick={handleDownloadCSV}>Download CSV</Button>
            </>
          )}
        </div>
      </div>

      {/* Error */}
      {error && (
        <Alert type="error">
          {error}
          <div style={{ marginTop: 6, fontSize: '0.82rem' }}>
            Make sure the Java backend is running on <code>http://localhost:8080</code>
          </div>
        </Alert>
      )}

      {/* Loading */}
      {loading && (
        <div className={styles.loading_box}>
          <Spinner />
          <p>Running DSatur graph coloring algorithm…</p>
        </div>
      )}

      {/* Stats */}
      {result?.stats && !loading && (
        <div className={styles.stats_row}>
          <StatCard value={result.stats.totalSections} label="Sections" />
          <StatCard value={result.stats.totalScheduledLectures} label="Scheduled Lectures" />
          <StatCard value={result.stats.totalFreePeriods} label="Free Periods" />
          <StatCard
            value={result.stats.warningCount}
            label="Warnings"
            color={result.stats.warningCount > 0 ? '#d97706' : undefined}
          />
        </div>
      )}

      {/* Teacher Load */}
      {result?.stats?.teacherLoadMap && !loading && (
        <Card style={{ marginBottom: 0 }}>
          <FieldLabel>Teacher Weekly Load</FieldLabel>
          <div className={styles.teacher_load}>
            {Object.entries(result.stats.teacherLoadMap).map(([t, count]) => (
              <div key={t} className={styles.load_row}>
                <span className={styles.load_name}>{t}</span>
                <div className={styles.load_bar_wrap}>
                  <div
                    className={styles.load_bar}
                    style={{ width: `${Math.min(100, (count / 30) * 100)}%` }}
                  />
                </div>
                <span className={styles.load_count}>{count} lec</span>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Warnings */}
      {result?.warnings?.length > 0 && !loading && (
        <div className={styles.warnings}>
          {result.warnings.map((w, i) => (
            <Alert key={i} type="warn">⚠️ {w}</Alert>
          ))}
        </div>
      )}

      {/* Success */}
      {result?.timetable && result.warnings?.length === 0 && !loading && (
        <Alert type="success">✅ Timetable generated with zero conflicts!</Alert>
      )}

      {/* Timetables Rendering */}
      {result?.timetable && !loading && (
        <div className={styles.grid_container}>
          {(filterSectionId
            ? sections.filter(s => s.id === filterSectionId)
            : sections
          ).map(sec => (
            <Card key={sec.id} style={{ padding: 0, overflow: 'hidden' }}>
              <div className={styles.sec_card_header}>
                <div className={styles.sec_card_title}>
                  Section: {sec.name || sec.id}
                </div>
                <div className={styles.sec_card_meta}>
                  {result.timetable[sec.id] ? 'Generated Successfully' : 'No Data'}
                </div>
              </div>
              <div style={{ padding: '20px' }}>
                <TimetableGrid
                  timetable={result.timetable}
                  sectionId={sec.id}
                  periodsPerDay={periodsPerDay}
                  daysPerWeek={daysPerWeek}
                />
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Empty state */}
      {!result && !loading && !error && (
        <div className={styles.empty}>
          <div className={styles.empty_icon}>⚡</div>
          <div className={styles.empty_title}>Ready to schedule</div>
          <p className={styles.empty_sub}>
            Click Generate Timetable to run the DSatur algorithm on your sections.
          </p>
          <Button
            variant="primary"
            size="lg"
            onClick={onGenerate}
            disabled={sections.length === 0}
          >
            ⚡ Generate Timetable
          </Button>
          {sections.length === 0 && (
            <p className={styles.empty_hint}>Add sections first from the Setup page.</p>
          )}
        </div>
      )}

      <StepFooter
        onPrev={onPrev}
        prevLabel="← Back to Review"
        showNext={false}
      />
    </div>
  );
}

