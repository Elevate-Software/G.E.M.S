import { createSignal, Show } from 'solid-js';
import { useNavigate, A } from '@solidjs/router';
import { register as apiRegister, login as apiLogin, getMe } from '../lib/api';
import { setToken, setUser } from '../lib/auth';
import { useToast } from '../components/ToastProvider';

export default function Register() {
  const navigate = useNavigate();
  const showToast = useToast();

  const [role, setRole] = createSignal('STUDENT');
  const [fullName, setFullName] = createSignal('');
  const [email, setEmail] = createSignal('');
  const [password, setPassword] = createSignal('');
  const [studentNumber, setStudentNumber] = createSignal('');
  const [phoneNumber, setPhoneNumber] = createSignal('');
  
  const [loading, setLoading] = createSignal(false);
  const [errors, setErrors] = createSignal({});

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
    if (!password()) {
      newErrors.password = 'Password is required';
      valid = false;
    } else if (password().length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
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
      await apiRegister({
        fullName: fullName().trim(),
        email: email().trim(),
        password: password(),
        role: role(),
        studentNumber: studentNumber().trim() || null,
        phoneNumber: phoneNumber().trim() || null
      });

      // Auto-login
      const { accessToken } = await apiLogin(email().trim(), password());
      setToken(accessToken);
      
      const user = await getMe();
      setUser(user);

      showToast(`Welcome, ${user.fullName}! Account created.`, 'success');
      navigate('/dashboard');
    } catch (err) {
      showToast(err.message || 'Registration failed', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div class="auth-layout">
      <div class="auth-branding">
        <div class="auth-branding-content">
          <div class="auth-logo">💎</div>
          <h1 class="gradient-text">G.E.M.S</h1>
          <p>Create your campus access account and join the secure gate management network.</p>
          <div class="auth-features">
            <div class="auth-feature">
              <div class="auth-feature-icon">🎓</div>
              <span>Students get instant digital credentials</span>
            </div>
            <div class="auth-feature">
              <div class="auth-feature-icon">🔒</div>
              <span>Secure, encrypted access tokens</span>
            </div>
            <div class="auth-feature">
              <div class="auth-feature-icon">📱</div>
              <span>Scan in seconds at any campus gate</span>
            </div>
          </div>
        </div>
      </div>

      <div class="auth-form-panel">
        <div class="auth-form-wrapper">
          <h2 class="auth-form-title">Create Account</h2>
          <p class="auth-form-subtitle">Fill in your details to get started</p>

          <form class="auth-form" onSubmit={handleSubmit} noValidate>
            <div class="form-group">
              <label class="form-label">I am a</label>
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
              <label class="form-label" for="register-name">Full Name</label>
              <input
                type="text"
                id="register-name"
                class={`form-input ${errors().fullName ? 'error' : ''}`}
                placeholder="Your full name"
                value={fullName()}
                onInput={(e) => setFullName(e.target.value)}
                disabled={loading()}
              />
              <span class="form-error">{errors().fullName}</span>
            </div>

            <div class="form-group">
              <label class="form-label" for="register-email">Email Address</label>
              <input
                type="email"
                id="register-email"
                class={`form-input ${errors().email ? 'error' : ''}`}
                placeholder="you@university.edu"
                value={email()}
                onInput={(e) => setEmail(e.target.value)}
                disabled={loading()}
              />
              <span class="form-error">{errors().email}</span>
            </div>

            <div class="form-group">
              <label class="form-label" for="register-password">Password</label>
              <input
                type="password"
                id="register-password"
                class={`form-input ${errors().password ? 'error' : ''}`}
                placeholder="Min. 6 characters"
                value={password()}
                onInput={(e) => setPassword(e.target.value)}
                disabled={loading()}
              />
              <span class="form-error">{errors().password}</span>
            </div>

            <Show when={role() === 'STUDENT'}>
              <div class="form-group">
                <label class="form-label" for="register-student-number">Student Number</label>
                <input
                  type="text"
                  id="register-student-number"
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
              <label class="form-label" for="register-phone">Phone Number <span style={{ color: 'var(--text-tertiary)', "text-transform": 'none', "font-weight": 'var(--fw-regular)' }}>(optional)</span></label>
              <input
                type="tel"
                id="register-phone"
                class="form-input"
                placeholder="+251 9XX XXX XXX"
                value={phoneNumber()}
                onInput={(e) => setPhoneNumber(e.target.value)}
                disabled={loading()}
              />
            </div>

            <button type="submit" class="btn btn-primary btn-lg" disabled={loading()}>
              {loading() ? <><span class="spinner"></span> Creating account…</> : 'Create Account'}
            </button>
          </form>

          <div class="auth-footer">
            Already have an account? <A href="/login">Sign in</A>
          </div>
        </div>
      </div>
    </div>
  );
}
