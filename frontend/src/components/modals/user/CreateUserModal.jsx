import React, { useState } from 'react';
import { UI_TEXT } from '../../../constants/uiText';

export default function CreateUserModal({ onClose, onSubmit }) {
  const [userName, setUserName] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!userName.trim()) return;
    onSubmit(userName.trim());
  };

  return (
    <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className="modal-card">
        <div className="modal-header-row">
          <div className="modal-header">{UI_TEXT.MODAL_NEW_USER_TITLE}</div>
          <button type="button" className="btn-modal-close-icon" onClick={onClose} title={UI_TEXT.TOOLTIP_CLOSE}>✕</button>
        </div>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div className="form-group">
            <label>{UI_TEXT.LABEL_USER_NAME}</label>
            <input
              type="text"
              required
              placeholder={UI_TEXT.PLACEHOLDER_USER_NAME}
              value={userName}
              onChange={(e) => setUserName(e.target.value)}
            />
          </div>

          <div className="modal-actions">
            <button
              type="button"
              className="btn-cancel"
              onClick={onClose}
            >
              {UI_TEXT.BTN_CANCEL}
            </button>
            <button
              type="submit"
              className="btn-submit"
            >
              {UI_TEXT.BTN_SUBMIT}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
