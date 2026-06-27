import { createSignal, createEffect, Show } from 'solid-js';
import { getUser } from '../lib/auth';
import { getMe, generateCredential, getMyCredential } from '../lib/api';
import { formatDate, getInitials, roleLabel, statusBadgeClass } from '../lib/utils';
import { useToast } from '../components/ToastProvider';

export default function Profile() {
  const [profile, setProfile] = createSignal(getUser());
  const [credential, setCredential] = createSignal(null);
  const [loading, setLoading] = createSignal(false);
  const showToast = useToast();
  
  createEffect(() => {
    getMe().then(data => {
      if (data) setProfile(data);
    }).catch(() => {});
  });

  const loadCredential = async () => {
    try {
      const data = await getMyCredential();
      setCredential(data);
    } catch (err) {
      // No credential exists yet
      setCredential(null);
    }
  };

  const handleGenerateCredential = async () => {
    setLoading(true);
    try {
      const data = await generateCredential();
      setCredential(data);
      showToast('Credential generated successfully', 'success');
    } catch (err) {
      showToast(err.message || 'Failed to generate credential', 'error');
    } finally {
      setLoading(false);
    }
  };

  // Load credential on mount
  createEffect(() => {
    if (profile()) {
      loadCredential();
    }
  });

  return (
    <div class="page-container page-enter">
      <header class="page-header">
        <h1 class="page-title">My Profile</h1>
        <p class="page-subtitle">Manage your personal information and credentials.</p>
      </header>

      <Show when={profile()}>
        <div class="card" style={{ "margin-bottom": "var(--sp-6)" }}>
          <div class="profile-hero">
            <div class="profile-avatar">
              {getInitials(profile()?.fullName)}
            </div>
            <div class="profile-info">
              <h2>{profile()?.fullName}</h2>
              <div class="profile-info-meta">
                <span><span style={{ color: 'var(--accent-teal)' }}>■</span> {roleLabel(profile()?.role)}</span>
                <span>•</span>
                <span>{profile()?.email}</span>
                <Show when={profile()?.accountStatus}>
                  <span>•</span>
                  <span class={`badge badge-dot ${statusBadgeClass(profile()?.accountStatus)}`}>
                    {profile()?.accountStatus}
                  </span>
                </Show>
              </div>
            </div>
          </div>

          <div class="divider"></div>

          <div class="profile-details">
            <div class="profile-detail-item">
              <div class="profile-detail-label">Full Name</div>
              <div class="profile-detail-value">{profile()?.fullName}</div>
            </div>
            
            <div class="profile-detail-item">
              <div class="profile-detail-label">Email Address</div>
              <div class="profile-detail-value">{profile()?.email}</div>
            </div>
            
            <div class="profile-detail-item">
              <div class="profile-detail-label">Phone Number</div>
              <div class="profile-detail-value">{profile()?.phoneNumber || 'Not provided'}</div>
            </div>
            
            <Show when={profile()?.role === 'STUDENT'}>
              <div class="profile-detail-item">
                <div class="profile-detail-label">Student ID</div>
                <div class="profile-detail-value" style={{ "font-family": "var(--font-mono)" }}>
                  {profile()?.studentNumber || '—'}
                </div>
              </div>
            </Show>
            
            <div class="profile-detail-item">
              <div class="profile-detail-label">Member Since</div>
              <div class="profile-detail-value">{formatDate(profile()?.createdAt)}</div>
            </div>
          </div>
        </div>

        <Show when={profile()?.role === 'STUDENT'}>
          <div class="card">
            <div class="card-header">
              <h3 class="card-title">Digital Credential</h3>
            </div>
            <div class="card-body">
              <Show when={credential()} fallback={
                <div style={{ "text-align": "center", padding: "var(--sp-8)" }}>
                  <div style={{ "font-size": "3rem", "margin-bottom": "var(--sp-3)" }}>🔑</div>
                  <div style={{ "font-weight": "var(--fw-semibold)", color: "var(--text-primary)", "margin-bottom": "var(--sp-2)" }}>No Active Credential</div>
                  <div style={{ "font-size": "var(--fs-sm)", color: "var(--text-secondary)", "margin-bottom": "var(--sp-4)" }}>Generate your digital access credential to scan at campus gates.</div>
                  <button class="btn btn-primary" onClick={handleGenerateCredential} disabled={loading()}>
                    {loading() ? <><span class="spinner"></span> Generating…</> : 'Generate Credential'}
                  </button>
                </div>
              }>
                <div style={{ "display": "flex", "flex-direction": "column", gap: "var(--sp-4)" }}>
                  <div style={{ "display": "flex", "align-items": "center", gap: "var(--sp-4)", "background": "var(--bg-deep)", padding: "var(--sp-5)", "border-radius": "var(--radius-lg)", border: "1px solid var(--border-subtle)" }}>
                    <div style={{ "font-size": "3rem" }}>📱</div>
                    <div style={{ flex: 1 }}>
                      <div style={{ "font-weight": "var(--fw-semibold)", color: "var(--text-primary)", "margin-bottom": "var(--sp-1)" }}>Active Access Token</div>
                      <div style={{ "font-size": "var(--fs-sm)", color: "var(--text-secondary)", "margin-bottom": "var(--sp-2)" }}>
                        Issued: {formatDate(credential()?.issueDate)} • Expires: {formatDate(credential()?.expiryDate)}
                      </div>
                      <div style={{ "font-family": "var(--font-mono)", "font-size": "var(--fs-xs)", color: "var(--text-tertiary)", "word-break": "break-all" }}>
                        {credential()?.authToken}
                      </div>
                    </div>
                    <span class="badge badge-success">Valid</span>
                  </div>
                  <button class="btn btn-secondary" onClick={handleGenerateCredential} disabled={loading()}>
                    {loading() ? <><span class="spinner"></span> Regenerating…</> : '🔄 Regenerate Credential'}
                  </button>
                </div>
              </Show>
            </div>
          </div>
        </Show>
      </Show>
    </div>
  );
}
