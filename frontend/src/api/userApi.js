import { request } from './apiClient';

export const userApi = {
  getUsers: () => request('/users'),
  createUser: (name) => request('/users', {
    method: 'POST',
    body: JSON.stringify({ name })
  })
};
