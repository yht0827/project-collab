import React, { useState, useEffect } from 'react';
import Header from './components/layout/Header';
import Sidebar from './components/layout/Sidebar';
import ProjectBanner from './components/layout/ProjectBanner';
import FilterBar from './components/layout/FilterBar';
import KanbanColumn from './components/kanban/KanbanColumn';
import ToastContainer from './components/common/ToastContainer';

import CreateUserModal from './components/modals/user/CreateUserModal';
import CreateProjectModal from './components/modals/project/CreateProjectModal';
import EditProjectModal from './components/modals/project/EditProjectModal';
import CreateTaskModal from './components/modals/task/CreateTaskModal';
import TaskDetailModal from './components/modals/task/TaskDetailModal';
import MemberManageModal from './components/modals/member/MemberManageModal';
import LabelManageModal from './components/modals/label/LabelManageModal';

import { useToast } from './hooks/useToast';
import { useDebounce } from './hooks/useDebounce';
import { userApi } from './api/userApi';
import { projectApi } from './api/projectApi';
import { taskApi } from './api/taskApi';
import { labelApi } from './api/labelApi';
import { UI_TEXT } from './constants/uiText';

export default function App() {
  const [users, setUsers] = useState([]);
  const [currentUserId, setCurrentUserId] = useState(1);

  const [projects, setProjects] = useState([]);
  const [selectedProject, setSelectedProject] = useState(null);
  const [projectDetail, setProjectDetail] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [members, setMembers] = useState([]);
  const [projectLabels, setProjectLabels] = useState([]);
  
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const [keywordInput, setKeywordInput] = useState('');
  const debouncedKeyword = useDebounce(keywordInput, 300);
  const [statusFilter, setStatusFilter] = useState('');
  const [labelFilter, setLabelFilter] = useState('');

  const { toasts, showToast, removeToast } = useToast();

  // 모달 상태
  const [showNewTaskModal, setShowNewTaskModal] = useState(false);
  const [showEditTaskModal, setShowEditTaskModal] = useState(false);
  const [editingTask, setEditingTask] = useState(null);
  const [showNewProjectModal, setShowNewProjectModal] = useState(false);
  const [showEditProjectModal, setShowEditProjectModal] = useState(false);
  const [showMembersModal, setShowMembersModal] = useState(false);
  const [showLabelsModal, setShowLabelsModal] = useState(false);
  const [showNewUserModal, setShowNewUserModal] = useState(false);

  const isManager = projectDetail?.myRole === 'OWNER' || projectDetail?.myRole === 'ADMIN';

  useEffect(() => {
    setCurrentPage(0);
  }, [debouncedKeyword, labelFilter, statusFilter]);

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      const res = await userApi.getUsers();
      if (res.success && res.data.length > 0) {
        setUsers(res.data);
        if (!currentUserId) setCurrentUserId(res.data[0].id);
      }
    } catch {
      showToast(UI_TEXT.ERR_NETWORK, 'error');
    }
  };

  useEffect(() => {
    if (currentUserId) loadProjects();
  }, [currentUserId]);

  useEffect(() => {
    if (selectedProject && currentUserId) {
      loadProjectDetail(selectedProject.id);
      loadTasks(selectedProject.id);
      loadMembers(selectedProject.id);
      loadLabels(selectedProject.id);
    }
  }, [selectedProject?.id, currentUserId, statusFilter, labelFilter, debouncedKeyword, currentPage, pageSize]);

  const loadProjects = async () => {
    try {
      const res = await projectApi.getProjects(currentUserId);
      if (res.success && res.data.length > 0) {
        setProjects(res.data);
        setSelectedProject(prev => {
          if (!prev) return res.data[0];
          const matched = res.data.find(p => p.id === prev.id);
          return matched || res.data[0];
        });
      } else {
        setProjects([]);
        setSelectedProject(null);
      }
    } catch {
      showToast(UI_TEXT.ERR_NETWORK, 'error');
    }
  };

  const loadProjectDetail = async (projectId) => {
    const res = await projectApi.getProject(projectId, currentUserId);
    if (res.success) {
      setProjectDetail(res.data);
    }
  };

  const loadTasks = async (projectId) => {
    const res = await taskApi.getTasks(projectId, currentUserId, {
      page: currentPage,
      size: pageSize,
      status: statusFilter,
      labelId: labelFilter,
      keyword: debouncedKeyword
    });
    if (res.success) {
      setTasks(res.data.content || []);
      setTotalPages(res.data.totalPages || 1);
      setTotalElements(res.data.totalElements || 0);
    }
  };

  const loadMembers = async (projectId) => {
    const res = await projectApi.getMembers(projectId, currentUserId);
    if (res.success) setMembers(res.data);
  };

  const loadLabels = async (projectId) => {
    const res = await labelApi.getLabels(projectId, currentUserId);
    if (res.success) setProjectLabels(res.data);
  };

  // 핸들러 모음
  const handleCreateUser = async (userName) => {
    const res = await userApi.createUser(userName);
    if (res.success) {
      setShowNewUserModal(false);
      showToast(UI_TEXT.SUCCESS_USER_CREATE(res.data.name), 'success', UI_TEXT.TITLE_SUCCESS_USER_CREATE);
      await loadUsers();
      setCurrentUserId(res.data.id);
    } else {
      showToast(res.message || UI_TEXT.ERR_USER_CREATE_FAIL, 'error', UI_TEXT.TITLE_ERROR_USER_CREATE);
    }
  };

  const handleCreateProject = async (projectData) => {
    const res = await projectApi.createProject(currentUserId, projectData);
    if (res.success) {
      setShowNewProjectModal(false);
      showToast(UI_TEXT.SUCCESS_PROJECT_CREATE(res.data.name), 'success', UI_TEXT.TITLE_SUCCESS_PROJECT_CREATE);
      loadProjects();
    } else {
      showToast(res.message || UI_TEXT.ERR_PROJECT_DELETE_FAIL, 'error', UI_TEXT.TITLE_ERROR_PROJECT_CREATE);
    }
  };

  const handleUpdateProject = async (projectData) => {
    const res = await projectApi.updateProject(selectedProject.id, currentUserId, projectData);
    if (res.success) {
      setShowEditProjectModal(false);
      showToast(UI_TEXT.SUCCESS_PROJECT_UPDATE, 'success', UI_TEXT.TITLE_SUCCESS_PROJECT_UPDATE);
      setSelectedProject(res.data);
      setProjects(prev => prev.map(p => p.id === res.data.id ? res.data : p));
      setProjectDetail(prev => prev ? { ...prev, name: res.data.name, description: res.data.description } : prev);
    } else {
      showToast(res.message || UI_TEXT.ERR_PROJECT_UPDATE_FAIL, 'error', UI_TEXT.TITLE_ERROR_PROJECT_UPDATE);
    }
  };

  const handleDeleteProject = async () => {
    if (!confirm(UI_TEXT.CONFIRM_PROJECT_DELETE)) return;
    const res = await projectApi.deleteProject(selectedProject.id, currentUserId);
    if (res.success) {
      showToast(UI_TEXT.SUCCESS_PROJECT_DELETE, 'success');
      loadProjects();
    } else {
      showToast(res.message || UI_TEXT.ERR_PROJECT_DELETE_FAIL, 'error');
    }
  };

  const handleInviteMember = async (targetUserId, role) => {
    const res = await projectApi.inviteMember(selectedProject.id, currentUserId, targetUserId, role);
    if (res.success) {
      showToast(UI_TEXT.SUCCESS_MEMBER_INVITE, 'success', UI_TEXT.TITLE_SUCCESS_MEMBER_INVITE);
      loadMembers(selectedProject.id);
    } else {
      showToast(res.message || UI_TEXT.TITLE_ERROR_MEMBER_INVITE, 'error', UI_TEXT.TITLE_ERROR_MEMBER_INVITE);
    }
  };

  const handleUpdateMemberRole = async (targetUserId, role) => {
    const res = await projectApi.updateMemberRole(selectedProject.id, currentUserId, targetUserId, role);
    if (res.success) {
      showToast(UI_TEXT.SUCCESS_MEMBER_ROLE, 'success');
      loadMembers(selectedProject.id);
      loadProjectDetail(selectedProject.id);
    } else {
      showToast(res.message || UI_TEXT.ERR_MEMBER_ROLE_FAIL, 'error', UI_TEXT.TITLE_ERROR_MEMBER_ROLE);
    }
  };

  const handleRemoveMember = async (targetUserId) => {
    if (!confirm(UI_TEXT.CONFIRM_MEMBER_REMOVE)) return;
    const res = await projectApi.removeMember(selectedProject.id, currentUserId, targetUserId);
    if (res.success) {
      showToast(UI_TEXT.SUCCESS_MEMBER_REMOVE, 'success');
      loadMembers(selectedProject.id);
      loadProjectDetail(selectedProject.id);
      loadProjects();
    } else {
      showToast(res.message || UI_TEXT.ERR_MEMBER_REMOVE_FAIL, 'error', UI_TEXT.TITLE_ERROR_MEMBER_REMOVE);
    }
  };

  const handleCreateLabel = async (labelData) => {
    const res = await labelApi.createLabel(selectedProject.id, currentUserId, labelData);
    if (res.success) {
      showToast(UI_TEXT.SUCCESS_LABEL_CREATE, 'success', UI_TEXT.TITLE_SUCCESS_LABEL_CREATE);
      loadLabels(selectedProject.id);
    } else {
      showToast(res.message || UI_TEXT.ERR_LABEL_CREATE_FAIL, 'error', UI_TEXT.TITLE_ERROR_LABEL_CREATE);
    }
  };

  const handleDeleteLabel = async (labelId) => {
    if (!confirm(UI_TEXT.CONFIRM_LABEL_DELETE)) return;
    const res = await labelApi.deleteLabel(selectedProject.id, labelId, currentUserId);
    if (res.success) {
      showToast(UI_TEXT.SUCCESS_LABEL_DELETE, 'success');
      loadLabels(selectedProject.id);
      loadTasks(selectedProject.id);
    } else {
      showToast(res.message || UI_TEXT.ERR_LABEL_DELETE_FAIL, 'error', UI_TEXT.TITLE_ERROR_LABEL_DELETE);
    }
  };

  const handleCreateTask = async (taskData) => {
    const res = await taskApi.createTask(selectedProject.id, currentUserId, taskData);
    if (res.success) {
      setShowNewTaskModal(false);
      showToast(UI_TEXT.SUCCESS_TASK_CREATE, 'success');
      loadTasks(selectedProject.id);
    } else {
      showToast(res.message || UI_TEXT.ERR_TASK_CREATE_FAIL, 'error', UI_TEXT.TITLE_ERROR_TASK_CREATE);
    }
  };

  const handleOpenEditModal = (task) => {
    setEditingTask(task);
    setShowEditTaskModal(true);
  };

  const handleSaveTaskEdit = async (taskData) => {
    const res = await taskApi.updateTask(selectedProject.id, editingTask.id, currentUserId, taskData);
    if (res.success) {
      setShowEditTaskModal(false);
      setEditingTask(null);
      showToast(UI_TEXT.SUCCESS_TASK_UPDATE, 'success');
      loadTasks(selectedProject.id);
    } else {
      showToast(res.message || UI_TEXT.ERR_TASK_UPDATE_FAIL, 'error', UI_TEXT.TITLE_ERROR_TASK_UPDATE);
    }
  };

  const handleUpdateTaskStatus = async (task, newStatus) => {
    const res = await taskApi.updateTask(selectedProject.id, task.id, currentUserId, {
      title: task.title,
      description: task.description,
      assigneeId: task.assigneeId ? task.assigneeId : null,
      status: newStatus,
      dueDate: task.dueDate || null,
      labelIds: task.labels ? task.labels.map(l => l.id) : []
    });
    if (res.success) {
      showToast(UI_TEXT.SUCCESS_TASK_STATUS_CHANGE(newStatus), 'success');
      loadTasks(selectedProject.id);
    } else {
      showToast(res.message || UI_TEXT.ERR_TASK_UPDATE_FAIL, 'error', UI_TEXT.TITLE_ERROR_TASK_UPDATE);
    }
  };

  const handleDeleteTask = async (taskId) => {
    if (!confirm(UI_TEXT.CONFIRM_TASK_DELETE)) return;
    const res = await taskApi.deleteTask(selectedProject.id, taskId, currentUserId);
    if (res.success) {
      showToast(UI_TEXT.SUCCESS_TASK_DELETE, 'success');
      loadTasks(selectedProject.id);
    } else {
      showToast(res.message || UI_TEXT.ERR_TASK_DELETE_FAIL, 'error');
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', overflow: 'hidden' }}>
      <Header
        users={users}
        currentUserId={currentUserId}
        onUserChange={setCurrentUserId}
        onOpenNewUserModal={() => setShowNewUserModal(true)}
      />

      <div className="main-container">
        <Sidebar
          projects={projects}
          selectedProject={selectedProject}
          onSelectProject={setSelectedProject}
          onOpenNewProjectModal={() => setShowNewProjectModal(true)}
        />

        <main>
          {selectedProject ? (
            <>
              <ProjectBanner
                project={selectedProject}
                projectDetail={projectDetail}
                memberCount={members.length}
                labelCount={projectLabels.length}
                onOpenMembersModal={() => setShowMembersModal(true)}
                onOpenLabelsModal={() => setShowLabelsModal(true)}
                onOpenEditProjectModal={() => setShowEditProjectModal(true)}
                onDeleteProject={handleDeleteProject}
                onOpenNewTaskModal={() => setShowNewTaskModal(true)}
              />

              <FilterBar
                keywordInput={keywordInput}
                onKeywordChange={setKeywordInput}
                onClearKeyword={() => setKeywordInput('')}
                totalElements={totalElements}
                projectLabels={projectLabels}
                labelFilter={labelFilter}
                onLabelFilterChange={setLabelFilter}
                statusFilter={statusFilter}
                onStatusFilterChange={setStatusFilter}
                pageSize={pageSize}
                onPageSizeChange={setPageSize}
              />

              {/* 🌟 3열 칸반 그리드 */}
              <div className="kanban-grid">
                <KanbanColumn
                  title={UI_TEXT.STATUS_TODO}
                  badgeClass="badge-todo"
                  tasks={tasks.filter(t => t.status === 'TODO')}
                  onStatusChange={handleUpdateTaskStatus}
                  onEditTask={handleOpenEditModal}
                  onDeleteTask={handleDeleteTask}
                />

                <KanbanColumn
                  title={UI_TEXT.STATUS_IN_PROGRESS}
                  badgeClass="badge-progress"
                  tasks={tasks.filter(t => t.status === 'IN_PROGRESS')}
                  onStatusChange={handleUpdateTaskStatus}
                  onEditTask={handleOpenEditModal}
                  onDeleteTask={handleDeleteTask}
                />

                <KanbanColumn
                  title={UI_TEXT.STATUS_DONE}
                  badgeClass="badge-done"
                  tasks={tasks.filter(t => t.status === 'DONE')}
                  onStatusChange={handleUpdateTaskStatus}
                  onEditTask={handleOpenEditModal}
                  onDeleteTask={handleDeleteTask}
                />
              </div>

              {totalPages > 1 && (
                <div className="pagination-bar">
                  <button
                    className="btn-page"
                    disabled={currentPage === 0}
                    onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                  >
                    {UI_TEXT.PAGE_PREV}
                  </button>
                  
                  <span className="page-info">
                    {UI_TEXT.PAGE_INFO(currentPage + 1, totalPages)}
                  </span>

                  <button
                    className="btn-page"
                    disabled={currentPage >= totalPages - 1}
                    onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                  >
                    {UI_TEXT.PAGE_NEXT}
                  </button>
                </div>
              )}
            </>
          ) : (
            <div style={{ textAlign: 'center', padding: '60px', color: '#94a3b8' }}>
              {UI_TEXT.NO_PROJECTS}
            </div>
          )}
        </main>
      </div>

      {/* 모달 팝업들 */}
      {showNewUserModal && (
        <CreateUserModal
          onClose={() => setShowNewUserModal(false)}
          onSubmit={handleCreateUser}
        />
      )}

      {showNewProjectModal && (
        <CreateProjectModal
          onClose={() => setShowNewProjectModal(false)}
          onSubmit={handleCreateProject}
        />
      )}

      {showEditProjectModal && selectedProject && (
        <EditProjectModal
          project={selectedProject}
          onClose={() => setShowEditProjectModal(false)}
          onSubmit={handleUpdateProject}
        />
      )}

      {showNewTaskModal && (
        <CreateTaskModal
          members={members}
          projectLabels={projectLabels}
          onClose={() => setShowNewTaskModal(false)}
          onSubmit={handleCreateTask}
        />
      )}

      {showEditTaskModal && editingTask && (
        <TaskDetailModal
          task={editingTask}
          members={members}
          projectLabels={projectLabels}
          onClose={() => { setShowEditTaskModal(false); setEditingTask(null); }}
          onSave={handleSaveTaskEdit}
        />
      )}

      {showMembersModal && (
        <MemberManageModal
          members={members}
          users={users}
          onClose={() => setShowMembersModal(false)}
          onInviteMember={handleInviteMember}
          onUpdateRole={handleUpdateMemberRole}
          onRemoveMember={handleRemoveMember}
        />
      )}

      {showLabelsModal && (
        <LabelManageModal
          labels={projectLabels}
          isManager={isManager}
          onClose={() => setShowLabelsModal(false)}
          onCreateLabel={handleCreateLabel}
          onDeleteLabel={handleDeleteLabel}
        />
      )}

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
