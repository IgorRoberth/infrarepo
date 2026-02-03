document.addEventListener('DOMContentLoaded', async () => {
    checkAuth();
    
    // Obter ID do produto da URL
    const urlParams = new URLSearchParams(window.location.search);
    const productId = urlParams.get('id');
    
    if (!productId) {
        showMessage('ID do produto não fornecido', 'error');
        setTimeout(() => window.location.href = '/produtos/list', 2000);
        return;
    }
    
    document.getElementById('productId').value = productId;
    
    // Carregar dados do produto
    await loadProductData(productId);
    
    // Configurar listeners
    setupImageUpload();
    setupFormSubmit(productId);
    setupLogout();
});

function checkAuth() {
    const token = localStorage.getItem('authToken');
    const userType = (localStorage.getItem('userType') || '').trim().toLowerCase();
    
    if (!token) {
        window.location.href = '/login/login';
        return;
    }
    
    if (userType !== 'seller') {
        window.location.href = '/login/login';
        return;
    }
}

async function loadProductData(id) {
    const token = localStorage.getItem('authToken');
    
    try {
        const response = await fetch(`/api/products/${id}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (!response.ok) {
            throw new Error('Erro ao carregar produto');
        }
        
        const product = await response.json();
        populateForm(product);
        
    } catch (error) {
        console.error('Erro:', error);
        showMessage('Não foi possível carregar os dados do produto', 'error');
    }
}

function populateForm(product) {
    document.getElementById('nome').value = product.nome || '';
    document.getElementById('descricao').value = product.descricao || '';
    document.getElementById('preco').value = product.preco || '';
    document.getElementById('estoque').value = product.estoque || '';
    document.getElementById('marca').value = product.marca || '';
    document.getElementById('sku').value = product.sku || '';
    
    // Categoria
    const categoriaSelect = document.getElementById('categoria');
    if (product.categoria) {
        // Tentar encontrar a opção correspondente (pode vir como string ou objeto)
        const categoriaValue = typeof product.categoria === 'string' ? product.categoria : product.categoria.codigo;
        
        // Percorrer opções para encontrar match (case insensitive)
        for (let i = 0; i < categoriaSelect.options.length; i++) {
            if (categoriaSelect.options[i].value.toUpperCase() === categoriaValue.toUpperCase()) {
                categoriaSelect.selectedIndex = i;
                break;
            }
        }
    }
    
    // Checkbox Ativo
    document.getElementById('ativo').checked = product.ativo !== false; // Default true se null
    
    // Imagem
    if (product.imagemUrl) {
        const previewImg = document.getElementById('previewImg');
        const uploadPlaceholder = document.getElementById('uploadPlaceholder');
        const imagePreview = document.getElementById('imagePreview');
        
        previewImg.src = product.imagemUrl;
        uploadPlaceholder.classList.add('hidden');
        imagePreview.classList.remove('hidden');
    }
}

function setupImageUpload() {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('productImage');
    const removeBtn = document.getElementById('removeBtn');
    
    // Clique na área abre o seletor de arquivos
    uploadArea.addEventListener('click', (e) => {
        if (e.target !== removeBtn) {
            fileInput.click();
        }
    });
    
    // Drag and drop
    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.classList.add('border-primary');
    });
    
    uploadArea.addEventListener('dragleave', () => {
        uploadArea.classList.remove('border-primary');
    });
    
    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('border-primary');
        
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            handleFileSelect(e.dataTransfer.files[0]);
        }
    });
    
    // Seleção via input
    fileInput.addEventListener('change', (e) => {
        if (e.target.files && e.target.files[0]) {
            handleFileSelect(e.target.files[0]);
        }
    });
    
    // Remover imagem
    removeBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        fileInput.value = '';
        document.getElementById('previewImg').src = '';
        document.getElementById('imagePreview').classList.add('hidden');
        document.getElementById('uploadPlaceholder').classList.remove('hidden');
    });
}

function handleFileSelect(file) {
    // Validar tipo
    if (!file.type.startsWith('image/')) {
        showMessage('Por favor, selecione apenas arquivos de imagem', 'error');
        return;
    }
    
    // Validar tamanho (5MB)
    if (file.size > 5 * 1024 * 1024) {
        showMessage('A imagem deve ter no máximo 5MB', 'error');
        return;
    }
    
    const reader = new FileReader();
    reader.onload = (e) => {
        const previewImg = document.getElementById('previewImg');
        previewImg.src = e.target.result;
        
        document.getElementById('uploadPlaceholder').classList.add('hidden');
        document.getElementById('imagePreview').classList.remove('hidden');
    };
    reader.readAsDataURL(file);
}

function setupFormSubmit(id) {
    const form = document.getElementById('editProductForm');
    
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const submitBtn = document.getElementById('submitBtn');
        const submitText = document.getElementById('submitText');
        const loadingText = document.getElementById('loadingText');
        
        // Estado de loading
        submitBtn.disabled = true;
        submitBtn.classList.add('opacity-70', 'cursor-not-allowed');
        submitText.classList.add('hidden');
        loadingText.classList.remove('hidden');
        
        try {
            const formData = new FormData();
            
            // Campos de texto
            formData.append('nome', document.getElementById('nome').value);
            formData.append('descricao', document.getElementById('descricao').value);
            formData.append('preco', document.getElementById('preco').value);
            formData.append('estoque', document.getElementById('estoque').value);
            formData.append('categoria', document.getElementById('categoria').value);
            formData.append('marca', document.getElementById('marca').value);
            formData.append('sku', document.getElementById('sku').value);
            formData.append('ativo', document.getElementById('ativo').checked);
            
            // Arquivo (se selecionado)
            const fileInput = document.getElementById('productImage');
            if (fileInput.files && fileInput.files[0]) {
                formData.append('file', fileInput.files[0]);
            }
            
            const token = localStorage.getItem('authToken');
            
            const response = await fetch(`/api/products/${id}/with-image`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });
            
            const data = await response.json();
            
            if (response.ok) {
                showMessage(data.message || 'Produto atualizado com sucesso!', 'success');
                // Redirecionar após 1.5s
                setTimeout(() => {
                    window.location.href = '/produtos/list';
                }, 1500);
            } else {
                // Tratamento de erros
                let errorMessage = data.erro || data.message || 'Erro ao atualizar produto';
                
                // Se o erro for de validação (array de erros ou campos específicos)
                if (data.errors && Array.isArray(data.errors)) {
                    errorMessage = data.errors[0].defaultMessage;
                }
                
                showMessage(errorMessage, 'error');
            }
            
        } catch (error) {
            console.error('Erro:', error);
            showMessage('Ocorreu um erro ao processar sua solicitação', 'error');
        } finally {
            // Resetar estado do botão
            submitBtn.disabled = false;
            submitBtn.classList.remove('opacity-70', 'cursor-not-allowed');
            submitText.classList.remove('hidden');
            loadingText.classList.add('hidden');
        }
    });
}

function showMessage(message, type = 'success') {
    const container = document.getElementById('messageContainer');
    const content = document.getElementById('messageContent');
    
    container.classList.remove('hidden');
    content.className = `p-4 rounded-md shadow-lg ${
        type === 'error' ? 'bg-red-100 text-red-800 border border-red-200' : 
        'bg-green-100 text-green-800 border border-green-200'
    }`;
    
    content.textContent = message;
    
    // Auto-hide após 5 segundos
    setTimeout(() => {
        container.classList.add('hidden');
    }, 5000);
}

function setupLogout() {
    document.getElementById('logoutBtn').addEventListener('click', () => {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userType');
        localStorage.removeItem('userName');
        window.location.href = '/login/login.html';
    });
}
