document.addEventListener('DOMContentLoaded', function() {
    
    class ContemporaryUpload {
        constructor() {
            const isDocker = window.location.hostname === 'frontend';
            this.API_BASE_URL = window.location.origin;
            this.isLoading = false;
            this.elements = {};
            this.init();
        }
        
        async init() {
            const isAuthed = await this.checkAuth();
            if (!isAuthed) return;
            this.initializeElements();
            this.setupEventListeners();
            this.setupImageUpload();
        }
        
        async checkAuth() {
            let token = localStorage.getItem('authToken');
            let counter = 0;

            // No Docker, o LocalStorage pode demorar para persistir. Tentamos por 2 segundos.
            while (!token && counter < 4) {
                console.log(`Tentativa ${counter + 1}: Aguardando token no LocalStorage...`);
                await new Promise(r => setTimeout(r, 500));
                token = localStorage.getItem('authToken');
                counter++;
            }

            const userType = (localStorage.getItem('userType') || '').trim().toLowerCase();
            
            if (!token || userType !== 'seller') {
                this.showMessage('Sessão expirada ou inválida. Redirecionando...', 'error');
                setTimeout(() => {
                    window.location.href = '/login/login';
                }, 2000);
                return false;
            }
            return true;
        }
        
        initializeElements() {
            this.elements = {
                productForm: document.getElementById('productForm'),
                nome: document.getElementById('nome'),
                descricao: document.getElementById('descricao'),
                preco: document.getElementById('preco'),
                estoque: document.getElementById('estoque'),
                categoria: document.getElementById('categoria'),
                marca: document.getElementById('marca'),
                sku: document.getElementById('sku'),
                productImage: document.getElementById('productImage'),
                uploadArea: document.getElementById('uploadArea'),
                uploadPlaceholder: document.getElementById('uploadPlaceholder'),
                imagePreview: document.getElementById('imagePreview'),
                previewImg: document.getElementById('previewImg'),
                removeBtn: document.getElementById('removeBtn'),
                submitBtn: document.getElementById('submitBtn'),
                submitText: document.getElementById('submitText'),
                loadingText: document.getElementById('loadingText'),
                cancelBtn: document.getElementById('cancelBtn'),
                logoutBtn: document.getElementById('logoutBtn'),
                messageContainer: document.getElementById('messageContainer'),
                messageContent: document.getElementById('messageContent')
            };
        }
        
        setupEventListeners() {
            const { productForm, cancelBtn, logoutBtn } = this.elements;
            
            if (productForm) {
                productForm.addEventListener('submit', (e) => {
                    e.preventDefault();
                    this.handleSubmit();
                });
            }
            
            if (cancelBtn) {
                cancelBtn.addEventListener('click', () => {
                    this.resetForm();
                });
            }
            
            if (logoutBtn) {
                logoutBtn.addEventListener('click', () => {
                    this.handleLogout();
                });
            }
        }
        
        setupImageUpload() {
            const { productImage, uploadArea, removeBtn } = this.elements;
            
            if (uploadArea && productImage) {
                uploadArea.addEventListener('click', () => {
                    productImage.click();
                });
                
                productImage.addEventListener('change', (e) => {
                    this.handleImagePreview(e.target);
                });
            }
            
            if (removeBtn) {
                removeBtn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    this.removeImage();
                });
            }
        }
        
        handleImagePreview(input) {
            const file = input.files[0];
            const { imagePreview, previewImg, uploadPlaceholder } = this.elements;
            
            if (file) {
                if (!file.type.startsWith('image/')) {
                    this.showMessage('Por favor, selecione apenas arquivos de imagem.', 'error');
                    input.value = '';
                    return;
                }
                
                if (file.size > 5 * 1024 * 1024) {
                    this.showMessage('A imagem deve ter no máximo 5MB.', 'error');
                    input.value = '';
                    return;
                }
                
                const reader = new FileReader();
                reader.onload = function(e) {
                    if (previewImg && imagePreview && uploadPlaceholder) {
                        previewImg.src = e.target.result;
                        uploadPlaceholder.classList.add('hidden');
                        imagePreview.classList.remove('hidden');
                        imagePreview.classList.add('fade-in');
                    }
                };
                reader.readAsDataURL(file);
            }
        }
        
        removeImage() {
            const { productImage, imagePreview, previewImg, uploadPlaceholder } = this.elements;
            
            if (productImage) productImage.value = '';
            if (previewImg) previewImg.src = '';
            if (imagePreview) imagePreview.classList.add('hidden');
            if (uploadPlaceholder) uploadPlaceholder.classList.remove('hidden');
        }
        
    async handleSubmit() {
            if (this.isLoading) return;

            // Garantia extra: pega o token atualizado no momento do clique
            let token = localStorage.getItem('authToken');

            if (!token || token.split('.').length !== 3) {
                this.showMessage('Sessão inválida. Por favor, faça login novamente.', 'error');
                return;
            }

            const formData = this.collectFormData();
            if (!formData) return;

            this.setLoading(true);

            try {
                const response = await fetch(`${this.API_BASE_URL}/api/products/with-image`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    },
                    body: formData
                });

                if (response.ok) {
                    const data = await response.json().catch(() => ({}));
                    this.showMessage(data.message || 'Produto cadastrado com sucesso.', 'success');
                    this.elements.productForm.reset();
                    this.removeImage();
                } else {
                    const errorData = await response.json().catch(() => ({}));
                    this.showMessage(errorData.message || 'Erro no cadastro', 'error');
                }
            } catch (error) {
                console.error(error);
                this.showMessage('Servidor offline. Verifique a conexão.', 'error');
            } finally {
                this.setLoading(false);
            }
        }
        
        collectFormData() {
            const { nome, descricao, preco, estoque, categoria, marca, sku, productImage } = this.elements;
            
            const nomeValue = nome?.value?.trim();
            const descricaoValue = descricao?.value?.trim();
            const precoValue = preco?.value;
            const estoqueValue = estoque?.value;
            const categoriaValue = categoria?.value;
            const marcaValue = marca?.value?.trim();
            const skuValue = sku?.value?.trim();
            const imageFile = productImage?.files?.[0];
            
            if (!nomeValue || !descricaoValue || !precoValue || !estoqueValue || !categoriaValue) {
                this.showMessage('Por favor, preencha todos os campos obrigatórios.', 'error');
                return null;
            }
            
            if (!imageFile) {
                this.showMessage('Por favor, selecione uma imagem para o produto.', 'error');
                return null;
            }
            
            const formData = new FormData();
            formData.append('nome', nomeValue);
            formData.append('descricao', descricaoValue);
            formData.append('preco', precoValue);
            formData.append('estoque', estoqueValue);
            formData.append('categoria', categoriaValue);
            formData.append('marca', marcaValue || '');
            formData.append('sku', skuValue || '');
            formData.append('file', imageFile);
            
            return formData;
        }
        
        resetForm() {
            const { productForm } = this.elements;
            
            if (productForm) {
                productForm.reset();
            }
            
            this.removeImage();
            this.hideMessage();
        }
        
        setLoading(loading) {
            this.isLoading = loading;
            const { submitBtn, submitText, loadingText } = this.elements;
            
            if (submitBtn) {
                submitBtn.disabled = loading;
            }
            
            if (loading) {
                submitText?.classList.add('hidden');
                if (loadingText) {
                    loadingText.classList.remove('hidden');
                    loadingText.innerHTML = '<span class="loading-spinner"></span>Cadastrando...';
                }
            } else {
                submitText?.classList.remove('hidden');
                loadingText?.classList.add('hidden');
            }
        }
        
    showMessage(message, type = 'info') {
    const { messageContainer, messageContent } = this.elements;
    
    if (!messageContainer || !messageContent) {
        alert(message);
        return;
    }
    
    // Define as cores baseadas no tipo para combinar com o padrão do login
    const styles = {
        success: 'bg-green-100 text-green-800 border-green-200',
        error: 'bg-red-100 text-red-800 border-red-200',
        info: 'bg-blue-100 text-blue-800 border-blue-200'
    };

    // Aplica as classes dinamicamente
    messageContent.className = `p-4 rounded-md text-sm font-medium border ${styles[type] || styles.info}`;
    messageContent.textContent = message;
    
    // Mostra o container
    messageContainer.classList.remove('hidden');
    messageContainer.classList.add('fade-in');
            
            if (type !== 'success') {
                setTimeout(() => {
                    this.hideMessage();
                }, 5000);
            }
        }
        
        hideMessage() {
            const { messageContainer } = this.elements;
            if (messageContainer) {
                messageContainer.classList.add('hidden');
            }
        }
        
        handleLogout() {

    if(!localStorage.getItem('authToken')) {
        this.showMessage('Você não está logado', 'error');
        return;
    }
    
    localStorage.removeItem('authToken');
    localStorage.removeItem('userType');
    localStorage.removeItem('userName');
    this.showMessage('Logout realizado com sucesso!', 'success');
    setTimeout(() => {
        window.location.href = '/login/login';
        }, 1500);
    }
}
    //  SISTEMA DE INDICADOR DE USUÁRIO LOGADO
class UserIndicator {
    constructor() {
        this.elements = {
            userContainer: document.getElementById('userContainer'),
            loginBtn: document.getElementById('loginBtn'),
            userBtn: document.getElementById('userBtn'),
            userDropdown: document.getElementById('userDropdown'),
            userInitial: document.getElementById('userInitial'),
            userName: document.getElementById('userName'),
            userType: document.getElementById('userType'),
            logoutBtn: document.getElementById('logoutBtn')
        };
        
        this.init();
    }
    
    init() {
        this.checkAuthStatus();
        this.setupEventListeners();
        
        setInterval(() => {
            this.checkAuthStatus();
        }, 3000);
    }
    
    setupEventListeners() {
        const { loginBtn, userBtn, userDropdown, logoutBtn } = this.elements;
        
        // Clique no botão de login
        if (loginBtn) {
            loginBtn.addEventListener('click', () => {
                window.location.href = '/login/login';
            });
        }
        
        // Toggle dropdown
        if (userBtn) {
            userBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                this.toggleDropdown();
            });
        }
        
        // Fechar dropdown ao clicar fora
        document.addEventListener('click', () => {
            this.closeDropdown();
        });
        
        // Logout
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => {
                this.handleLogout();
            });
        }
    }
    
    checkAuthStatus() {
        const token = localStorage.getItem('authToken');
        const userType = localStorage.getItem('userType');
        const userName = localStorage.getItem('userName');
        
        if (token && this.isValidToken(token)) {
            this.showLoggedInState(userName, userType);
        } else {
            this.showLoggedOutState();
        }
    }
    
    isValidToken(token) {
      try {
         if (!token || token.split('.').length !== 3) return false;
         const payload = JSON.parse(atob(token.split('.')[1]));
         const now = Date.now() / 1000;
         return payload.exp > now;
      } catch (e) {
         return false;
      }
    }
    
    showLoggedInState(userName, userType) {
        document.body.classList.add('user-logged-in');
        document.body.classList.remove('user-logged-out');   
        document.body.classList.add(`user-${userType?.toLowerCase() || 'customer'}`);
        this.updateUserInfo(userName, userType);
        this.updateUserButton(userName);
    }
    
    showLoggedOutState() {
        document.body.classList.add('user-logged-out');
        document.body.classList.remove('user-logged-in');
        document.body.classList.remove('user-seller', 'user-customer');
        
        this.closeDropdown();
    }
    
    updateUserInfo(userName, userType) {
        const { userName: userNameEl, userType: userTypeEl } = this.elements;
        
        if (userNameEl) {
            userNameEl.textContent = userName || 'Usuário';
        }
        
        if (userTypeEl) {
            const typeLabels = {
                'seller': 'Vendedor',
                'customer': 'Cliente'
            };
            userTypeEl.textContent = typeLabels[userType?.toLowerCase()] || 'Usuário';
        }
    }
    
    updateUserButton(userName) {
        const { userInitial, userBtn } = this.elements;
        
        if (userInitial && userName) {
            const initial = userName.charAt(0).toUpperCase();
            userInitial.textContent = initial;
            
            const colors = {
                'A': 'bg-red-500',
                'B': 'bg-blue-500',
                'C': 'bg-green-500',
                'D': 'bg-yellow-500',
                'E': 'bg-purple-500',
                'F': 'bg-pink-500',
                'G': 'bg-indigo-500',
                'H': 'bg-orange-500'
            };
            
            const colorClass = colors[initial] || 'bg-primary';
            
            if (userBtn) {
                userBtn.className = userBtn.className.replace(/bg-\w+-\d+/g, '');
                userBtn.classList.add(colorClass);
            }
        }
    }
    
    toggleDropdown() {
        const { userDropdown } = this.elements;
        
        if (userDropdown) {
            userDropdown.classList.toggle('hidden');
        }
    }
    
    closeDropdown() {
        const { userDropdown } = this.elements;
        
        if (userDropdown) {
            userDropdown.classList.add('hidden');
        }
    }
    
    handleLogout() {
        if (confirm('Tem certeza que deseja sair?')) {
            localStorage.removeItem('authToken');
            localStorage.removeItem('userType');
            localStorage.removeItem('userName');
            
            this.showLoggedOutState();
            this.showMessage('Logout realizado com sucesso!', 'success');
            
            setTimeout(() => {
                window.location.href = '/login/login';
            }, 1500);
        }
    }
    
    showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `fixed top-4 right-4 p-4 rounded-md text-white z-50 ${
            type === 'success' ? 'bg-green-500' : 
            type === 'error' ? 'bg-red-500' : 'bg-blue-500'
        }`;
        toast.textContent = message;
        
        document.body.appendChild(toast);
        
        setTimeout(() => {
            toast.remove();
        }, 3000);
    }
}

// INICIALIZAR QUANDO DOM ESTIVER PRONTO
document.addEventListener('DOMContentLoaded', () => {
    new UserIndicator();
});

// ATUALIZAR APÓS LOGIN BEM-SUCEDIDO
function updateUserIndicatorAfterLogin(userName, userType) {
    localStorage.setItem('userName', userName);
    localStorage.setItem('userType', userType);
    
    // Recarregar indicador
    if (window.userIndicator) {
        window.userIndicator.checkAuthStatus();
    }
}
    
    new ContemporaryUpload();
});
