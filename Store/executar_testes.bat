@echo off
echo [1/2] Iniciando testes em modo Headless via Docker...
docker-compose up --build --abort-on-container-exit --exit-code-from robot-tests

echo.
echo [2/2] Docker finalizado. Iniciando validacao visual do Cadastro de Produto...
:: Aqui rodamos apenas a pasta ou arquivo especifico com interface aberta
robot -d ./results --variable HEADLESS:false --variable TEST_URL:http://localhost:8085/login/login.html tests/testescadastro_prod
pause