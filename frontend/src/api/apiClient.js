const API_BASE = '/api/v1';

export async function request(endpoint, options = {}, userId = null) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  if (userId) {
    headers['X-User-Id'] = userId;
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers
  });

  const json = await response.json().catch(() => ({
    success: false,
    message: '응답을 파싱할 수 없습니다.'
  }));

  if (!response.ok && json.success === undefined) {
    json.success = false;
  }

  return json;
}
