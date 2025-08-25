/**
 * Vietnamese Input Prevention - Claude.ai's Clean Approach
 * Shared JavaScript functions for Coffee Shop authentication forms
 */

// Cleaner Vietnamese input prevention + Real-time filtering
function forceEnglishInput() {
    const englishOnlyFields = document.querySelectorAll('.english-only');
    
    englishOnlyFields.forEach(field => {
        // Prevent composition events (Vietnamese IME) - Claude.ai approach
        field.addEventListener('compositionstart', function(e) {
            e.preventDefault();
            return false;
        });
        
        field.addEventListener('compositionupdate', function(e) {
            e.preventDefault();
            return false;
        });
        
        field.addEventListener('compositionend', function(e) {
            e.preventDefault();
            return false;
        });
        
        // Real-time Vietnamese character filtering
        field.addEventListener('input', function(e) {
            let value = this.value;
            
            // Remove Vietnamese diacritics using normalize() - ChatGPT approach
            const originalLength = value.length;
            value = value.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
            
            // Allow only English letters, numbers, and basic symbols for username/password
            value = value.replace(/[^a-zA-Z0-9@._-]/g, '');
            
            if (value !== this.value) {
                const cursorPosition = this.selectionStart - (originalLength - value.length);
                this.value = value;
                this.setSelectionRange(cursorPosition, cursorPosition);
            }
        });
        
        // Additional focus handler as ChatGPT suggested
        field.addEventListener('focus', function() {
            this.setAttribute('lang', 'en');
            this.style.imeMode = 'disabled';
        });
        
        // Add visual feedback when user tries Vietnamese input
        field.addEventListener('keydown', function(e) {
            if (e.isComposing || e.which === 229) {
                showVietnameseWarning(this);
                e.preventDefault();
                return false;
            }
        });
    });
}

// Show warning when Vietnamese input is attempted
function showVietnameseWarning(inputElement) {
    let warningDiv = inputElement.parentNode.querySelector('.vietnamese-warning');
    
    if (!warningDiv) {
        warningDiv = document.createElement('div');
        warningDiv.className = 'vietnamese-warning';
        warningDiv.innerHTML = '<i class="fas fa-exclamation-triangle me-1"></i>Vui lòng chuyển sang bàn phím Tiếng Anh (Alt+Shift)';
        inputElement.parentNode.appendChild(warningDiv);
    }
    
    warningDiv.classList.add('show');
    
    setTimeout(() => {
        warningDiv.classList.remove('show');
    }, 3000);
}

// Toggle password visibility for multiple fields
function togglePassword(fieldId) {
    const passwordInput = document.getElementById(fieldId);
    const toggleIcon = document.getElementById('toggleIcon' + fieldId.charAt(0).toUpperCase() + fieldId.slice(1));
    
    if (!passwordInput || !toggleIcon) {
        console.error('Password field or toggle icon not found:', fieldId);
        return;
    }
    
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        toggleIcon.className = 'fas fa-eye-slash';
        toggleIcon.title = 'Ẩn mật khẩu';
    } else {
        passwordInput.type = 'password';
        toggleIcon.className = 'fas fa-eye';
        toggleIcon.title = 'Hiện mật khẩu';
    }
}

// Universal alert system
function showAlert(message, type = 'info', duration = 5000) {
    const alertDiv = document.createElement('div');
    const iconMap = {
        'success': 'check-circle',
        'error': 'exclamation-triangle',
        'danger': 'exclamation-triangle',
        'warning': 'exclamation-triangle',
        'info': 'info-circle'
    };
    
    const icon = iconMap[type] || 'info-circle';
    const alertType = type === 'error' ? 'danger' : type;
    
    alertDiv.className = `alert alert-${alertType} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        <i class="fas fa-${icon} me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    // Find the best container for the alert
    const containers = [
        document.querySelector('.card-body'),
        document.querySelector('.container'),
        document.body
    ];
    
    const container = containers.find(c => c !== null);
    if (container) {
        const firstChild = container.firstElementChild;
        if (firstChild && firstChild.tagName !== 'DIV' || !firstChild) {
            container.insertBefore(alertDiv, firstChild);
        } else {
            container.insertBefore(alertDiv, firstChild.nextSibling);
        }
    }
    
    // Auto remove after duration
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, duration);
    
    return alertDiv;
}

// Password strength checker
function checkPasswordStrength(password) {
    const strengthIndicator = document.querySelector('.password-strength');
    if (!strengthIndicator) return;
    
    const progressBar = strengthIndicator.querySelector('.progress-bar');
    const strengthText = strengthIndicator.querySelector('.strength-text');
    
    let score = 0;
    let feedback = [];
    
    // Length check
    if (password.length >= 8) score += 20;
    else if (password.length >= 6) score += 10;
    else feedback.push('Ít nhất 6 ký tự');
    
    // Lowercase check
    if (/[a-z]/.test(password)) score += 20;
    else feedback.push('Chữ thường (a-z)');
    
    // Uppercase check
    if (/[A-Z]/.test(password)) score += 20;
    else feedback.push('Chữ hoa (A-Z)');
    
    // Number check
    if (/[0-9]/.test(password)) score += 20;
    else feedback.push('Số (0-9)');
    
    // Special character check
    if (/[^A-Za-z0-9]/.test(password)) score += 20;
    else feedback.push('Ký tự đặc biệt (!@#$...)');
    
    // Update progress bar
    progressBar.style.width = score + '%';
    
    if (score < 40) {
        progressBar.className = 'progress-bar bg-danger';
        strengthText.textContent = 'Yếu' + (feedback.length ? ' - Thiếu: ' + feedback.slice(0, 2).join(', ') : '');
        strengthText.className = 'strength-text text-danger small';
    } else if (score < 70) {
        progressBar.className = 'progress-bar bg-warning';
        strengthText.textContent = 'Trung bình' + (feedback.length ? ' - Thiếu: ' + feedback.slice(0, 1).join(', ') : '');
        strengthText.className = 'strength-text text-warning small';
    } else if (score < 100) {
        progressBar.className = 'progress-bar bg-info';
        strengthText.textContent = 'Khá';
        strengthText.className = 'strength-text text-info small';
    } else {
        progressBar.className = 'progress-bar bg-success';
        strengthText.textContent = 'Mạnh';
        strengthText.className = 'strength-text text-success small';
    }
    
    // Show/hide indicator
    if (password.length > 0) {
        strengthIndicator.classList.remove('d-none');
    } else {
        strengthIndicator.classList.add('d-none');
    }
    
    return score;
}

// Real-time password matching validation
function setupPasswordMatch(passwordFieldId, confirmPasswordFieldId) {
    const passwordField = document.getElementById(passwordFieldId);
    const confirmPasswordField = document.getElementById(confirmPasswordFieldId);
    
    if (!passwordField || !confirmPasswordField) return;
    
    function checkPasswordMatch() {
        const password = passwordField.value;
        const confirmPassword = confirmPasswordField.value;
        
        if (confirmPassword && password !== confirmPassword) {
            confirmPasswordField.classList.add('is-invalid');
            confirmPasswordField.classList.remove('is-valid');
        } else if (confirmPassword) {
            confirmPasswordField.classList.remove('is-invalid');
            confirmPasswordField.classList.add('is-valid');
        } else {
            confirmPasswordField.classList.remove('is-invalid', 'is-valid');
        }
    }
    
    passwordField.addEventListener('input', checkPasswordMatch);
    confirmPasswordField.addEventListener('input', checkPasswordMatch);
}

// Form submission helper with loading state
function handleFormSubmission(formSelector, validationCallback) {
    const form = document.querySelector(formSelector);
    if (!form) return;
    
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    
    form.addEventListener('submit', function(e) {
        // Custom validation
        if (validationCallback && typeof validationCallback === 'function') {
            const isValid = validationCallback();
            if (!isValid) {
                e.preventDefault();
                return;
            }
        }
        
        // Show loading state
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang xử lý...';
        
        // Reset button state if form submission fails (after 10 seconds)
        setTimeout(() => {
            if (submitBtn.disabled) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalText;
            }
        }, 10000);
    });
}

// Input validation helpers
const ValidationHelpers = {
    isValidEmail: (email) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    },
    
    isValidPhone: (phone) => {
        const phoneRegex = /^[0-9]{10,11}$/;
        return phoneRegex.test(phone.replace(/\D/g, ''));
    },
    
    isStrongPassword: (password) => {
        return password.length >= 6 && /[A-Za-z]/.test(password) && /[0-9]/.test(password);
    },
    
    isValidUsername: (username) => {
        const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/;
        return usernameRegex.test(username);
    }
};

// Initialize all authentication features
function initAuthForms() {
    // Apply Vietnamese input prevention
    forceEnglishInput();
    
    // Setup password strength checker for new passwords
    const newPasswordField = document.querySelector('#password, #newPassword');
    if (newPasswordField) {
        newPasswordField.addEventListener('input', function() {
            checkPasswordStrength(this.value);
        });
    }
    
    // Setup password matching for common field combinations
    setupPasswordMatch('password', 'confirmPassword');
    setupPasswordMatch('newPassword', 'confirmPassword');
    setupPasswordMatch('newPassword', 'confirmNewPassword');
    
    // Add placeholder tooltips for password visibility toggles
    document.querySelectorAll('[onclick^="togglePassword"]').forEach(btn => {
        btn.title = 'Hiện/Ẩn mật khẩu';
    });
    
    console.log('Auth forms initialized with Vietnamese input prevention');
}

// Auto-initialize when DOM is ready
document.addEventListener('DOMContentLoaded', initAuthForms);
