class AuthManager {
    constructor() {
        this.init();
    }

    init() {
        this.checkAuthentication();
    }

    checkAuthentication() {
        const token = localStorage.getItem('authToken');
        const userType = localStorage.getItem('userType');
        const userName = localStorage.getItem('userName');

        if (!token ||!userType || userType.toLowerCase()!== 'seller') {
            this.showUnauthorized();
            return;
        }

        this.validateToken(token)
           .then(isValid => {
                if (isValid) {
                    this.showMainContent(userName);
                } else {
                    this.clearAuthAndShowUnauthorized();
                }
            })
           .catch(() => {
                this.showMainContent(userName);
            });
    }

    async validateToken(token) {
        const BASE_URL = window.location.origin; 
        try {
            const response = await fetch(`${BASE_URL}/api/auth/seller/validate`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
            return response.ok;
        } catch (error) {
            return false;
        }
    }

    clearAuthAndShowUnauthorized() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userType');
        localStorage.removeItem('userName');
        this.showUnauthorized();
    }

    showUnauthorized() {
        const loading = document.getElementById('loadingScreen');
        const unauth = document.getElementById('unauthorizedScreen');
        if (loading) loading.classList.add('hidden');
        if (unauth) unauth.classList.remove('hidden');
    }

    showMainContent(userName) {
        const loading = document.getElementById('loadingScreen');
        const unauth = document.getElementById('unauthorizedScreen');
        const main = document.getElementById('mainContent');
        
        if (loading) loading.classList.add('hidden');
        if (unauth) unauth.classList.add('hidden');
        if (main) main.classList.remove('hidden');
        
        this.initializeDashboard();
    }

    initializeDashboard() {
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) logoutBtn.addEventListener('click', this.logout);

        setTimeout(() => {
            if (typeof HomePageManager!== 'undefined') {
                new HomePageManager();
            }
        }, 300);

        this.loadStats();
    }

    async loadStats() {
        const token = localStorage.getItem('authToken');
        try {
            const response = await fetch(`${window.location.origin}/api/products/my-products`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (response.ok) {
                const data = await response.json();
                const totalEl = document.getElementById('totalProducts');
                const activeEl = document.getElementById('activeProducts');
                if (totalEl && data.statistics) totalEl.textContent = data.statistics.totalProducts || 0;
                if (activeEl && data.statistics) activeEl.textContent = data.statistics.activeProducts || 0;
            }
        } catch (e) {}
    }

    logout() {
        localStorage.clear();
        sessionStorage.clear();
        window.location.href = '/login';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    try {
        new AuthManager();
    } catch (e) {
        const loading = document.getElementById('loadingScreen');
        if (loading) loading.classList.add('hidden');
    }
});