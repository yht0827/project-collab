import { request } from './apiClient';

export const projectApi = {
  getProjects: (userId) => request('/projects', {}, userId),
  getProject: (projectId, userId) => request(`/projects/${projectId}`, {}, userId),
  createProject: (userId, data) => request('/projects', {
    method: 'POST',
    body: JSON.stringify(data)
  }, userId),
  deleteProject: (projectId, userId) => request(`/projects/${projectId}`, {
    method: 'DELETE'
  }, userId),
  getMembers: (projectId, userId) => request(`/projects/${projectId}/members`, {}, userId),
  inviteMember: (projectId, userId, targetUserId, role) => request(`/projects/${projectId}/members`, {
    method: 'POST',
    body: JSON.stringify({ userId: targetUserId, role })
  }, userId),
  updateMemberRole: (projectId, userId, targetUserId, role) => request(`/projects/${projectId}/members/${targetUserId}`, {
    method: 'PUT',
    body: JSON.stringify({ role })
  }, userId),
  removeMember: (projectId, userId, targetUserId) => request(`/projects/${projectId}/members/${targetUserId}`, {
    method: 'DELETE'
  }, userId)
};
