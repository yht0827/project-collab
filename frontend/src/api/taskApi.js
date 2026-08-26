import { request } from './client';

export const taskApi = {
  createTask: (projectId, userId, data) =>
    request(`/projects/${projectId}/tasks`, {
      method: 'POST',
      headers: { 'X-User-Id': userId },
      body: JSON.stringify(data)
    }),

  getTasks: (projectId, userId, params = {}) => {
    const query = new URLSearchParams();
    if (params.page !== undefined) query.set('page', params.page);
    if (params.size !== undefined) query.set('size', params.size);
    if (params.status) query.set('status', params.status);
    if (params.labelId) query.set('labelId', params.labelId);
    if (params.keyword) query.set('keyword', params.keyword);

    return request(`/projects/${projectId}/tasks?${query.toString()}`, {
      headers: { 'X-User-Id': userId }
    });
  },

  getTask: (projectId, taskId, userId) =>
    request(`/projects/${projectId}/tasks/${taskId}`, {
      headers: { 'X-User-Id': userId }
    }),

  updateTask: (projectId, taskId, userId, data) =>
    request(`/projects/${projectId}/tasks/${taskId}`, {
      method: 'PUT',
      headers: { 'X-User-Id': userId },
      body: JSON.stringify(data)
    }),

  deleteTask: (projectId, taskId, userId) =>
    request(`/projects/${projectId}/tasks/${taskId}`, {
      method: 'DELETE',
      headers: { 'X-User-Id': userId }
    })
};
