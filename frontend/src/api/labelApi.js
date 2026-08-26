import { request } from './apiClient';

export const labelApi = {
  getLabels: (projectId, userId) => request(`/projects/${projectId}/labels`, {}, userId),
  createLabel: (projectId, userId, { name, color }) => request(`/projects/${projectId}/labels`, {
    method: 'POST',
    body: JSON.stringify({ name, color })
  }, userId),
  deleteLabel: (projectId, labelId, userId) => request(`/projects/${projectId}/labels/${labelId}`, {
    method: 'DELETE'
  }, userId)
};
