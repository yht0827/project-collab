import React, { useState } from 'react';
import { UI_TEXT } from '../../../constants/uiText';

function getDueInfo(dueDateStr, isDone) {
  if (!dueDateStr) return null;
  if (isDone) {
    return { text: UI_TEXT.DUE_DONE(dueDateStr), className: 'due-normal' };
  }

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const due = new Date(dueDateStr);
  due.setHours(0, 0, 0, 0);

  const diffTime = due - today;
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays < 0) {
    return { text: UI_TEXT.DUE_OVERDUE(Math.abs(diffDays)), className: 'due-overdue' };
  } else if (diffDays === 0) {
    return { text: UI_TEXT.DUE_TODAY, className: 'due-today' };
  } else if (diffDays <= 3) {
    return { text: UI_TEXT.DUE_NEAR(diffDays, dueDateStr), className: 'due-near' };
  } else {
    return { text: UI_TEXT.DUE_NORMAL(dueDateStr), className: 'due-normal' };
  }
}

export default function TaskDetailModal({
  task,
  members,
  projectLabels,
  onClose,
  onSave
}) {
  const [title, setTitle] = useState(task.title || '');
  const [description, setDescription] = useState(task.description || '');
  const [assigneeId, setAssigneeId] = useState(task.assigneeId || '');
  const [status, setStatus] = useState(task.status || 'TODO');
  const [dueDate, setDueDate] = useState(task.dueDate || '');
  const [labelIds, setLabelIds] = useState(task.labels ? task.labels.map(l => l.id) : []);

  const toggleLabelSelection = (labelId) => {
    setLabelIds(prev =>
      prev.includes(labelId) ? prev.filter(id => id !== labelId) : [...prev, labelId]
    );
  };

  const handleSave = (e) => {
    e.preventDefault();
    if (!title.trim()) return;

    onSave({
      title: title.trim(),
      description: description.trim(),
      assigneeId: assigneeId ? Number(assigneeId) : null,
      status,
      dueDate: dueDate || null,
      labelIds
    });
  };

  const dueInfo = getDueInfo(dueDate, status === 'DONE');

  return (
    <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className="modal-card" style={{ maxWidth: '540px' }}>
        <div className="modal-header-row">
          <div className="modal-header">{UI_TEXT.MODAL_EDIT_TASK_TITLE}</div>
          <button type="button" className="btn-modal-close-icon" onClick={onClose} title={UI_TEXT.TOOLTIP_CLOSE}>✕</button>
        </div>

        <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div className="form-group">
            <label>{UI_TEXT.LABEL_TITLE}</label>
            <input
              type="text"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>{UI_TEXT.LABEL_DESCRIPTION}</label>
            <textarea
              rows="3"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder={UI_TEXT.PLACEHOLDER_TASK_EDIT_DESC}
            ></textarea>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div className="form-group">
              <label>{UI_TEXT.LABEL_STATUS}</label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value)}
              >
                <option value="TODO">{UI_TEXT.STATUS_TODO}</option>
                <option value="IN_PROGRESS">{UI_TEXT.STATUS_IN_PROGRESS}</option>
                <option value="DONE">{UI_TEXT.STATUS_DONE}</option>
              </select>
            </div>

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
          </div>

          <div className="form-group">
            <label>{UI_TEXT.LABEL_DUE_DATE}</label>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <input
                type="date"
                value={dueDate}
                onChange={(e) => setDueDate(e.target.value)}
                style={{ flex: 1 }}
              />
              {dueInfo && (
                <span className={`card-due-badge ${dueInfo.className}`}>{dueInfo.text}</span>
              )}
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
              {UI_TEXT.BTN_SAVE}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
