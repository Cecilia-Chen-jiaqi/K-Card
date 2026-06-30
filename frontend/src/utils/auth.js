export function getCurrentUser() {
  try {
    return JSON.parse(localStorage.getItem('currentUser') || 'null');
  } catch {
    return null;
  }
}

export function isLoggedIn() {
  return !!localStorage.getItem('authToken');
}

export function isAdmin() {
  const user = getCurrentUser();
  return user?.role === 1;
}

export function getAuthHeaders() {
  const token = localStorage.getItem('authToken');
  return token ? { Authorization: `Bearer ${token}` } : {};
}
