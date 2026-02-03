#!/bin/bash
# wait-for-mysql.sh - Versão com Java

host="$1"
port="$2"
shift 2
cmd="$@"

echo "Aguardando MySQL em $host:$port..."

# Usar Java para testar conexão
for i in {1..60}; do
    if java -cp /app/target/classes -Djava.net.useSystemProxies=true \
        -Dfile.encoding=UTF-8 \
        -Djava.security.egd=file:/dev/./urandom \
        -jar /app/target/StoreProject-0.0.1-SNAPSHOT.jar \
        --spring.profiles.active=test \
        --spring.main.web-application-type=none \
        --logging.level.root=ERROR \
        --spring.datasource.url=jdbc:mysql://$host:$port/mysql \
        --spring.datasource.username=root \
        --spring.datasource.password=rootpassword \
        --spring.jpa.hibernate.ddl-auto=validate 2>/dev/null; then
        echo "MySQL está disponível após $((i*2)) segundos!"
        break
    fi
    echo "Tentativa $i/60: MySQL ainda não disponível..."
    sleep 2
done

echo "Iniciando aplicação Spring Boot..."
exec $cmd