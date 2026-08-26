import React, { useState } from 'react';
import { UI_TEXT } from '../../../constants/uiText';

const COLOR_PRESETS = [
  '#3b82f6', // 파랑
  '#10b981', // 초록
  '#8b5cf6', // 보라
  '#ef4444', // 빨강
  '#f59e0b', // 주황
  '#ec4899', // 핑크
  '#6366f1', // 인디고
  '#64748b'  // 회색
];

export default function LabelManageModal({
  labels,
  isManager,
  onClose,
  onCreateLabel,
  onDeleteLabel
}) {
  const [name, setName] = useState('');
  const [color, setColor] = useState(COLOR_PRESETS[0]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    onCreateLabel({ name: name.trim(), color });
    setName('');
  };

  return (
    <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className="modal-card" style={{ maxWidth: '520px' }}>
        <div className="modal-header-row">
          <div className="modal-header">{UI_TEXT.MODAL_LABELS_TITLE}</div>
          <button type="button" className="btn-modal-close-icon" onClick={onClose} title={UI_TEXT.TOOLTIP_CLOSE}>✕</button>
        </div>

        {/* 1. 관리자 전용: 새 라벨 생성 폼 */}
        {isManager ? (
          <form onSubmit={handleSubmit} style={{ background: '#f8fafc', padding: '14px', borderRadius: '10px', border: '1px solid #e2e8f0', display: 'flex', flexDirection: 'column', gap: '10px' }}>
            <div className="form-group">
              <label>{UI_TEXT.LABEL_NAME}</label>
              <input
                type="text"
                required
                placeholder={UI_TEXT.PLACEHOLDER_LABEL_NAME}
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label>{UI_TEXT.LABEL_COLOR}</label>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                {COLOR_PRESETS.map(c => (
                  <button
                    key={c}
                    type="button"
                    onClick={() => setColor(c)}
                    style={{
                      width: '26px',
                      height: '26px',
                      borderRadius: '50%',
                      backgroundColor: c,
                      border: color === c ? '3px solid #0f172a' : '2px solid #ffffff',
                      boxShadow: '0 1px 3px rgba(0,0,0,0.15)',
                      cursor: 'pointer',
                      transform: color === c ? 'scale(1.15)' : 'scale(1)',
                      transition: 'all 0.1s'
                    }}
                    title={c}
                  />
                ))}
              </div>
            </div>

            <button
              type="submit"
              className="btn-submit"
              style={{ marginTop: '4px', alignSelf: 'flex-end', padding: '7px 14px' }}
            >
              {UI_TEXT.BTN_ADD_LABEL}
            </button>
          </form>
        ) : (
          <div style={{ background: '#f1f5f9', padding: '10px 14px', borderRadius: '8px', fontSize: '12px', color: '#64748b' }}>
            💡 라벨 추가 및 삭제는 프로젝트 관리자(OWNER, ADMIN)만 가능합니다.
          </div>
        )}

        {/* 2. 등록된 라벨 목록 */}
        <div>
          <div style={{ fontSize: '12px', fontWeight: '700', color: '#475569', marginBottom: '8px' }}>
            {UI_TEXT.LABELS_LIST_TITLE(labels.length)}
          </div>

          {labels.length === 0 ? (
            <div style={{ fontSize: '12px', color: '#94a3b8', padding: '16px 0', textAlign: 'center' }}>
              {UI_TEXT.NO_LABELS}
            </div>
          ) : (
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', maxHeight: '180px', overflowY: 'auto', padding: '4px 0' }}>
              {labels.map(l => (
                <div
                  key={l.id}
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '6px',
                    backgroundColor: l.color,
                    color: '#ffffff',
                    padding: '4px 10px',
                    borderRadius: '6px',
                    fontSize: '12px',
                    fontWeight: '700',
                    boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
                  }}
                >
                  <span>🏷️ {l.name}</span>
                  {isManager && (
                    <button
                      type="button"
                      onClick={() => onDeleteLabel(l.id)}
                      title={UI_TEXT.TOOLTIP_DELETE_LABEL}
                      style={{
                        background: 'rgba(0,0,0,0.2)',
                        border: 'none',
                        color: '#ffffff',
                        borderRadius: '50%',
                        width: '16px',
                        height: '16px',
                        fontSize: '10px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        cursor: 'pointer'
                      }}
                    >
                      ✕
                    </button>
                  )}
                </div>
              ))}
            </div>
          )}
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
