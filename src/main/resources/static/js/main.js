// Persistent Dark Mode
function initDarkMode() {
    const isDark = localStorage.getItem('darkMode') === 'enabled';
    const toggleBtn = document.getElementById('darkModeToggle');
    
    if (isDark) {
        document.body.classList.add('dark-mode');
        if (toggleBtn) toggleBtn.innerHTML = '☀️';
    } else {
        document.body.classList.remove('dark-mode');
        if (toggleBtn) toggleBtn.innerHTML = '🌙';
    }

    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            const body = document.body;
            body.classList.toggle('dark-mode');
            const currentMode = body.classList.contains('dark-mode') ? 'enabled' : 'disabled';
            localStorage.setItem('darkMode', currentMode);
            toggleBtn.innerHTML = currentMode === 'enabled' ? '☀️' : '🌙';
        });
    }
}

// Mobile Menu Toggle
function initMobileMenu() {
    const menuToggle = document.getElementById('menuToggle');
    const navLinks = document.getElementById('navLinks');
    
    if (menuToggle && navLinks) {
        menuToggle.addEventListener('click', () => {
            navLinks.classList.toggle('active');
        });
    }
}

// Set Active Class on Nav Menu
function initActiveNav() {
    const path = window.location.pathname;
    const page = path.split("/").pop();
    const links = document.querySelectorAll('.navbar-links a');
    
    links.forEach(link => {
        const href = link.getAttribute('href');
        if (href === page || (page === '' && href === 'index.html')) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}

// Session Validation (Protected Views)
async function checkAuthentication(requiredRole = 'ROLE_USER') {
    try {
        const response = await fetch('/api/auth/user');
        if (!response.ok) {
            window.location.href = '/login.html';
            return null;
        }
        
        const user = await response.json();
        
        // Check Role
        if (requiredRole === 'ROLE_ADMIN' && user.role !== 'ROLE_ADMIN') {
            window.location.href = '/dashboard.html';
            return null;
        }

        // Show authenticated nav links
        toggleNavAuthElements(user);
        return user;
    } catch (e) {
        window.location.href = '/login.html';
        return null;
    }
}

// Dynamic Nav Display based on Auth State
async function checkAuthNavOnly() {
    try {
        const response = await fetch('/api/auth/user');
        if (response.ok) {
            const user = await response.json();
            toggleNavAuthElements(user);
            return user;
        } else {
            toggleNavAuthElements(null);
            return null;
        }
    } catch (e) {
        toggleNavAuthElements(null);
        return null;
    }
}

function toggleNavAuthElements(user) {
    const authLinks = document.querySelectorAll('.auth-only');
    const guestLinks = document.querySelectorAll('.guest-only');
    const adminLinks = document.querySelectorAll('.admin-only');
    const userNameDisplay = document.getElementById('userNameDisplay');

    if (user) {
        authLinks.forEach(el => el.style.display = 'block');
        guestLinks.forEach(el => el.style.display = 'none');
        
        if (user.role === 'ROLE_ADMIN') {
            adminLinks.forEach(el => el.style.display = 'block');
        } else {
            adminLinks.forEach(el => el.style.display = 'none');
        }
        
        if (userNameDisplay) {
            userNameDisplay.textContent = `Hello, ${user.fullName.split(' ')[0]}`;
        }
    } else {
        authLinks.forEach(el => el.style.display = 'none');
        guestLinks.forEach(el => el.style.display = 'block');
        adminLinks.forEach(el => el.style.display = 'none');
        if (userNameDisplay) userNameDisplay.textContent = '';
    }
}

// Logout Implementation
async function logout() {
    try {
        showLoading(true);
        const response = await fetch('/api/auth/logout', { method: 'POST' });
        showLoading(false);
        if (response.ok) {
            window.location.href = '/index.html';
        } else {
            showToast('Logout failed. Please try again.', 'danger');
        }
    } catch (e) {
        showLoading(false);
        showToast('An error occurred during logout.', 'danger');
    }
}

// Toast Alert Messages
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer') || createToastContainer();
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    
    const icon = type === 'success' ? '🌱' : '⚠️';
    alertDiv.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
    
    container.appendChild(alertDiv);
    
    // Automatically fade out after 4 seconds
    setTimeout(() => {
        alertDiv.style.opacity = '0';
        alertDiv.style.transition = 'opacity 0.5s ease';
        setTimeout(() => alertDiv.remove(), 500);
    }, 4000);
}

function createToastContainer() {
    const div = document.createElement('div');
    div.id = 'toastContainer';
    div.style.position = 'fixed';
    div.style.top = '85px';
    div.style.right = '20px';
    div.style.zIndex = '9999';
    div.style.width = '300px';
    div.style.display = 'flex';
    div.style.flexDirection = 'column';
    div.style.gap = '0.5rem';
    document.body.appendChild(div);
    return div;
}

// Loading Spinner Overlay
function showLoading(show) {
    let overlay = document.getElementById('loadingOverlay');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.id = 'loadingOverlay';
        overlay.className = 'loading-overlay';
        overlay.innerHTML = `
            <div class="spinner"></div>
            <div>Analyzing conditions with Gemini AI... Please wait...</div>
        `;
        document.body.appendChild(overlay);
    }
    
    if (show) {
        overlay.classList.add('active');
    } else {
        overlay.classList.remove('active');
    }
}

// Setup common page features upon loading
document.addEventListener('DOMContentLoaded', () => {
    initDarkMode();
    initMobileMenu();
    initActiveNav();
    
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            logout();
        });
    }
});
