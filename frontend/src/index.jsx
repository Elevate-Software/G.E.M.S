import { render } from 'solid-js/web';
import { Router, Route } from '@solidjs/router';
import { ToastProvider } from './components/ToastProvider';

import './styles/index.css';
import './styles/components.css';
import './styles/pages.css';

import App from './App';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Scan from './pages/Scan';
import Profile from './pages/Profile';
import Users from './pages/Users';
import ChangePassword from './pages/ChangePassword';
import NotFound from './pages/NotFound';

const root = document.getElementById('root');

if (import.meta.env.DEV && !(root instanceof HTMLElement)) {
  throw new Error('Root element not found.');
}

render(
  () => (
    <ToastProvider>
      <Router>
        <Route path="/login" component={Login} />
        <Route path="/register" component={Register} />
        <Route path="/change-password" component={ChangePassword} />
        <Route path="/" component={App}>
          <Route path="/dashboard" component={Dashboard} />
          <Route path="/scan" component={Scan} />
          <Route path="/users" component={Users} />
          <Route path="/profile" component={Profile} />
          <Route path="*404" component={NotFound} />
        </Route>
      </Router>
    </ToastProvider>
  ),
  root
);
