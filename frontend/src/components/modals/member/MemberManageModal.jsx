import React, { useState } from 'react';
import { UI_TEXT } from '../../../constants/uiText';

export default function MemberManageModal({
  members,
  users,
  onClose,
  onInviteMember,
  onUpdateRole,
  onRemoveMember
}) {
  const [inviteUserId, setInviteUserId] = useState('');
  const [inviteRole, setInviteRole] = useState('MEMBER');

  const handleInvite = (e) => {
    e.preventDefault();
    if (!inviteUserId) return;
    onInviteMember(Number(inviteUserId), inviteRole);
    setInviteUserId('');
  };

  return (
    <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className="modal-card" style={{ maxWidth: '560px' }}>
        <div className="modal-header-row">
          <div className="modal-header">{UI_TEXT.MODAL_MEMBERS_TITLE}</div>
          <button type="button" className="btn-modal-close-icon" onClick={onClose} title={UI_TEXT.TOOLTIP_CLOSE}>✕</button>
        </div>
        
        {/* 1. 멤버 초대 */}
        <form onSubmit={handleInvite} style={{ background: '#f8fafc', padding: '12px', borderRadius: '8px', border: '1px solid #e2e8f0', display: 'flex', gap: '8px', alignItems: 'flex-end' }}>
          <div className="form-group" style={{ flex: 1 }}>
            <label>{UI_TEXT.LABEL_USER_SELECT}</label>
            <select
              required
              value={inviteUserId}
              onChange={(e) => setInviteUserId(e.target.value)}
            >
              <option value="">{UI_TEXT.OPTION_SELECT_USER}</option>
              {users.map(u => (
                <option key={u.id} value={u.id}>{u.name}</option>
              ))}
            </select>
          </div>

          <div className="form-group" style={{ width: '110px' }}>
            <label>{UI_TEXT.LABEL_ROLE_SELECT}</label>
            <select
              value={inviteRole}
              onChange={(e) => setInviteRole(e.target.value)}
            >
              <option value="MEMBER">MEMBER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </div>

          <button type="submit" className="btn-submit" style={{ padding: '8px 14px' }}>
            {UI_TEXT.BTN_INVITE}
          </button>
        </form>

        {/* 2. 멤버 목록 */}
        <div>
          <div style={{ fontSize: '12px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>
            {UI_TEXT.MEMBERS_LIST_TITLE(members.length)}
          </div>
          <table className="member-table">
            <thead>
              <tr>
                <th>{UI_TEXT.TH_USER}</th>
                <th>{UI_TEXT.TH_ROLE_CHANGE}</th>
                <th>{UI_TEXT.TH_JOINED_AT}</th>
                <th style={{ textAlign: 'center' }}>{UI_TEXT.TH_MANAGE}</th>
              </tr>
            </thead>
            <tbody>
              {members.map(m => (
                <tr key={m.userId}>
                  <td style={{ fontWeight: '600' }}>{m.userName}</td>
                  <td>
                    <select
                      className="role-select"
                      value={m.role}
                      onChange={(e) => onUpdateRole(m.userId, e.target.value)}
                    >
                      <option value="OWNER">OWNER</option>
                      <option value="ADMIN">ADMIN</option>
                      <option value="MEMBER">MEMBER</option>
                    </select>
                  </td>
                  <td style={{ color: '#94a3b8', fontSize: '11px' }}>
                    {new Date(m.joinedAt).toLocaleDateString()}
                  </td>
                  <td style={{ textAlign: 'center' }}>
                    <button
                      className="btn-remove-member"
                      onClick={() => onRemoveMember(m.userId)}
                      title={UI_TEXT.TOOLTIP_REMOVE_MEMBER}
                    >
                      ✕
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="modal-actions">
          <button
            type="button"
            className="btn-cancel"
            onClick={onClose}
          >
            {UI_TEXT.BTN_CLOSE}
          </button>
        </div>
      </div>
    </div>
  );
}
