import { createSignal, createResource, Show, For } from 'solid-js';
import { getUsers, updateUserStatus } from '../lib/api';
import { formatDate, statusBadgeClass, getInitials, roleLabel, roleBadgeClass } from '../lib/utils';
import { useToast } from '../components/ToastProvider';
import CreateUserModal from '../components/CreateUserModal';

const fetchUsers = async (page) => {
  return await getUsers(page, 15);
};

export default function Users() {
  const showToast = useToast();
  const [page, setPage] = createSignal(0);
  const [data, { refetch }] = createResource(page, fetchUsers);
  const [showCreateModal, setShowCreateModal] = createSignal(false);

  const handleStatusChange = async (id, newStatus) => {
    try {
      await updateUserStatus(id, newStatus);
      showToast('User status updated successfully', 'success');
      refetch();
    } catch (err) {
      showToast(err.message || 'Failed to update status', 'error');
    }
  };

  return (
    <div class="page-container page-enter">
      <header class="page-header">
        <h1 class="page-title">User Management</h1>
        <p class="page-subtitle">Manage system users, roles, and account statuses.</p>
      </header>

      <div class="users-toolbar">
        <div class="search-bar">
          <span class="search-icon">🔍</span>
          <input type="text" class="form-input" placeholder="Search users by name or email..." />
        </div>
        <button class="btn btn-primary" onClick={() => setShowCreateModal(true)}>
          <span style={{ "margin-right": "var(--sp-1)" }}>➕</span>
          Create User
        </button>
      </div>

      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th>User</th>
              <th>Role</th>
              <th>Student ID</th>
              <th>Joined Date</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <Show when={data.loading}>
              <tr><td colspan="6" style={{ "text-align": "center", padding: "2rem" }}><span class="spinner" style={{ "border-color": "var(--accent-teal)", "border-right-color": "transparent" }}></span> Loading...</td></tr>
            </Show>
            <Show when={!data.loading && data()?.content}>
              <For each={data().content}>
                {(user) => (
                  <tr>
                    <td>
                      <div class="user-name-cell">
                        <div class="avatar">{getInitials(user.fullName)}</div>
                        <div>
                          <div class="user-name-text">{user.fullName}</div>
                          <div style={{ "font-size": "var(--fs-xs)", color: "var(--text-tertiary)" }}>{user.email}</div>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span class={`badge ${roleBadgeClass(user.role)}`}>{roleLabel(user.role)}</span>
                    </td>
                    <td style={{ "font-family": "var(--font-mono)", "font-size": "var(--fs-xs)" }}>
                      {user.studentNumber || '—'}
                    </td>
                    <td>{formatDate(user.createdAt)}</td>
                    <td>
                      <span class={`badge badge-dot ${statusBadgeClass(user.accountStatus)}`}>
                        {user.accountStatus}
                      </span>
                    </td>
                    <td>
                      <select 
                        class="form-select" 
                        style={{ padding: "var(--sp-1) var(--sp-2)", "padding-right": "var(--sp-8)", "font-size": "var(--fs-xs)", width: "auto" }}
                        value={user.accountStatus}
                        onChange={(e) => handleStatusChange(user.id, e.target.value)}
                      >
                        <option value="ACTIVE">Set Active</option>
                        <option value="SUSPENDED">Suspend</option>
                        <option value="GRADUATED">Graduate</option>
                      </select>
                    </td>
                  </tr>
                )}
              </For>
            </Show>
          </tbody>
        </table>
        
        <Show when={data()?.totalPages > 1}>
          <div class="pagination">
            <button class="pagination-btn" disabled={data().first} onClick={() => setPage(p => p - 1)}>←</button>
            <span style={{ "font-size": "var(--fs-sm)", color: "var(--text-secondary)", margin: "0 var(--sp-2)" }}>
              Page {data().number + 1} of {data().totalPages}
            </span>
            <button class="pagination-btn" disabled={data().last} onClick={() => setPage(p => p + 1)}>→</button>
          </div>
        </Show>
      </div>

      <CreateUserModal 
        isOpen={showCreateModal()} 
        onClose={() => setShowCreateModal(false)}
        onSuccess={() => refetch()}
      />
    </div>
  );
}
