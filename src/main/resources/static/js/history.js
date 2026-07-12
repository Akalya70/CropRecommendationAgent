document.addEventListener('DOMContentLoaded', async () => {
    const user = await checkAuthentication();
    if (!user) return;
    loadHistory();
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

document.getElementById('searchBtn').addEventListener('click', () => {
    loadHistory(document.getElementById('searchInput').value.trim());
});

document.getElementById('searchInput').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        e.preventDefault();
        loadHistory(document.getElementById('searchInput').value.trim());
    }
});

async function loadHistory(query = '') {
    try {
        showLoading(true);
        const url = query ? `/api/history?query=${encodeURIComponent(query)}` : '/api/history';
        const response = await fetch(url);
        showLoading(false);

        if (!response.ok) {
            showToast('Failed to load history.', 'danger');
            return;
        }

        const data = await response.json();
        renderLandTable(data.land || []);
        renderLeafTable(data.leaf || []);
        document.getElementById('landCount').textContent = (data.land || []).length;
        document.getElementById('leafCount').textContent = (data.leaf || []).length;
    } catch (err) {
        showLoading(false);
        showToast('Server connection failure', 'danger');
    }
}

function renderLandTable(records) {
    const tbody = document.getElementById('landTableBody');
    tbody.innerHTML = '';

    if (records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color: var(--text-secondary);">No land analysis records found.</td></tr>';
        return;
    }

    records.forEach(rec => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${formatDate(rec.createdAt)}</td>
            <td><span class="badge badge-land">${escapeHtml(rec.cropName)}</span></td>
            <td>${escapeHtml(rec.district)}, ${escapeHtml(rec.state)}</td>
            <td>${escapeHtml(rec.suitableSeason)}</td>
            <td><button class="btn-icon fav-btn" data-id="${rec.id}" data-type="LAND">${rec.isFavorite ? '⭐' : '☆'}</button></td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-secondary download-btn" data-id="${rec.id}" data-type="LAND">⬇</button>
                    <button class="btn btn-secondary email-btn" data-id="${rec.id}" data-type="LAND">✉</button>
                    <button class="btn btn-danger delete-btn" data-id="${rec.id}" data-type="LAND">🗑</button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });

    attachRowHandlers();
}

function renderLeafTable(records) {
    const tbody = document.getElementById('leafTableBody');
    tbody.innerHTML = '';

    if (records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color: var(--text-secondary);">No leaf analysis records found.</td></tr>';
        return;
    }

    records.forEach(rec => {
        const label = rec.analysisType === 'IMAGE' ? (rec.imageName || 'Uploaded Image') : rec.cropName;
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${formatDate(rec.createdAt)}</td>
            <td><span class="badge badge-leaf">${escapeHtml(rec.analysisType)}</span></td>
            <td>${escapeHtml(label)}</td>
            <td>${escapeHtml(rec.disease)}</td>
            <td><button class="btn-icon fav-btn" data-id="${rec.id}" data-type="LEAF">${rec.isFavorite ? '⭐' : '☆'}</button></td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-secondary download-btn" data-id="${rec.id}" data-type="LEAF">⬇</button>
                    <button class="btn btn-secondary email-btn" data-id="${rec.id}" data-type="LEAF">✉</button>
                    <button class="btn btn-danger delete-btn" data-id="${rec.id}" data-type="LEAF">🗑</button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });

    attachRowHandlers();
}

function attachRowHandlers() {
    document.querySelectorAll('.fav-btn').forEach(btn => {
        btn.onclick = async () => {
            const { id, type } = btn.dataset;
            try {
                const response = await fetch(`/api/history/favorite/${id}?type=${type}`, { method: 'POST' });
                if (response.ok) {
                    loadHistory(document.getElementById('searchInput').value.trim());
                } else {
                    showToast('Failed to update favorite.', 'danger');
                }
            } catch (err) {
                showToast('Server connection failure', 'danger');
            }
        };
    });

    document.querySelectorAll('.download-btn').forEach(btn => {
        btn.onclick = () => {
            const { id, type } = btn.dataset;
            window.location.href = `/api/history/download/${id}?type=${type}`;
        };
    });

    document.querySelectorAll('.email-btn').forEach(btn => {
        btn.onclick = async () => {
            const { id, type } = btn.dataset;
            try {
                showLoading(true);
                const response = await fetch(`/api/history/email/${id}?type=${type}`, { method: 'POST' });
                showLoading(false);
                const result = await response.json();
                if (response.ok) {
                    showToast(result.message || 'Report emailed successfully!', 'success');
                } else {
                    showToast(result.error || 'Failed to email report.', 'danger');
                }
            } catch (err) {
                showLoading(false);
                showToast('Server connection failure', 'danger');
            }
        };
    });

    document.querySelectorAll('.delete-btn').forEach(btn => {
        btn.onclick = async () => {
            const { id, type } = btn.dataset;
            if (!confirm('Are you sure you want to delete this record?')) return;
            try {
                const response = await fetch(`/api/history/${id}?type=${type}`, { method: 'DELETE' });
                if (response.ok) {
                    showToast('Record deleted successfully.', 'success');
                    loadHistory(document.getElementById('searchInput').value.trim());
                } else {
                    showToast('Failed to delete record.', 'danger');
                }
            } catch (err) {
                showToast('Server connection failure', 'danger');
            }
        };
    });
}

function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    const d = new Date(dateStr);
    return d.toLocaleString();
}

function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
