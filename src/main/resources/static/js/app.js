// JavaScript chính cho Coffee Shop

// Khởi tạo khi trang load
document.addEventListener('DOMContentLoaded', function() {
    // Cập nhật số lượng giỏ hàng
    updateCartCount();
    
    // Khởi tạo tooltips Bootstrap
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Fade in animation cho các elements
    const fadeElements = document.querySelectorAll('.fade-in');
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('show');
            }
        });
    });
    
    fadeElements.forEach(el => observer.observe(el));
});

// ============ CART FUNCTIONS ============

// Lấy giỏ hàng từ localStorage
function getCart() {
    return JSON.parse(localStorage.getItem('cart') || '[]');
}

// Lưu giỏ hàng vào localStorage
function saveCart(cart) {
    localStorage.setItem('cart', JSON.stringify(cart));
    updateCartCount();
}

// Thêm sản phẩm vào giỏ hàng
function addToCart(productId, quantity = 1) {
    showLoading('Đang thêm vào giỏ hàng...');
    
    // Gọi API để lấy thông tin sản phẩm
    fetch('/api/products/' + productId)
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể lấy thông tin sản phẩm');
            }
            return response.json();
        })
        .then(product => {
            let cart = getCart();
            
            // Tìm sản phẩm trong giỏ hàng
            let existingItem = cart.find(item => item.id === productId);
            
            if (existingItem) {
                existingItem.quantity += quantity;
            } else {
                cart.push({
                    id: product.id,
                    name: product.name,
                    price: product.price,
                    quantity: quantity,
                    imageUrl: product.imageUrl || '/images/default-product.jpg'
                });
            }
            
            saveCart(cart);
            hideLoading();
            showAlert('Đã thêm "' + product.name + '" vào giỏ hàng!', 'success');
        })
        .catch(error => {
            console.error('Lỗi:', error);
            hideLoading();
            showAlert('Có lỗi xảy ra khi thêm sản phẩm: ' + error.message, 'error');
        });
}

// Xóa sản phẩm khỏi giỏ hàng
function removeFromCart(productId) {
    let cart = getCart();
    cart = cart.filter(item => item.id !== productId);
    saveCart(cart);
    
    // Reload trang giỏ hàng nếu đang ở trang đó
    if (window.location.pathname.includes('/cart')) {
        location.reload();
    }
    
    showAlert('Đã xóa sản phẩm khỏi giỏ hàng!', 'info');
}

// Cập nhật số lượng sản phẩm trong giỏ hàng
function updateCartQuantity(productId, newQuantity) {
    if (newQuantity <= 0) {
        removeFromCart(productId);
        return;
    }
    
    let cart = getCart();
    let item = cart.find(item => item.id === productId);
    
    if (item) {
        item.quantity = newQuantity;
        saveCart(cart);
    }
}

// Cập nhật hiển thị số lượng giỏ hàng
function updateCartCount() {
    let cart = getCart();
    let totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    
    const cartCountElement = document.getElementById('cart-count');
    if (cartCountElement) {
        cartCountElement.textContent = totalItems;
        
        // Thêm animation khi có thay đổi
        cartCountElement.classList.add('animate__animated', 'animate__pulse');
        setTimeout(() => {
            cartCountElement.classList.remove('animate__animated', 'animate__pulse');
        }, 1000);
    }
}

// Tính tổng giá trị giỏ hàng
function getCartTotal() {
    let cart = getCart();
    return cart.reduce((total, item) => total + (item.price * item.quantity), 0);
}

// ============ UI FUNCTIONS ============

// Hiển thị thông báo
function showAlert(message, type = 'info') {
    const alertContainer = document.getElementById('alert-container') || document.body;
    
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${getBootstrapAlertClass(type)} alert-dismissible fade show position-fixed`;
    alertDiv.style.cssText = `
        top: 20px; 
        right: 20px; 
        z-index: 9999; 
        min-width: 300px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    `;
    
    const icon = getAlertIcon(type);
    alertDiv.innerHTML = `
        <i class="${icon} me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    alertContainer.appendChild(alertDiv);
    
    // Tự động ẩn sau 4 giây
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 4000);
}

function getBootstrapAlertClass(type) {
    const classMap = {
        'success': 'success',
        'error': 'danger',
        'warning': 'warning',
        'info': 'info'
    };
    return classMap[type] || 'info';
}

function getAlertIcon(type) {
    const iconMap = {
        'success': 'fas fa-check-circle',
        'error': 'fas fa-exclamation-triangle',
        'warning': 'fas fa-exclamation-circle',
        'info': 'fas fa-info-circle'
    };
    return iconMap[type] || 'fas fa-info-circle';
}

// Hiển thị loading
function showLoading(message = 'Đang xử lý...') {
    let loadingDiv = document.getElementById('global-loading');
    
    if (!loadingDiv) {
        loadingDiv = document.createElement('div');
        loadingDiv.id = 'global-loading';
        loadingDiv.className = 'position-fixed d-flex justify-content-center align-items-center';
        loadingDiv.style.cssText = `
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(0,0,0,0.5);
            z-index: 10000;
        `;
        
        loadingDiv.innerHTML = `
            <div class="bg-white p-4 rounded text-center">
                <div class="loading mb-3"></div>
                <div>${message}</div>
            </div>
        `;
        
        document.body.appendChild(loadingDiv);
    }
}

// Ẩn loading
function hideLoading() {
    const loadingDiv = document.getElementById('global-loading');
    if (loadingDiv) {
        loadingDiv.remove();
    }
}

// ============ FORM FUNCTIONS ============

// Validate form
function validateForm(formElement) {
    const inputs = formElement.querySelectorAll('input[required], select[required], textarea[required]');
    let isValid = true;
    
    inputs.forEach(input => {
        if (!input.value.trim()) {
            isValid = false;
            input.classList.add('is-invalid');
        } else {
            input.classList.remove('is-invalid');
        }
    });
    
    return isValid;
}

// Format số tiền
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Format số
function formatNumber(number) {
    return new Intl.NumberFormat('vi-VN').format(number);
}

// ============ SEARCH FUNCTIONS ============

// Tìm kiếm sản phẩm
function searchProducts(query) {
    const products = document.querySelectorAll('.product-item');
    
    products.forEach(product => {
        const productName = product.querySelector('.product-name')?.textContent.toLowerCase() || '';
        const productDesc = product.querySelector('.product-description')?.textContent.toLowerCase() || '';
        
        if (productName.includes(query.toLowerCase()) || productDesc.includes(query.toLowerCase())) {
            product.style.display = 'block';
        } else {
            product.style.display = 'none';
        }
    });
}

// Filter sản phẩm theo category
function filterByCategory(categoryId) {
    const products = document.querySelectorAll('.product-item');
    
    products.forEach(product => {
        const productCategory = product.dataset.categoryId;
        
        if (categoryId === 'all' || productCategory === categoryId) {
            product.style.display = 'block';
        } else {
            product.style.display = 'none';
        }
    });
}

// ============ UTILITY FUNCTIONS ============

// Debounce function cho search
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Check nếu user đã đăng nhập
function isLoggedIn() {
    // Kiểm tra từ server hoặc session storage
    return document.body.classList.contains('authenticated');
}

// Redirect tới trang login nếu chưa đăng nhập
function requireAuth(callback) {
    if (!isLoggedIn()) {
        showAlert('Vui lòng đăng nhập để tiếp tục!', 'warning');
        setTimeout(() => {
            window.location.href = '/login';
        }, 1500);
        return false;
    }
    
    if (callback) callback();
    return true;
}

// ============ EVENT LISTENERS ============

// Search input
document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(function(e) {
            searchProducts(e.target.value);
        }, 300));
    }
    
    // Category filter
    const categoryButtons = document.querySelectorAll('.category-filter');
    categoryButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const categoryId = this.dataset.categoryId;
            filterByCategory(categoryId);
            
            // Update active state
            categoryButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
        });
    });
});

// Global error handler
window.addEventListener('error', function(e) {
    console.error('JavaScript Error:', e.error);
    showAlert('Có lỗi xảy ra. Vui lòng thử lại!', 'error');
});

// Prevent form double submission
document.addEventListener('submit', function(e) {
    const form = e.target;
    const submitBtn = form.querySelector('button[type="submit"]');
    
    if (submitBtn && !submitBtn.disabled) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="loading me-2"></span>Đang xử lý...';
        
        // Enable lại sau 3 giây để tránh bị stuck
        setTimeout(() => {
            submitBtn.disabled = false;
            submitBtn.innerHTML = submitBtn.dataset.originalText || 'Gửi';
        }, 3000);
    }
});

// Common JavaScript functions for Coffee Shop application

// Global variables
let isAuthenticated = false;
let currentUser = null;

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication status
    checkAuthStatus();
    
    // Update cart count on all pages
    updateCartCount();
    
    // Initialize common features
    initializeAlertSystem();
    initializeScrollToTop();
});

// Check if user is authenticated
async function checkAuthStatus() {
    try {
        // This would typically call an API endpoint to check auth status
        // For now, we'll check if there's user data in session storage
        const userData = sessionStorage.getItem('user');
        if (userData) {
            isAuthenticated = true;
            currentUser = JSON.parse(userData);
        }
    } catch (error) {
        console.log('Not authenticated');
    }
}

// Update cart count badge
function updateCartCount() {
    const cart = JSON.parse(localStorage.getItem('cart') || '[]');
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    
    const cartCountElements = document.querySelectorAll('#cart-count, .cart-count');
    cartCountElements.forEach(element => {
        if (element) {
            element.textContent = totalItems;
            
            // Hide badge if cart is empty
            if (totalItems === 0) {
                element.style.display = 'none';
            } else {
                element.style.display = 'inline';
            }
        }
    });
}

// Add item to cart
function addToCart(product) {
    let cart = JSON.parse(localStorage.getItem('cart') || '[]');
    
    // Check if item already exists
    const existingItemIndex = cart.findIndex(item => item.id === product.id);
    
    if (existingItemIndex > -1) {
        // Increase quantity
        cart[existingItemIndex].quantity += 1;
    } else {
        // Add new item
        cart.push({
            id: product.id,
            name: product.name,
            price: product.price,
            quantity: 1,
            imageUrl: product.imageUrl
        });
    }
    
    // Save to localStorage
    localStorage.setItem('cart', JSON.stringify(cart));
    
    // Update cart count
    updateCartCount();
    
    // Show success message
    showAlert(`Đã thêm ${product.name} vào giỏ hàng!`, 'success');
}

// Remove item from cart
function removeFromCart(productId) {
    let cart = JSON.parse(localStorage.getItem('cart') || '[]');
    cart = cart.filter(item => item.id !== productId);
    localStorage.setItem('cart', JSON.stringify(cart));
    updateCartCount();
    showAlert('Đã xóa sản phẩm khỏi giỏ hàng', 'info');
}

// Clear entire cart
function clearCart() {
    localStorage.removeItem('cart');
    updateCartCount();
    showAlert('Đã xóa tất cả sản phẩm khỏi giỏ hàng', 'info');
}

// Get cart items
function getCartItems() {
    return JSON.parse(localStorage.getItem('cart') || '[]');
}

// Calculate cart total
function getCartTotal() {
    const cart = getCartItems();
    return cart.reduce((total, item) => total + (item.price * item.quantity), 0);
}

// Format currency to Vietnamese Dong
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Format date to Vietnamese format
function formatDate(dateString) {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    }).format(date);
}

// Format datetime to Vietnamese format
function formatDateTime(dateString) {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

// Show alert message
function showAlert(message, type = 'info', duration = 4000) {
    const alertTypes = {
        'success': 'alert-success',
        'error': 'alert-danger',
        'warning': 'alert-warning',
        'info': 'alert-info'
    };
    
    const alertClass = alertTypes[type] || 'alert-info';
    
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert ${alertClass} alert-dismissible fade show position-fixed`;
    alertDiv.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px; max-width: 500px;';
    alertDiv.innerHTML = `
        <div class="d-flex align-items-center">
            <i class="fas ${getAlertIcon(type)} mr-2"></i>
            <span>${message}</span>
            <button type="button" class="close ml-auto" data-dismiss="alert" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
    `;
    
    document.body.appendChild(alertDiv);
    
    // Auto remove after duration
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.classList.remove('show');
            setTimeout(() => {
                if (alertDiv.parentNode) {
                    alertDiv.remove();
                }
            }, 150);
        }
    }, duration);
}

// Get icon for alert type
function getAlertIcon(type) {
    const icons = {
        'success': 'fa-check-circle',
        'error': 'fa-exclamation-circle',
        'warning': 'fa-exclamation-triangle',
        'info': 'fa-info-circle'
    };
    return icons[type] || 'fa-info-circle';
}

// Show loading overlay
function showLoading(message = 'Đang tải...') {
    const loadingDiv = document.createElement('div');
    loadingDiv.id = 'loading-overlay';
    loadingDiv.className = 'position-fixed d-flex align-items-center justify-content-center';
    loadingDiv.style.cssText = 'top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 10000;';
    loadingDiv.innerHTML = `
        <div class="bg-white rounded p-4 text-center">
            <div class="spinner-border text-primary mb-3" role="status">
                <span class="sr-only">Loading...</span>
            </div>
            <div>${message}</div>
        </div>
    `;
    
    document.body.appendChild(loadingDiv);
}

// Hide loading overlay
function hideLoading() {
    const loadingDiv = document.getElementById('loading-overlay');
    if (loadingDiv) {
        loadingDiv.remove();
    }
}

// Debounce function for search inputs
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Validate email format
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// Validate phone number (Vietnamese format)
function isValidPhone(phone) {
    const phoneRegex = /^(\+84|84|0)[3|5|7|8|9][0-9]{8}$/;
    return phoneRegex.test(phone.replace(/\s/g, ''));
}

// Initialize alert system
function initializeAlertSystem() {
    // Auto-close alerts
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('close') || e.target.closest('.close')) {
            const alert = e.target.closest('.alert');
            if (alert) {
                alert.classList.remove('show');
                setTimeout(() => {
                    if (alert.parentNode) {
                        alert.remove();
                    }
                }, 150);
            }
        }
    });
}

// Initialize scroll to top button
function initializeScrollToTop() {
    const backToTopBtn = document.querySelector('.back-to-top');
    
    if (backToTopBtn) {
        // Show/hide button based on scroll position
        window.addEventListener('scroll', function() {
            if (window.pageYOffset > 300) {
                backToTopBtn.style.display = 'block';
            } else {
                backToTopBtn.style.display = 'none';
            }
        });
        
        // Smooth scroll to top
        backToTopBtn.addEventListener('click', function(e) {
            e.preventDefault();
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    }
}

// API Helper functions
const API = {
    // Base URL for API calls
    baseUrl: window.location.origin + '/api',
    
    // GET request
    async get(endpoint) {
        try {
            const response = await fetch(`${this.baseUrl}${endpoint}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    ...this.getAuthHeaders()
                }
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('API GET error:', error);
            throw error;
        }
    },
    
    // POST request
    async post(endpoint, data) {
        try {
            const response = await fetch(`${this.baseUrl}${endpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...this.getAuthHeaders()
                },
                body: JSON.stringify(data)
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('API POST error:', error);
            throw error;
        }
    },
    
    // PUT request
    async put(endpoint, data) {
        try {
            const response = await fetch(`${this.baseUrl}${endpoint}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    ...this.getAuthHeaders()
                },
                body: JSON.stringify(data)
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('API PUT error:', error);
            throw error;
        }
    },
    
    // DELETE request
    async delete(endpoint) {
        try {
            const response = await fetch(`${this.baseUrl}${endpoint}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    ...this.getAuthHeaders()
                }
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('API DELETE error:', error);
            throw error;
        }
    },
    
    // Get authentication headers
    getAuthHeaders() {
        const token = localStorage.getItem('authToken');
        if (token) {
            return {
                'Authorization': `Bearer ${token}`
            };
        }
        return {};
    }
};

// Local Storage helpers
const Storage = {
    // Set item with expiration
    setItem(key, value, expirationMinutes = null) {
        const item = {
            value: value,
            timestamp: new Date().getTime()
        };
        
        if (expirationMinutes) {
            item.expiration = new Date().getTime() + (expirationMinutes * 60 * 1000);
        }
        
        localStorage.setItem(key, JSON.stringify(item));
    },
    
    // Get item with expiration check
    getItem(key) {
        const itemStr = localStorage.getItem(key);
        
        if (!itemStr) {
            return null;
        }
        
        try {
            const item = JSON.parse(itemStr);
            
            // Check if item has expiration and is expired
            if (item.expiration && new Date().getTime() > item.expiration) {
                localStorage.removeItem(key);
                return null;
            }
            
            return item.value;
        } catch (error) {
            // If parsing fails, treat as regular string
            return itemStr;
        }
    },
    
    // Remove item
    removeItem(key) {
        localStorage.removeItem(key);
    },
    
    // Clear all storage
    clear() {
        localStorage.clear();
    }
};

// Export functions for use in other scripts
window.CoffeeShop = {
    // Cart functions
    addToCart,
    removeFromCart,
    clearCart,
    getCartItems,
    getCartTotal,
    updateCartCount,
    
    // Utility functions
    formatCurrency,
    formatDate,
    formatDateTime,
    showAlert,
    showLoading,
    hideLoading,
    debounce,
    isValidEmail,
    isValidPhone,
    
    // API helper
    API,
    
    // Storage helper
    Storage,
    
    // Auth functions
    checkAuthStatus,
    isAuthenticated: () => isAuthenticated,
    getCurrentUser: () => currentUser
};
