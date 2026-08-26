import React from 'react';
import { UI_TEXT } from '../../constants/uiText';

export default function Sidebar({ projects, selectedProject, onSelectProject, onOpenNewProjectModal }) {
  return (
    <aside>
      <div className="sidebar-header">
        <span className="sidebar-title">{UI_TEXT.MY_PROJECTS_TITLE}</span>
        <span className="project-count">{projects.length}</span>
      </div>

      <button
        onClick={onOpenNewProjectModal}
        className="btn-new-project"
      >
        {UI_TEXT.BTN_NEW_PROJECT}
      </button>

      <div className="project-list">
        {projects.map(p => (
          <button
            key={p.id}
            onClick={() => onSelectProject(p)}
            className={`project-card ${selectedProject?.id === p.id ? 'active' : ''}`}
          >
            <div className="project-card-name">{p.name}</div>
            <div className="project-card-desc">{p.description || UI_TEXT.NO_DESCRIPTION}</div>
          </button>
        ))}
      </div>
    </aside>
  );
}
