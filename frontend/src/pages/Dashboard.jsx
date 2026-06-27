import { createSignal, createEffect, Show } from 'solid-js';
import { getUser, hasRole } from '../lib/auth';
import { getMe } from '../lib/api';

export default function Dashboard() {
  const user = getUser();
  const [profile, setProfile] = createSignal(user);
  
  createEffect(() => {
    getMe().then(data => {
      if (data) setProfile(data);
    }).catch(() => {});
  });

  return (
    <div class="page-container page-enter">
      <header class="page-header">
        <h1 class="page-title">Welcome, {profile()?.fullName || 'User'}</h1>
        <p class="page-subtitle">Here's your campus access overview.</p>
      </header>

      <div class="stats-grid">
        <Show when={hasRole('ADMIN', 'SECURITY')}>
          <div class="card stat-card stat-card-teal">
            <div class="stat-icon stat-icon-teal">📊</div>
            <div class="stat-value">1,248</div>
            <div class="stat-label">Total Scans Today</div>
          </div>
          
          <div class="card stat-card stat-card-violet">
            <div class="stat-icon stat-icon-violet">🚪</div>
            <div class="stat-value">4</div>
            <div class="stat-label">Active Gates</div>
          </div>
          
          <div class="card stat-card stat-card-warm">
            <div class="stat-icon stat-icon-warm">⚠️</div>
            <div class="stat-value">12</div>
            <div class="stat-label">Access Denied</div>
          </div>
        </Show>

        <Show when={hasRole('STUDENT')}>
          <div class="card stat-card stat-card-teal">
            <div class="stat-icon stat-icon-teal">✅</div>
            <div class="stat-value">{profile()?.accountStatus || 'ACTIVE'}</div>
            <div class="stat-label">Account Status</div>
          </div>
        </Show>
      </div>

      <div class="dashboard-grid">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">Recent Activity</h3>
          </div>
          <div class="card-body">
            <div class="activity-item">
              <div class="activity-dot activity-dot-success"></div>
              <div class="activity-text">Main Gate Entry Granted</div>
              <div class="activity-time">2 mins ago</div>
            </div>
            <div class="activity-item">
              <div class="activity-dot activity-dot-danger"></div>
              <div class="activity-text">Library Gate Exit Denied (Expired Token)</div>
              <div class="activity-time">15 mins ago</div>
            </div>
            <div class="activity-item">
              <div class="activity-dot activity-dot-success"></div>
              <div class="activity-text">North Gate Entry Granted</div>
              <div class="activity-time">1 hr ago</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
