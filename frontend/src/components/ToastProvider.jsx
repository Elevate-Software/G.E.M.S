import { createContext, useContext, createSignal, For } from 'solid-js';

const ToastContext = createContext();

export function ToastProvider(props) {
  const [toasts, setToasts] = createSignal([]);
  let toastId = 0;

  const showToast = (message, type = 'info', duration = 4000) => {
    const id = ++toastId;
    setToasts(prev => [...prev, { id, message, type, removing: false }]);
    
    if (duration > 0) {
      setTimeout(() => dismissToast(id), duration);
    }
  };

  const dismissToast = (id) => {
    setToasts(prev => prev.map(t => t.id === id ? { ...t, removing: true } : t));
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 300); // Wait for animation
  };

  const icons = {
    success: '✅',
    error: '❌',
    warning: '⚠️',
    info: 'ℹ️'
  };

  const titles = {
    success: 'Success',
    error: 'Error',
    warning: 'Warning',
    info: 'Info'
  };

  return (
    <ToastContext.Provider value={showToast}>
      {props.children}
      <div id="toast-container">
        <For each={toasts()}>
          {(toast) => (
            <div class={`toast toast-${toast.type} ${toast.removing ? 'removing' : ''}`}>
              <span class="toast-icon">{icons[toast.type]}</span>
              <div class="toast-content">
                <div class="toast-title">{titles[toast.type]}</div>
                <div class="toast-message">{toast.message}</div>
              </div>
              <button class="toast-close" onClick={() => dismissToast(toast.id)}>✕</button>
            </div>
          )}
        </For>
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  return useContext(ToastContext);
}
