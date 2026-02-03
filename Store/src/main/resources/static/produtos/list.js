document.addEventListener('DOMContentLoaded', () => {
    if (checkAuth()) {
        loadMyProducts();
        setupLogout();
        setupEditFormListener();
    }
});

/**
 * Validação de segurança: Verifica token e se o usuário é vendedor (case-insensitive)
 */
function checkAuth() {
  const token = localStorage.getItem('authToken');
  const userType = (localStorage.getItem('userType') || '').trim().toUpperCase();

  if (!token || userType !== 'SELLER') {
    console.error("Acesso negado: vendedor não identificado.");
    localStorage.clear();
    window.location.href = '/login/login';
    return false;
  }
  return true;
}


/**
 * Carrega os produtos do vendedor através da API
 */
async function loadMyProducts() {
    const grid = document.getElementById('productsGrid');
    const loading = document.getElementById('loadingState');
    const empty = document.getElementById('emptyState');
    const token = localStorage.getItem('authToken');

    if (!grid) return;

    grid.innerHTML = '';
    if (loading) loading.classList.remove('hidden');
    if (empty) empty.classList.add('hidden');

    try {
        const response = await fetch('/api/products/my-products', {
            headers: { 
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            if (response.status === 401) throw new Error("Sessão expirada. Faça login novamente.");
            throw new Error("Não foi possível carregar seus produtos.");
        }

        const data = await response.json();
        const products = data.products || [];

        if (loading) loading.classList.add('hidden');

        if (products.length === 0) {
            if (empty) empty.classList.remove('hidden');
            return;
        }

        // Renderiza os cards
        grid.innerHTML = products.map(product => createProductCard(product)).join('');

    } catch (error) {
        console.error('Erro técnico:', error);
        if (loading) loading.classList.add('hidden');
        grid.innerHTML = `
            <div class="col-span-full text-center py-12">
                <p class="text-red-500 font-medium mb-4">${error.message}</p>
                <button onclick="loadMyProducts()" class="px-4 py-2 bg-gray-200 rounded-md text-sm hover:bg-gray-300 transition-colors">
                    Tentar Novamente
                </button>
            </div>`;
    }
}

/**
 * Gera o template HTML do card.
 * AQUI ESTÁ A CORREÇÃO: Usamos o endpoint de imagem da API em vez do caminho do banco.
 */
function createProductCard(product) {
    const formatPrice = (p) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(p || 0);
    
    // CORREÇÃO: Ignoramos product.imagemUrl (que vem com erro) e usamos o padrão que funciona na Home
    const imageUrl = `/api/images/product/${product.id}`;
    
    const stockColor = product.estoque > 0? 'text-green-600' : 'text-red-600';
    const stockLabel = product.estoque > 0? `✅ ${product.estoque} em estoque` : '❌ Esgotado';

    return `
        <div class="bg-white rounded-lg shadow-sm border border-border overflow-hidden hover:shadow-md transition-all duration-300">
            <div class="relative h-48 bg-gray-100">
                <img src="${imageUrl}" 
                     alt="${product.nome}" 
                     class="w-full h-48 object-cover" 
                     onerror="this.src='/images/placeholder.jpg'">
                <span class="absolute top-2 right-2 bg-white/90 backdrop-blur px-2 py-1 rounded text-xs font-semibold shadow-sm uppercase">
                    ${product.categoria || 'Geral'}
                </span>
            </div>
            <div class="p-4">
                <h3 class="font-semibold text-lg mb-1 truncate text-gray-800">${product.nome}</h3>
                <p class="text-gray-500 text-sm mb-3 line-clamp-2 h-10">${product.descricao || 'Sem descrição.'}</p>
                
                <div class="flex justify-between items-end mb-4">
                    <span class="text-xl font-bold text-primary">${formatPrice(product.preco)}</span>
                    <span class="text-xs font-medium ${stockColor}">${stockLabel}</span>
                </div>
                
                <div class="flex gap-2">
                    <button onclick="editProduct(${product.id})" class="flex-1 bg-gray-100 py-2 rounded text-sm font-medium hover:bg-gray-200 transition-colors">
                        Editar
                    </button>
                    <button onclick="viewDetails(${product.id})" class="flex-1 bg-black text-white py-2 rounded text-sm font-medium hover:bg-gray-800 transition-colors">
                        Detalhes
                    </button>
                </div>
            </div>
        </div>`;
}

// --- FUNÇÕES AUXILIARES ---

async function editProduct(id) {
    const token = localStorage.getItem('authToken');
    try {
        const response = await fetch(`/api/products/${id}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const p = await response.json();
        
        document.getElementById('edit-id').value = p.id;
        document.getElementById('edit-nome').value = p.nome;
        document.getElementById('edit-preco').value = p.preco;
        document.getElementById('edit-estoque').value = p.estoque;
        document.getElementById('edit-descricao').value = p.descricao || '';
        document.getElementById('edit-categoria').value = p.categoria || '';
        
        document.getElementById('editModal').classList.remove('hidden');
    } catch (e) { alert('Erro ao buscar dados.'); }
}

function setupEditFormListener() {
    const form = document.getElementById('editForm');
    if (!form) return;
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('edit-id').value;
        const token = localStorage.getItem('authToken');
        const data = {
            nome: document.getElementById('edit-nome').value,
            descricao: document.getElementById('edit-descricao').value,
            preco: parseFloat(document.getElementById('edit-preco').value),
            estoque: parseInt(document.getElementById('edit-estoque').value),
            categoria: document.getElementById('edit-categoria').value
        };
        const res = await fetch(`/api/products/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify(data)
        });
        if (res.ok) {
            alert('Atualizado!');
            document.getElementById('editModal').classList.add('hidden');
            loadMyProducts();
        }
    });
}

function closeEditModal() { document.getElementById('editModal').classList.add('hidden'); }
function viewDetails(id) { window.location.href = `/produtos/detalhes.html?id=${id}`; }

function setupLogout() {
    const btn = document.getElementById('logoutBtn');
    if (btn) {
        btn.onclick = () => {
            localStorage.clear();
            window.location.href = '/login/login';
        };
    }
}