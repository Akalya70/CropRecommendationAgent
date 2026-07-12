let currentUserId = null;

document.addEventListener('DOMContentLoaded', async () => {
    const user = await checkAuthentication('ROLE_ADMIN');
    if (!user) return;
    currentUserId = user.id;
    loadUsers();
    loadLandHistory();
    loadLeafHistory();
    loadFeedback();
});

// Tab Switching
const tabBtns = document.querySelectorAll('.tab-btn');
tabBtns.forEach(btn => {
    btn.addEventListener('click', () => {
        tabBtns.forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(tc => tc.classList.remove('active'));
        btn.classList.add('active');
        document.getElementById(btn.dataset.tab).classList.add('active');
    });
});

async function loadUsers() {
    try {
        const response = await fetch('/api/admin/users');
        if (!response.ok) return;
        const users = await response.json();
        const tbody = document.getElementById('usersTableBody');
        tbody.innerHTML = '';

        if (users.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No users found.</td></tr>';
            return;
        }

        users.forEach(u => {
            const tr = document.createElement('tr');
            const isSelf = u.id === currentUserId;
            tr.innerHTML = `
                <td>${u.id}</td>
                <td>${escapeHtml(u.fullName)}</td>
                <td>${escapeHtml(u.email)}</td>
                <td>${escapeHtml(u.phoneNumber)}</td>
                <td><span class="badge ${u.role === 'ROLE_ADMIN' ? 'badge-land' : 'badge-leaf'}">${escapeHtml(u.role)}</span></td>
                <td>${formatDate(u.createdAt)}</td>
                <td>
                    ${isSelf ? '<span style="color: var(--text-secondary);">You</span>' :
                        `<button class="btn btn-danger delete-user-btn" data-id="${u.id}">🗑 Delete</button>`}
                </td>
            `;
            tbody.appendChild(tr);
        });

        document.querySelectorAll('.delete-user-btn').forEach(btn => {
            btn.onclick = async () => {
                if (!confirm('Delete this user and all of their history? This cannot be undone.')) return;
                try {
                    const res = await fetch(`/api/admin/users/${btn.dataset.id}`, { method: 'DELETE' });
                    const result = await res.json();
                    if (res.ok) {
                        showToast(result.message || 'User deleted.', 'success');
                        loadUsers();
                        loadLandHistory();
                        loadLeafHistory();
                    } else {
                        showToast(result.error || 'Failed to delete user.', 'danger');
                    }
                } catch (err) {
                    showToast('Server connection failure', 'danger');
                }
            };
        });
    } catch (err) {
        showToast('Failed to load users.', 'danger');
    }
}

async function loadLandHistory() {
    try {
        const response = await fetch('/api/admin/history/land');
        if (!response.ok) return;
        const records = await response.json();
        const tbody = document.getElementById('landTableBody');
        tbody.innerHTML = '';

        if (records.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No land analysis records.</td></tr>';
            return;
        }

        records.forEach(rec => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${formatDate(rec.createdAt)}</td>
                <td>${rec.user ? escapeHtml(rec.user.fullName) : 'N/A'}</td>
                <td><span class="badge badge-land">${escapeHtml(rec.cropName)}</span></td>
                <td>${escapeHtml(rec.district)}, ${escapeHtml(rec.state)}</td>
                <td><button class="btn btn-danger delete-land-btn" data-id="${rec.id}">🗑 Delete</button></td>
            `;
            tbody.appendChild(tr);
        });

        document.querySelectorAll('.delete-land-btn').forEach(btn => {
            btn.onclick = async () => {
                if (!confirm('Delete this land analysis record?')) return;
                try {
                    const res = await fetch(`/api/admin/history/land/${btn.dataset.id}`, { method: 'DELETE' });
                    if (res.ok) {
                        showToast('Record deleted.', 'success');
                        loadLandHistory();
                    } else {
                        showToast('Failed to delete record.', 'danger');
                    }
                } catch (err) {
                    showToast('Server connection failure', 'danger');
                }
            };
        });
    } catch (err) {
        showToast('Failed to load land history.', 'danger');
    }
}

async function loadLeafHistory() {
    try {
        const response = await fetch('/api/admin/history/leaf');
        if (!response.ok) return;
        const records = await response.json();
        const tbody = document.getElementById('leafTableBody');
        tbody.innerHTML = '';

        if (records.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No leaf analysis records.</td></tr>';
            return;
        }

        records.forEach(rec => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${formatDate(rec.createdAt)}</td>
                <td>${rec.user ? escapeHtml(rec.user.fullName) : 'N/A'}</td>
                <td><span class="badge badge-leaf">${escapeHtml(rec.analysisType)}</span></td>
                <td>${escapeHtml(rec.disease)}</td>
                <td><button class="btn btn-danger delete-leaf-btn" data-id="${rec.id}">🗑 Delete</button></td>
            `;
            tbody.appendChild(tr);
        });

        document.querySelectorAll('.delete-leaf-btn').forEach(btn => {
            btn.onclick = async () => {
                if (!confirm('Delete this leaf analysis record?')) return;
                try {
                    const res = await fetch(`/api/admin/history/leaf/${btn.dataset.id}`, { method: 'DELETE' });
                    if (res.ok) {
                        showToast('Record deleted.', 'success');
                        loadLeafHistory();
                    } else {
                        showToast('Failed to delete record.', 'danger');
                    }
                } catch (err) {
                    showToast('Server connection failure', 'danger');
                }
            };
        });
    } catch (err) {
        showToast('Failed to load leaf history.', 'danger');
    }
}

async function loadFeedback() {
    try {
        const response = await fetch('/api/admin/feedback');
        if (!response.ok) return;
        const records = await response.json();
        const tbody = document.getElementById('feedbackTableBody');
        tbody.innerHTML = '';

        if (records.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No feedback submissions.</td></tr>';
            return;
        }

        records.forEach(rec => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${formatDate(rec.createdAt)}</td>
                <td>${escapeHtml(rec.name)}</td>
                <td>${escapeHtml(rec.email)}</td>
                <td>${escapeHtml(rec.message)}</td>
                <td><button class="btn btn-danger delete-feedback-btn" data-id="${rec.id}">🗑 Delete</button></td>
            `;
            tbody.appendChild(tr);
        });

        document.querySelectorAll('.delete-feedback-btn').forEach(btn => {
            btn.onclick = async () => {
                if (!confirm('Delete this feedback entry?')) return;
                try {
                    const res = await fetch(`/api/admin/feedback/${btn.dataset.id}`, { method: 'DELETE' });
                    if (res.ok) {
                        showToast('Feedback deleted.', 'success');
                        loadFeedback();
                    } else {
                        showToast('Failed to delete feedback.', 'danger');
                    }
                } catch (err) {
                    showToast('Server connection failure', 'danger');
                }
            };
        });
    } catch (err) {
        showToast('Failed to load feedback.', 'danger');
    }
}

function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleString();
}

function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
