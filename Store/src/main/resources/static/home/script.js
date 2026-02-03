/**
 * Contemporary Store - Dashboard do Vendedor
 * Funcionalidades: Autenticação, produtos do vendedor, formatação, logout e mensagens
 */

function formatPrice(price) {
    try {
        const numPrice = typeof price === 'string' ? parseFloat(price) : price;
        if (isNaN(numPrice)) return 'R$ 0,00';
        
        return new Intl.NumberFormat('pt-BR', {
            style: 'currency',
            currency: 'BRL',
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(numPrice);
    } catch (error) {
        console.error('Erro ao formatar preço:', error);
        return `R$ ${price}`;
    }
}

/**
 * Dashboard Manager
 */
class HomePageManager {
    constructor() {
        this.API_BASE_URL = window.location.origin;
        this.allProducts = [];
        this.productsByCategory = {};
        this.elements = {
            productsGrid: document.getElementById('products-grid'),
            totalProducts: document.getElementById('totalProducts'),
            activeProducts: document.getElementById('activeProducts'),
            logoutBtn: document.getElementById('logoutBtn'),
            mobileLogoutBtn: document.getElementById('mobileLogoutBtn')
        };
        this.init();
    }

    init() {
        this.loadSellerProfile();
        this.loadProducts();
        this.setupLogout();
    }

    setupLogout() {
        if (this.elements.logoutBtn) {
            this.elements.logoutBtn.onclick = (e) => {
                e.preventDefault();
                this.handleLogout();
            };
        }
        if (this.elements.mobileLogoutBtn) {
            this.elements.mobileLogoutBtn.onclick = (e) => {
                e.preventDefault();
                this.handleLogout();
            };
        }
    }

    handleLogout() {
    
        localStorage.removeItem('authToken');
        localStorage.removeItem('userType');
        localStorage.removeItem('userName');

        this.showMessage('Logout realizado com sucesso!', 'success');
        setTimeout(() => {
            window.location.href = '/login/login';
        }, 2500);
    }

    showMessage(text, type) {
        let container = document.getElementById('message-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'message-container';
            document.body.appendChild(container);
        }

        container.textContent = text;
        container.className = 'fixed top-4 right-4 z-[100] p-4 rounded-md shadow-lg border transition-all text-sm font-medium animate-bounce';
        
        if (type === 'success') {
            container.classList.add('bg-green-50', 'text-green-800', 'border-green-200');
        } else {
            container.classList.add('bg-red-50', 'text-red-800', 'border-red-200');
        }

        container.classList.remove('hidden');
        setTimeout(() => {
            container.classList.add('hidden');
        }, 3000);
    }

    async loadSellerProfile() {
        const token = localStorage.getItem('authToken');
        try {
            const response = await fetch(`${this.API_BASE_URL}/api/registerseller/me`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                const data = await response.json();
                const sellerEl = document.getElementById('sellerName');
                const welcomeEl = document.getElementById('welcomeName');
                if (sellerEl) sellerEl.textContent = data.nome;
                if (welcomeEl) welcomeEl.textContent = data.nome;
            }
        } catch (error) {
            console.error("Erro ao carregar perfil:", error);
        }
    }

    async loadProducts() {
        const token = localStorage.getItem('authToken');
        try {
            const response = await fetch(`${this.API_BASE_URL}/api/products/my-products`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                const data = await response.json();
                this.allProducts = data.products || [];
                
                // Atualiza contadores
                if (this.elements.totalProducts) this.elements.totalProducts.textContent = this.allProducts.length;
                if (this.elements.activeProducts) this.elements.activeProducts.textContent = this.allProducts.length;

                this.organizeProductsByCategory(this.allProducts);
                this.renderAllCategories();
            }
        } catch (error) {
            console.error("Erro ao carregar produtos:", error);
        }
    }

    organizeProductsByCategory(products) {
        this.productsByCategory = {};
        products.forEach(p => {
            const cat = p.categoria?.toLowerCase() || 'outros';
            if (!this.productsByCategory[cat]) this.productsByCategory[cat] = [];
            this.productsByCategory[cat].push(p);
        });
    }

    renderAllCategories() {
        const grid = this.elements.productsGrid;
        if (!grid) return;
        
        const html = Object.keys(this.productsByCategory).map(cat => {
            return this.productsByCategory[cat].map(p => this.createCard(p)).join('');
        }).join('');

        grid.innerHTML = html || '<p class="col-span-full text-center py-10">Nenhum produto cadastrado.</p>';
    }

    createCard(p) {
        const priceFormatted = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(p.preco || 0);
        const isEsgotado = (p.estoque === 0 || p.estoque === '0' || !p.estoque);
        
        const stockIndicator = isEsgotado 
            ? `<span class="ml-2 text-[10px] text-red-600 font-bold flex items-center inline-flex">
                <svg class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                Esgotado
               </span>`
            : `<span class="ml-2 text-[10px] text-green-600 font-medium flex items-center inline-flex">
                <svg class="w-3 h-3 mr-1" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"></path>
                </svg>
                ${p.estoque} em estoque
               </span>`;

        return `
            <div class="bg-white rounded-lg shadow-sm border p-4 flex flex-col h-full">
                <div class="relative mb-4">
                    <img src="/api/images/product/${p.id}" class="w-full h-40 object-cover rounded" onerror="this.src='/images/placeholder.jpg'">
                    <span class="absolute top-2 right-2 text-[10px] px-2 py-1 bg-white/90 backdrop-blur-sm rounded uppercase font-bold border border-black/5 text-gray-600">
                        ${p.categoria || 'Geral'}
                    </span>
                </div>

                <h3 class="font-bold text-gray-900 text-lg mb-1">${p.nome}</h3>
                <p class="text-sm text-muted-foreground line-clamp-2 mb-4 flex-grow">${p.descricao || ''}</p>
                
                <div class="mb-4">
                    <span class="font-bold text-xl text-gray-900">${priceFormatted}</span>
                    ${stockIndicator}
                </div>

                <div class="grid grid-cols-2 gap-2">
                    <a href="/produtos/editar-produto.html?id=${p.id}" 
                       class="flex items-center justify-center px-4 py-2.5 bg-[#f3f3f5] text-primary text-sm font-semibold rounded-md hover:bg-[#ececf0] transition-all border border-transparent">
                        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-5M18.364 5.636l-3.536 3.536m0 0l-1.414-1.414m1.414 1.414L21 3m-5.636 5.636l1.414-1.414"></path>
                        </svg>
                        Editar
                    </a>

                    <a href="/produtos/detalhes.html?id=${p.id}" 
                       class="flex items-center justify-center px-4 py-2.5 bg-[#030213] text-white text-sm font-semibold rounded-md hover:bg-black transition-all shadow-sm">
                        Detalhes
                    </a>
                </div>
            </div>`;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    setTimeout(() => {
        try { 
            window.homeManager = new HomePageManager(); 
        } catch (e) {
            console.error("Erro ao iniciar Dashboard:", e);
        }
    }, 1000);
});