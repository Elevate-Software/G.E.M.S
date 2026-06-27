import { A } from '@solidjs/router';

export default function NotFound() {
  return (
    <div class="not-found-page page-enter">
      <div class="not-found-code">404</div>
      <h1 class="not-found-title">Page Not Found</h1>
      <p class="not-found-text">
        The page you are looking for doesn't exist or you don't have permission to access it.
      </p>
      <A href="/dashboard" class="btn btn-primary btn-lg">
        Return to Dashboard
      </A>
    </div>
  );
}
