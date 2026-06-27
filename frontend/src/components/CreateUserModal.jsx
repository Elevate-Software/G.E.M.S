import { createSignal, Show } from 'solid-js';
import { createUser } from '../lib/api';
import { generatePassword, validatePassword } from '../lib/utils';
import { useToast } from './ToastProvider';

export default function CreateUserModal(props) {
  const showToast = useToast();

  const [role, setRole] = createSignal('STUDENT');
  const [fullName, setFullName] = createSignal('');
  const [email, setEmail] = createSignal('');
  const [studentNumber, setStudentNumber] = createSignal('');
  const [phoneNumber, setPhoneNumber] = createSignal('');
  const [generatedPassword, setGeneratedPassword] = createSignal('');
  const [loading, setLoading] = createSignal(false);
  const [errors, setErrors] = createSignal({});
  const [showGeneratedPassword, setShowGeneratedPassword] = createSignal(false);

  const generateOneTimePassword = () => {
    const password = generatePassword(12);
    setGeneratedPassword(password);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    let valid = true;
    let newErrors = {};

    if (!fullName().trim()) {
      newErrors.fullName = 'Full name is required';
      valid = false;
    }
    if (!email().trim()) {
      newErrors.email = 'Email is required';
      valid = false;
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email().trim())) {
      newErrors.email = 'Enter a valid email address';
      valid = false;
    }
    if (!generatedPassword()) {
      newErrors.password = 'Please generate a one-time password';
      valid = false;
    }
    if (role() === 'STUDENT' && !studentNumber().trim()) {
      newErrors.studentNumber = 'Student number is required';
      valid = false;
    }

    setErrors(newErrors);
    if (!valid) return;

    setLoading(true);

    try {
      await createUser({
        fullName: fullName().trim(),
        email: email().trim(),
        password: generatedPassword(),
        role: role(),
        studentNumber: studentNumber().trim() || null,
        phoneNumber: phoneNumber().trim() || null,
        passwordChangeRequired: true
      });

      showToast('User created successfully with one-time password', 'success');
      
      // Reset form
      setFullName('');
      setEmail('');
      setStudentNumber('');
      setPhoneNumber('');
      setGeneratedPassword('');
      setErrors({});
      
      if (props.onSuccess) props.onSuccess();
      props.onClose();
    } catch (err) {
      showToast(err.message || 'Failed to create user', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setFullName('');
    setEmail('');
    setStudentNumber('');
    setPhoneNumber('');
    setGeneratedPassword('');
    setErrors({});
    props.onClose();
  };

  return (
    <Show when={props.isOpen}>
      <div class="modal-overlay" onClick={handleClose}>
        <div class="modal-content" onClick={(e) => e.stopPropagation()}>
          <div class="modal-header">
            <h3 class="modal-title">Create New User</h3>
            <button class="modal-close" onClick={handleClose}>×</button>
          </div>

          <form class="modal-form" onSubmit={handleSubmit} noValidate>
            <div class="form-group">
              <label class="form-label">Role</label>
              <div class="role-selector">
                <div 
                  class={`role-card ${role() === 'STUDENT' ? 'selected' : ''}`}
                  onClick={() => setRole('STUDENT')}
                >
                  <span class="role-card-icon">🎓</span>
                  <span class="role-card-name">Student</span>
                </div>
                <div 
                  class={`role-card ${role() === 'SECURITY' ? 'selected' : ''}`}
                  onClick={() => setRole('SECURITY')}
                >
                  <span class="role-card-icon">🛡️</span>
                  <span class="role-card-name">Security</span>
                </div>
                <div 
                  class={`role-card ${role() === 'ADMIN' ? 'selected' : ''}`}
                  onClick={() => setRole('ADMIN')}
                >
                  <span class="role-card-icon">⚙️</span>
                  <span class="role-card-name">Admin</span>
                </div>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label" for="create-name">Full Name</label>
              <input
                type="text"
                id="create-name"
                class={`form-input ${errors().fullName ? 'error' : ''}`}
                placeholder="User's full name"
                value={fullName()}
                onInput={(e) => setFullName(e.target.value)}
                disabled={loading()}
              />
              <span class="form-error">{errors().fullName}</span>
            </div>

            <div class="form-group">
              <label class="form-label" for="create-email">Email Address</label>
              <input
                type="email"
                id="create-email"
                class={`form-input ${errors().email ? 'error' : ''}`}
                placeholder="user@university.edu"
                value={email()}
                onInput={(e) => setEmail(e.target.value)}
                disabled={loading()}
              />
              <span class="form-error">{errors().email}</span>
            </div>

            <Show when={role() === 'STUDENT'}>
              <div class="form-group">
                <label class="form-label" for="create-student-number">Student Number</label>
                <input
                  type="text"
                  id="create-student-number"
                  class={`form-input ${errors().studentNumber ? 'error' : ''}`}
                  placeholder="e.g. S2024001"
                  value={studentNumber()}
                  onInput={(e) => setStudentNumber(e.target.value)}
                  disabled={loading()}
                />
                <span class="form-error">{errors().studentNumber}</span>
              </div>
            </Show>

            <div class="form-group">
              <label class="form-label" for="create-phone">Phone Number <span style={{ color: 'var(--text-tertiary)', "text-transform": 'none', "font-weight": 'var(--fw-regular)' }}>(optional)</span></label>
              <input
                type="tel"
                id="create-phone"
                class="form-input"
                placeholder="+251 9XX XXX XXX"
                value={phoneNumber()}
                onInput={(e) => setPhoneNumber(e.target.value)}
                disabled={loading()}
              />
            </div>

            <div class="form-group">
              <label class="form-label">One-Time Password</label>
              <div class="otp-section">
                <div class="password-display">
                  <input
                    type={showGeneratedPassword() ? 'text' : 'password'}
                    class={`form-input ${errors().password ? 'error' : ''}`}
                    value={generatedPassword()}
                    readOnly
                    placeholder="Generate a secure one-time password"
                  />
                  <button 
                    type="button"
                    class="btn btn-ghost btn-icon"
                    onClick={() => setShowGeneratedPassword(!showGeneratedPassword())}
                  >
                    {showGeneratedPassword() ? '🙈' : '👁️'}
                  </button>
                </div>
                <button 
                  type="button"
                  class="btn btn-secondary"
                  onClick={generateOneTimePassword}
                  disabled={loading()}
                >
                  🔄 Generate OTP
                </button>
              </div>
              <span class="form-error">{errors().password}</span>
              <Show when={generatedPassword()}>
                <div class="otp-info">
                  <span class="otp-info-icon">ℹ️</span>
                  <span>This password meets Argon2id security requirements. User must change it on first login.</span>
                </div>
              </Show>
            </div>

            <div class="modal-actions">
              <button 
                type="button" 
                class="btn btn-secondary" 
                onClick={handleClose}
                disabled={loading()}
              >
                Cancel
              </button>
              <button 
                type="submit" 
                class="btn btn-primary" 
                disabled={loading() || !generatedPassword()}
              >
                {loading() ? <><span class="spinner"></span> Creating…</> : 'Create User'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Show>
  );
}
