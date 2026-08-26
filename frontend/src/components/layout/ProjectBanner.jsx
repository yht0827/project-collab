import React from 'react';
import { UI_TEXT } from '../../constants/uiText';

export default function ProjectBanner({
  project,
  projectDetail,
  memberCount,
  labelCount,
  onOpenMembersModal,
  onOpenLabelsModal,
  onOpenEditProjectModal,
  onDeleteProject,
  onOpenNewTaskModal
}) {
  const isManager = projectDetail?.myRole === 'OWNER' || projectDetail?.myRole === 'ADMIN';

  return (
    <div className="project-banner">
      <div className="project-info">
        <h2>
          {project.name}
          {isManager && (
            <button
              className="btn-edit-task"
              onClick={onOpenEditProjectModal}
              title={UI_TEXT.TOOLTIP_EDIT_PROJECT}
              style={{ fontSize: '14px', marginLeft: '2px' }}
            >
              ✏️
            </button>
          )}
          {projectDetail && (
            <span className="role-badge">{UI_TEXT.ROLE_PREFIX} {projectDetail.myRole}</span>
          )}
        </h2>
        <p>{project.description}</p>
      </div>

      <div className="banner-actions">
        <button
          className="btn-secondary"
          onClick={onOpenMembersModal}
        >
          {UI_TEXT.BTN_MANAGE_MEMBERS(memberCount)}
        </button>

        <button
          className="btn-secondary"
          onClick={onOpenLabelsModal}
        >
          {UI_TEXT.BTN_MANAGE_LABELS(labelCount)}
        </button>

        {projectDetail?.myRole === 'OWNER' && (
          <button
            className="btn-danger-outline"
            onClick={onDeleteProject}
          >
            {UI_TEXT.BTN_DELETE_PROJECT}
          </button>
        )}

        <button
          className="btn-create-task"
          onClick={onOpenNewTaskModal}
        >
          {UI_TEXT.BTN_NEW_TASK}
        </button>
      </div>
    </div>
  );
}
