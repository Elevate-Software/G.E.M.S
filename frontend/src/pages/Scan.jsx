import { createSignal, Show, For, createEffect } from 'solid-js';
import { scanCredential, getGates } from '../lib/api';
import { useToast } from '../components/ToastProvider';

export default function Scan() {
  const showToast = useToast();
  const [token, setToken] = createSignal('');
  const [gateId, setGateId] = createSignal(1);
  const [direction, setDirection] = createSignal('ENTRY');
  const [gates, setGates] = createSignal([]);
  
  const [loading, setLoading] = createSignal(false);
  const [gatesLoading, setGatesLoading] = createSignal(false);
  const [result, setResult] = createSignal(null); // { result: 'GRANTED'|'DENIED', message, user }

  createEffect(() => {
    loadGates();
  });

  const loadGates = async () => {
    setGatesLoading(true);
    try {
      const data = await getGates();
      setGates(data);
      if (data.length > 0) {
        setGateId(data[0].id);
      }
    } catch (err) {
      showToast('Failed to load gates', 'error');
    } finally {
      setGatesLoading(false);
    }
  };

  const handleScan = async (e) => {
    e.preventDefault();
    if (!token().trim()) return;

    setLoading(true);
    setResult(null);

    try {
      const res = await scanCredential(token(), gateId(), direction());
      setResult(res);
      
      if (res.result === 'GRANTED') {
        showToast('Access Granted', 'success');
      } else {
        showToast(res.message || 'Access Denied', 'error');
      }
      
      setToken(''); // clear for next scan
    } catch (err) {
      showToast(err.message || 'Scan failed', 'error');
      setResult({ result: 'DENIED', message: err.message || 'System Error' });
    } finally {
      setLoading(false);
      
      // Auto-focus the input again
      const input = document.getElementById('scan-token');
      if (input) input.focus();
    }
  };

  return (
    <div class="page-container page-enter scan-page">
      <header class="page-header" style={{ "text-align": "center" }}>
        <h1 class="page-title">Gate Scanner</h1>
        <p class="page-subtitle">Scan digital credentials for instant verification.</p>
      </header>

      <div class="card scan-card">
        <form onSubmit={handleScan}>
          <div class="scan-options">
            <div class="form-group">
              <label class="form-label" for="scan-gate">Gate</label>
              <select 
                id="scan-gate" 
                class="form-select" 
                value={gateId()} 
                onChange={(e) => setGateId(parseInt(e.target.value, 10))}
                disabled={gatesLoading()}
              >
                <Show when={gatesLoading()}>
                  <option value="">Loading gates...</option>
                </Show>
                <Show when={!gatesLoading() && gates().length === 0}>
                  <option value="">No gates available</option>
                </Show>
                <For each={gates()}>
                  {(gate) => (
                    <option value={gate.id}>{gate.gateName}</option>
                  )}
                </For>
              </select>
            </div>
            
            <div class="form-group">
              <label class="form-label">Direction</label>
              <div class="toggle-group" style={{ width: '100%', display: 'flex' }}>
                <button 
                  type="button" 
                  class={`toggle-option ${direction() === 'ENTRY' ? 'active' : ''}`}
                  style={{ flex: 1 }}
                  onClick={() => setDirection('ENTRY')}
                >
                  ENTRY
                </button>
                <button 
                  type="button" 
                  class={`toggle-option ${direction() === 'EXIT' ? 'active' : ''}`}
                  style={{ flex: 1 }}
                  onClick={() => setDirection('EXIT')}
                >
                  EXIT
                </button>
              </div>
            </div>
          </div>

          <div class="scan-input-wrapper">
            <input
              type="text"
              id="scan-token"
              class="scan-input"
              placeholder="Waiting for scan..."
              value={token()}
              onInput={(e) => setToken(e.target.value)}
              autocomplete="off"
              autofocus
              disabled={loading()}
            />
          </div>
          
          <button type="submit" class="btn btn-primary btn-lg" style={{ width: '100%' }} disabled={loading() || !token().trim()}>
            {loading() ? <><span class="spinner"></span> Processing…</> : 'Manual Scan'}
          </button>
        </form>

        <Show when={result()}>
          <div class={`scan-result scan-result-${result().result.toLowerCase()}`}>
            <div class="scan-result-icon">
              {result().result === 'GRANTED' ? '✅' : '⛔'}
            </div>
            <div class="scan-result-status">
              ACCESS {result().result}
            </div>
            <div class="scan-result-message">
              {result().message}
            </div>
            
            <Show when={result().user}>
              <div class="scan-result-user">
                <div class="avatar" style={{ background: 'var(--gradient-surface)' }}>
                  {result().user.fullName.charAt(0)}
                </div>
                <div class="scan-result-user-info">
                  <div class="scan-result-user-name">{result().user.fullName}</div>
                  <div class="scan-result-user-id">{result().user.studentNumber || result().user.email}</div>
                </div>
              </div>
            </Show>
          </div>
        </Show>
      </div>
    </div>
  );
}
