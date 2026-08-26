import React, { useState } from 'react';
import { UI_TEXT } from '../../../constants/uiText';

export default function CreateTaskModal({ members, projectLabels, onClose, onSubmit }) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [assigneeId, setAssigneeId] = useState('');
  const [dueDate, setDueDate] = useState('');
  const [labelIds, setLabelIds] = useState([]);

  const toggleLabelSelection = (labelId) => {
    setLabelIds(prev =>
      prev.includes(labelId) ? prev.filter(id => id !== labelId) : [...prev, labelId]
    );
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!title.trim()) return;

    onSubmit({
      title: title.trim(),
      description: description.trim(),
      assigneeId: assigneeId ? Number(assigneeId) : null,
      dueDate: dueDate || null,
      labelIds
    });
  };

  return (
    <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className="modal-card" style={{ maxWidth: '520px' }}>
        <div className="modal-header-row">
          <div className="modal-header">{UI_TEXT.MODAL_NEW_TASK_TITLE}</div>
          <button type="button" className="btn-modal-close-icon" onClick={onClose} title={UI_TEXT.TOOLTIP_CLOSE}>✕</button>
        </div>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div className="form-group">
            <label>{UI_TEXT.LABEL_TITLE}</label>
            <input
              type="text"
              required
              placeholder={UI_TEXT.PLACEHOLDER_TASK_TITLE}
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>{UI_TEXT.LABEL_DESCRIPTION}</label>
            <textarea
              rows="2"
              placeholder={UI_TEXT.PLACEHOLDER_TASK_DESC}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            ></textarea>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div className="form-group">
              <label>{UI_TEXT.LABEL_ASSIGNEE}</label>
              <select
                value={assigneeId}
                onChange={(e) => setAssigneeId(e.target.value)}
              >
                <option value="">{UI_TEXT.OPTION_UNASSIGNED}</option>
                {members.map(m => (
                  <option key={m.userId} value={m.userId}>{m.userName} ({m.role})</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>{UI_TEXT.LABEL_DUE_DATE}</label>
              <input
                type="date"
                value={dueDate}
                onChange={(e) => setDueDate(e.target.value)}
              />
            </div>
          </div>

          {projectLabels.length > 0 && (
            <div className="form-group">
              <label>{UI_TEXT.LABEL_TAGS}</label>
              <div className="labels-selector">
                {projectLabels.map(l => {
                  const isSelected = labelIds.includes(l.id);
                  return (
                    <span
                      key={l.id}
                      className={`label-checkbox-tag ${isSelected ? 'selected' : ''}`}
                      style={{ backgroundColor: isSelected ? l.color : '#ffffff' }}
                      onClick={() => toggleLabelSelection(l.id)}
                    >
                      🏷️ {l.name}
                    </span>
                  );
                })}
              </div>
            </div>
          )}

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
