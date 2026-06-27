import { createSignal } from 'solid-js';
import { useNavigate, A } from '@solidjs/router';
import { login as apiLogin, getMe } from '../lib/api';
import { setToken, setUser } from '../lib/auth';
import { useToast } from '../components/ToastProvider';

export default function Login() {
  const navigate = useNavigate();
  const showToast = useToast();
  
  const [email, setEmail] = createSignal('');
  const [password, setPassword] = createSignal('');
  const [loading, setLoading] = createSignal(false);
  const [errors, setErrors] = createSignal({ email: '', password: '' });

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    let valid = true;
    let newErrors = { email: '', password: '' };

    if (!email()) {
      newErrors.email = 'Email is required';
      valid = false;
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email())) {
      newErrors.email = 'Enter a valid email address';
      valid = false;
    }
    
    if (!password()) {
      newErrors.password = 'Password is required';
      valid = false;
    }

    setErrors(newErrors);
    if (!valid) return;

    setLoading(true);
    
    try {
      const { accessToken } = await apiLogin(email(), password());
      setToken(accessToken);
      
      const user = await getMe();
      setUser(user);
      
      showToast(`Welcome back, ${user.fullName}!`, 'success');
      
      // Check if user needs to change password (first-time login)
      if (user.passwordChangeRequired) {
        navigate('/change-password');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      showToast(err.message || 'Login failed', 'error');
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
          <p>Gate Entrance Management System — Secure, smart campus access control for the modern university.</p>
          <div class="auth-features">
            <div class="auth-feature">
              <div class="auth-feature-icon">🔐</div>
              <span>Real-time credential scanning & verification</span>
            </div>
            <div class="auth-feature">
              <div class="auth-feature-icon">📊</div>
              <span>Comprehensive entry & exit logging</span>
            </div>
            <div class="auth-feature">
              <div class="auth-feature-icon">🛡️</div>
              <span>Role-based access with instant alerts</span>
            </div>
            <div class="auth-feature">
              <div class="auth-feature-icon">⚡</div>
              <span>Lightning-fast gate processing</span>
            </div>
          </div>
        </div>
      </div>

      <div class="auth-form-panel">
        <div class="auth-form-wrapper">
          <h2 class="auth-form-title">Welcome back</h2>
          <p class="auth-form-subtitle">Sign in to your account to continue</p>

          <form class="auth-form" onSubmit={handleSubmit} noValidate>
            <div class="form-group">
              <label class="form-label" for="login-email">Email Address</label>
              <input
                type="email"
                id="login-email"
                class={`form-input ${errors().email ? 'error' : ''}`}
                placeholder="you@university.edu"
                value={email()}
                onInput={(e) => setEmail(e.target.value)}
                disabled={loading()}
              />
              <span class="form-error">{errors().email}</span>
            </div>

            <div class="form-group">
              <label class="form-label" for="login-password">Password</label>
              <input
                type="password"
                id="login-password"
                class={`form-input ${errors().password ? 'error' : ''}`}
                placeholder="Enter your password"
                value={password()}
                onInput={(e) => setPassword(e.target.value)}
                disabled={loading()}
              />
              <span class="form-error">{errors().password}</span>
            </div>

            <button type="submit" class="btn btn-primary btn-lg" disabled={loading()}>
              {loading() ? <><span class="spinner"></span> Signing in…</> : 'Sign In'}
            </button>
          </form>

          <div class="auth-footer">
            Don't have an account? <A href="/register">Create one now</A>
          </div>
        </div>
      </div>
    </div>
  );
}
