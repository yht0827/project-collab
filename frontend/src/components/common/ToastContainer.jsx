import React from 'react';
import { UI_TEXT } from '../../constants/uiText';

export default function ToastContainer({ toasts, onRemove }) {
  if (!toasts || toasts.length === 0) return null;

  return (
    <div className="toast-container">
      {toasts.map(t => (
        <div key={t.id} className={`toast-item ${t.type}`}>
          <div className="toast-icon">
            {t.type === 'error' && '🚨'}
            {t.type === 'success' && '✅'}
            {t.type === 'info' && 'ℹ️'}
          </div>
          <div className="toast-content">
            {t.title && <div className="toast-title">{t.title}</div>}
            <div className="toast-message">{t.message}</div>
          </div>
          <button
            className="toast-close-btn"
            onClick={() => onRemove(t.id)}
            title={UI_TEXT.TOOLTIP_CLOSE}
          >
            ✕
          </button>
        </div>
      ))}
    </div>
  );
}
