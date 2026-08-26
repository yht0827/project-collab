import React from 'react';
import { UI_TEXT } from '../../constants/uiText';

export default function Header({ users, currentUserId, onUserChange, onOpenNewUserModal }) {
  return (
    <header>
      <div className="brand">
        <div className="brand-logo">P</div>
        <div>
          <div className="brand-title">{UI_TEXT.APP_TITLE}</div>
          <div className="brand-desc">{UI_TEXT.APP_SUBTITLE}</div>
        </div>
      </div>

      <div className="header-user-area">
        <div className="user-switcher">
          <label>{UI_TEXT.USER_SWITCHER_LABEL}</label>
          <select
            value={currentUserId || ''}
            onChange={(e) => onUserChange(Number(e.target.value))}
          >
            {users.map(u => (
              <option key={u.id} value={u.id}>{u.name} (ID: {u.id})</option>
            ))}
          </select>
        </div>

        <button
          className="btn-new-user"
          onClick={onOpenNewUserModal}
        >
          {UI_TEXT.BTN_NEW_USER}
        </button>
      </div>
    </header>
  );
}
