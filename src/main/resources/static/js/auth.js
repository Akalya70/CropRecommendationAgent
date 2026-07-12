// Registration Handler
const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        // Reset errors
        resetFormErrors();
        const errorAlert = document.getElementById('errorAlert');
        const successAlert = document.getElementById('successAlert');
        errorAlert.style.display = 'none';
        successAlert.style.display = 'none';

        const fullName = document.getElementById('fullName').value.trim();
        const email = document.getElementById('email').value.trim();
        const phoneNumber = document.getElementById('phoneNumber').value.trim();
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        // Basic front-end check
        if (password !== confirmPassword) {
            displayError('confirmPasswordError', 'Passwords do not match');
            return;
        }

        const payload = { fullName, email, phoneNumber, password, confirmPassword };
        
        try {
            showLoading(true);
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            showLoading(false);

            const result = await response.json();
            if (response.ok) {
                successAlert.textContent = "Registration successful! Redirecting to login...";
                successAlert.style.display = 'block';
                registerForm.reset();
                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 2000);
            } else {
                // If it's a validation error mapping, display in fields
                if (response.status === 400 && !result.error) {
                    for (const key in result) {
                        displayError(`${key}Error`, result[key]);
                    }
                } else {
                    errorAlert.textContent = result.error || "Registration failed. Try again.";
                    errorAlert.style.display = 'block';
                }
            }
        } catch (error) {
            showLoading(false);
            errorAlert.textContent = "Server connection lost. Try again later.";
            errorAlert.style.display = 'block';
        }
    });
}

// Login Handler
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        resetFormErrors();
        const errorAlert = document.getElementById('errorAlert');
        errorAlert.style.display = 'none';

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;

        const payload = { email, password };

        try {
            showLoading(true);
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            showLoading(false);

            const result = await response.json();
            if (response.ok && result.success) {
                showToast("Login successful!", "success");
                if (result.role === 'ADMIN') {
                    window.location.href = 'admin.html';
                } else {
                    window.location.href = 'dashboard.html';
                }
            } else {
                errorAlert.textContent = result.error || "Invalid username or password";
                errorAlert.style.display = 'block';
            }
        } catch (error) {
            showLoading(false);
            errorAlert.textContent = "Server connection lost. Try again later.";
            errorAlert.style.display = 'block';
        }
    });
}

// Profile Form Handler
const profileForm = document.getElementById('profileForm');
if (profileForm) {
    // Load current profile details
    document.addEventListener('DOMContentLoaded', async () => {
        const user = await checkAuthentication();
        if (user) {
            document.getElementById('fullName').value = user.fullName;
            document.getElementById('email').value = user.email;
            document.getElementById('phoneNumber').value = user.phoneNumber;
        }
    });

    profileForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        resetFormErrors();
        
        const fullName = document.getElementById('fullName').value.trim();
        const email = document.getElementById('email').value.trim();
        const phoneNumber = document.getElementById('phoneNumber').value.trim();

        const payload = { fullName, email, phoneNumber };

        try {
            showLoading(true);
            const response = await fetch('/api/profile', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            showLoading(false);

            const result = await response.json();
            if (response.ok) {
                showToast("Profile updated successfully!", "success");
                // Update username display in nav
                const nameDisplay = document.getElementById('userNameDisplay');
                if (nameDisplay) nameDisplay.textContent = `Hello, ${result.fullName.split(' ')[0]}`;
            } else {
                if (response.status === 400 && !result.error) {
                    for (const key in result) {
                        displayError(`${key}Error`, result[key]);
                    }
                } else {
                    showToast(result.error || "Update failed", "danger");
                }
            }
        } catch (error) {
            showLoading(false);
            showToast("Server connection failure", "danger");
        }
    });
}

// Password Change Handler
const passwordChangeForm = document.getElementById('passwordChangeForm');
if (passwordChangeForm) {
    passwordChangeForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        resetFormErrors();

        const oldPassword = document.getElementById('oldPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        if (newPassword !== confirmPassword) {
            displayError('confirmPasswordError', 'New passwords do not match');
            return;
        }

        const payload = { oldPassword, newPassword, confirmPassword };

        try {
            showLoading(true);
            const response = await fetch('/api/profile/change-password', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            showLoading(false);

            const result = await response.json();
            if (response.ok) {
                showToast("Password updated successfully!", "success");
                passwordChangeForm.reset();
            } else {
                if (response.status === 400 && !result.error) {
                    for (const key in result) {
                        displayError(`${key}Error`, result[key]);
                    }
                } else {
                    showToast(result.error || "Failed to update password", "danger");
                }
            }
        } catch (error) {
            showLoading(false);
            showToast("Server connection failure", "danger");
        }
    });
}

// Helper functions for forms
function displayError(elementId, message) {
    const errorSpan = document.getElementById(elementId);
    if (errorSpan) {
        errorSpan.textContent = message;
        errorSpan.style.display = 'block';
    }
}

function resetFormErrors() {
    const errors = document.querySelectorAll('.form-error');
    errors.forEach(err => {
        err.textContent = '';
        err.style.display = 'none';
    });
}
