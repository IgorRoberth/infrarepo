/**
 * Contemporary Login - JavaScript Separado
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('JavaScript externo carregado!');

    const loginForm = document.getElementById('loginForm');
    let currentUserType = 'customer';
    let isLoading = false;

    // Elementos DOM
    const customerBtn = document.getElementById('customerBtn');
    const sellerBtn = document.getElementById('sellerBtn');
    const submitText = document.getElementById('submitText');
    const registerLink = document.getElementById('registerLink');
    const togglePassword = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    const mobileMenu = document.getElementById('mobileMenu');
    const messageContainer = document.getElementById('messageContainer');
    const messageContent = document.getElementById('messageContent');

    // Verificar se usuário já está logado
    checkExistingAuth();

    // Toggle Cliente/Vendedor
    customerBtn?.addEventListener('click', function() {
        if (isLoading) return;
        currentUserType = 'customer';
        updateUserTypeUI();
    });

    sellerBtn?.addEventListener('click', function() {
        if (isLoading) return;
        currentUserType = 'seller';
        updateUserTypeUI();
    });

    function updateUserTypeUI() {
        customerBtn?.classList.remove('bg-white', 'text-primary', 'shadow-sm');
        customerBtn?.classList.add('text-muted-foreground');
        sellerBtn?.classList.remove('bg-white', 'text-primary', 'shadow-sm');
        sellerBtn?.classList.add('text-muted-foreground');

        if (currentUserType === 'customer') {
            customerBtn?.classList.add('bg-white', 'text-primary', 'shadow-sm');
            customerBtn?.classList.remove('text-muted-foreground');
            submitText.textContent = 'Entrar como Cliente';
            registerLink.textContent = 'Criar conta como Cliente';
        } else {
            sellerBtn?.classList.add('bg-white', 'text-primary', 'shadow-sm');
            sellerBtn?.classList.remove('text-muted-foreground');
            submitText.textContent = 'Entrar como Vendedor';
            registerLink.textContent = 'Criar conta como Vendedor';
        }
    }

    // Toggle senha
    togglePassword?.addEventListener('click', function() {
        const type = passwordInput.type === 'password' ? 'text' : 'password';
        passwordInput.type = type;
    });

    // Menu mobile
    mobileMenuBtn?.addEventListener('click', function() {
        mobileMenu?.classList.toggle('hidden');
    });

    // Form submission
    loginForm?.addEventListener('submit', async function(e) {
        e.preventDefault();

        if (isLoading) return;

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;

        if (!email || !password) {
            showMessage('Por favor, preencha todos os campos.', 'error');
            return;
        }

        if (!isValidEmail(email)) {
            showMessage('Por favor, insira um email válido.', 'error');
            return;
        }

        await performLogin(email, password);
    });

    async function performLogin(email, password) {
        setLoading(true);

        try {
            const endpoint = currentUserType === 'customer' 
                ? '/api/auth/customer/login'
                : '/api/auth/seller/login';
                
            const response = await fetch(endpoint, {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            console.log('Status da resposta:', response.status);

            let data;
            try {
                data = await response.json();
            } catch (e) {
                console.warn('Não foi possível fazer parse do JSON de resposta', e);
            }

        if (!response.ok) {
          const mensagemDoServidor = data.erro || data.message;

        if (mensagemDoServidor) {
          showMessage(mensagemDoServidor, 'error');
        } 
        else if (response.status === 401) {
          showMessage('Credenciais inválidas.', 'error');
     } 
    else {
        showMessage(`Erro inesperado (Status: ${response.status})`, 'error');
    }
    return;
}

            console.log('Dados recebidos:', data);

            if (data && data.token) {
                handleLoginSuccess(data, email);
            } else {
                showMessage((data && data.message) || 'Credenciais inválidas', 'error');
            }
        } catch (error) {
            console.error('Erro detalhado no login:', error);
            showMessage('Erro de conexão. Verifique se o servidor está rodando.', 'error');
        } finally {
            setLoading(false);
        }
    }

    function handleLoginSuccess(data, email) {
        localStorage.setItem('authToken', data.token);

        const userName = data.nome || data.username || data.name || email.split('@')[0];
        localStorage.setItem('userName', userName);
        if (data.userType === 'SELLER') {
            localStorage.setItem('sellerName', userName);
        }

        // Salvar o userType que o backend mandou
        if (data.userType) {
            localStorage.setItem('userType', data.userType.toLowerCase());
        } else {
            // fallback: usa o que estava no front
            localStorage.setItem('userType', currentUserType);
        }

        localStorage.setItem('userEmail', email);

        showMessage('Login realizado com sucesso!', 'success');

        setTimeout(() => {
            if (localStorage.getItem('userType') === 'seller') {
                window.location.href = '/home/index';
            } else {
                error('Página não encontrada.');
            }
        }, 1000);
    }

    function checkExistingAuth() {
        const token = localStorage.getItem('authToken');
        const userType = localStorage.getItem('userType');
        if (token && userType) {
            console.log('Usuário já autenticado, redirecionando...');
            if (userType === 'seller') {
                window.location.href = '/home/index';
            } else {
                window.location.href = '/home/index';
            }
        }
    }

    function setLoading(loading) {
        isLoading = loading;
        const submitBtn = document.getElementById('submitBtn');
        const loadingText = document.getElementById('loadingText');

        if (loading) {
            submitBtn.disabled = true;
            submitText.classList.add('hidden');
            loadingText.classList.remove('hidden');
        } else {
            submitBtn.disabled = false;
            submitText.classList.remove('hidden');
            loadingText.classList.add('hidden');
        }
    }

    function showMessage(message, type) {
        if (!messageContainer || !messageContent) {
            alert(message);
            return;
        }

        messageContent.className = 'p-4 rounded-md text-sm font-medium';

        switch (type) {
            case 'success':
                messageContent.classList.add('bg-green-50', 'text-green-800', 'border', 'border-green-200');
                break;
            case 'error':
                messageContent.classList.add('bg-red-50', 'text-red-800', 'border', 'border-red-200');
                break;
            default:
                messageContent.classList.add('bg-blue-50', 'text-blue-800', 'border', 'border-blue-200');
        }

        messageContent.textContent = message;
        messageContainer.classList.remove('hidden');

        setTimeout(() => messageContainer.classList.add('hidden'), 5000);
    }

    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    updateUserTypeUI();
});