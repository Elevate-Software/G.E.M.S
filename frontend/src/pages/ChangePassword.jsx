import { createSignal, Show } from 'solid-js';
import { useNavigate } from '@solidjs/router';
import { changePassword } from '../lib/api';
import { getUser, setUser } from '../lib/auth';
import { validatePassword } from '../lib/utils';
import { useToast } from '../components/ToastProvider';

export default function ChangePassword() {
  const navigate = useNavigate();
  const showToast = useToast();

  const [currentPassword, setCurrentPassword] = createSignal('');
  const [newPassword, setNewPassword] = createSignal('');
  const [confirmPassword, setConfirmPassword] = createSignal('');
  const [loading, setLoading] = createSignal(false);
  const [errors, setErrors] = createSignal({});
  const [showPassword, setShowPassword] = createSignal(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    let valid = true;
    let newErrors = {};

    if (!currentPassword()) {
      newErrors.currentPassword = 'Current password is required';
      valid = false;
    }

    if (!newPassword()) {
      newErrors.newPassword = 'New password is required';
      valid = false;
    } else {
      const validation = validatePassword(newPassword());
      if (!validation.isValid) {
        newErrors.newPassword = validation.errors[0];
        valid = false;
      }
    }

    if (!confirmPassword()) {
      newErrors.confirmPassword = 'Please confirm your new password';
      valid = false;
    } else if (newPassword() !== confirmPassword()) {
      newErrors.confirmPassword = 'Passwords do not match';
      valid = false;
    }

    setErrors(newErrors);
    if (!valid) return;

    setLoading(true);

    try {
      await changePassword({
        currentPassword: currentPassword(),
        newPassword: newPassword()
      });

      // Update user object to clear passwordChangeRequired flag
      const user = getUser();
      if (user) {
        setUser({ ...user, passwordChangeRequired: false });
      }

      showToast('Password changed successfully', 'success');
      navigate('/dashboard');
    } catch (err) {
      showToast(err.message || 'Failed to change password', 'error');
    } finally {
      setLoading(false);
    }
  };

  const passwordStrength = () => {
    const pwd = newPassword();
    if (!pwd) return { score: 0, label: '' };
    
    const validation = validatePassword(pwd);
    const passedChecks = 5 - validation.errors.length;
    
    if (passedChecks <= 1) return { score: 1, label: 'Very Weak' };
    if (passedChecks === 2) return { score: 2, label: 'Weak' };
    if (passedChecks === 3) return { score: 3, label: 'Fair' };
    if (passedChecks === 4) return { score: 4, label: 'Strong' };
    return { score: 5, label: 'Very Strong' };
  };

  const strengthClass = () => {
    const score = passwordStrength().score;
    if (score <= 1) return 'strength-weak';
    if (score === 2) return 'strength-fair';
    if (score === 3) return 'strength-good';
    if (score === 4) return 'strength-strong';
    return 'strength-excellent';
  };

  return (
    <div class="auth-layout">
      <div class="auth-branding">
        <div class="auth-branding-content">
          <div class="auth-logo">🔐</div>
          <h1 class="gradient-text">Change Your Password</h1>
          <p>For your security, you must change your password before continuing. Please create a strong password that meets our security requirements.</p>
          <div class="auth-features">
            <div class="auth-feature">
              <div class="auth-feature-icon">🔒</div>
              <span>Argon2id encryption for secure password storage</span>
            </div>
            <div class="auth-feature">
              <div class="auth-feature-icon">🛡️</div>
              <span>Enhanced security with complex password requirements</span>
            </div>
            <div class="auth-feature">
              <div class="auth-feature-icon">✅</div>
              <span>One-time setup for your account protection</span>
            </div>
          </div>
        </div>
      </div>

      <div class="auth-form-panel">
        <div class="auth-form-wrapper">
          <h2 class="auth-form-title">Set New Password</h2>
          <p class="auth-form-subtitle">Create a secure password for your account</p>

          <form class="auth-form" onSubmit={handleSubmit} noValidate>
            <div class="form-group">
              <label class="form-label" for="current-password">Current Password</label>
              <div class="password-input-wrapper">
                <input
                  type={showPassword() ? 'text' : 'password'}
                  id="current-password"
                  class={`form-input ${errors().currentPassword ? 'error' : ''}`}
                  placeholder="Enter your current password"
                  value={currentPassword()}
                  onInput={(e) => setCurrentPassword(e.target.value)}
                  disabled={loading()}
                />
                <button 
                  type="button" 
                  class="password-toggle"
                  onClick={() => setShowPassword(!showPassword())}
                >
                  {showPassword() ? '🙈' : '👁️'}
                </button>
              </div>
              <span class="form-error">{errors().currentPassword}</span>
            </div>

            <div class="form-group">
              <label class="form-label" for="new-password">New Password</label>
              <div class="password-input-wrapper">
                <input
                  type={showPassword() ? 'text' : 'password'}
                  id="new-password"
                  class={`form-input ${errors().newPassword ? 'error' : ''}`}
                  placeholder="Enter your new password"
                  value={newPassword()}
                  onInput={(e) => setNewPassword(e.target.value)}
                  disabled={loading()}
                />
                <button 
                  type="button" 
                  class="password-toggle"
                  onClick={() => setShowPassword(!showPassword())}
                >
                  {showPassword() ? '🙈' : '👁️'}
                </button>
              </div>
              <span class="form-error">{errors().newPassword}</span>
              
              <Show when={newPassword()}>
                <div class="password-strength">
                  <div class="strength-bar">
                    <div class={`strength-fill ${strengthClass()}`} style={{ width: `${(passwordStrength().score / 5) * 100}%` }}></div>
                  </div>
                  <span class="strength-label">{passwordStrength().label}</span>
                </div>
                
                <div class="password-requirements">
                  <div class={`requirement ${/[A-Z]/.test(newPassword()) ? 'met' : ''}`}>
                    <span class="requirement-icon">{/[A-Z]/.test(newPassword()) ? '✓' : '○'}</span>
                    <span>Uppercase letter</span>
                  </div>
                  <div class={`requirement ${/[a-z]/.test(newPassword()) ? 'met' : ''}`}>
                    <span class="requirement-icon">{/[a-z]/.test(newPassword()) ? '✓' : '○'}</span>
                    <span>Lowercase letter</span>
                  </div>
                  <div class={`requirement ${/[0-9]/.test(newPassword()) ? 'met' : ''}`}>
                    <span class="requirement-icon">{/[0-9]/.test(newPassword()) ? '✓' : '○'}</span>
                    <span>Number</span>
                  </div>
                  <div class={`requirement ${/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(newPassword()) ? 'met' : ''}`}>
                    <span class="requirement-icon">{/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(newPassword()) ? '✓' : '○'}</span>
                    <span>Special character</span>
                  </div>
                  <div class={`requirement ${newPassword().length >= 8 ? 'met' : ''}`}>
                    <span class="requirement-icon">{newPassword().length >= 8 ? '✓' : '○'}</span>
                    <span>At least 8 characters</span>
                  </div>
                </div>
              </Show>
            </div>

            <div class="form-group">
              <label class="form-label" for="confirm-password">Confirm New Password</label>
              <div class="password-input-wrapper">
                <input
                  type={showPassword() ? 'text' : 'password'}
                  id="confirm-password"
                  class={`form-input ${errors().confirmPassword ? 'error' : ''}`}
                  placeholder="Confirm your new password"
                  value={confirmPassword()}
                  onInput={(e) => setConfirmPassword(e.target.value)}
                  disabled={loading()}
                />
                <button 
                  type="button" 
                  class="password-toggle"
                  onClick={() => setShowPassword(!showPassword())}
                >
                  {showPassword() ? '🙈' : '👁️'}
                </button>
              </div>
              <span class="form-error">{errors().confirmPassword}</span>
            </div>

            <button type="submit" class="btn btn-primary btn-lg" disabled={loading()}>
              {loading() ? <><span class="spinner"></span> Changing password…</> : 'Change Password'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
