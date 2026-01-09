#!/bin/bash

# Скрипт для запуска всех сервисов проекта
# Использование: ./start-all.sh

set -e  # Останавливаем выполнение при ошибке

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Функция для вывода сообщений
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Проверка необходимых инструментов
check_command() {
    if ! command -v "$1" &> /dev/null; then
        log_error "$1 не найден. Установите $1 и повторите попытку."
        exit 1
    fi
}

log_info "Проверка необходимых инструментов..."
check_command "java"
check_command "mvn"
check_command "docker"

# Функция для проверки доступности порта
wait_for_port() {
    local host=$1
    local port=$2
    local service_name=$3
    local max_attempts=30
    local attempt=0

    log_info "Ожидание запуска $service_name на $host:$port..."
    
    while [ $attempt -lt $max_attempts ]; do
        # Пробуем разные способы проверки порта
        if command -v nc &> /dev/null && nc -z $host $port 2>/dev/null; then
            log_success "$service_name доступен на порту $port"
            return 0
        elif command -v curl &> /dev/null && curl -s --connect-timeout 1 "http://$host:$port" > /dev/null 2>&1; then
            log_success "$service_name доступен на порту $port"
            return 0
        elif timeout 1 bash -c "echo > /dev/tcp/$host/$port" 2>/dev/null; then
            log_success "$service_name доступен на порту $port"
            return 0
        fi
        attempt=$((attempt + 1))
        echo -n "."
        sleep 2
    done
    
    log_warning "$service_name может быть еще не готов, продолжаем..."
    return 0
}

# Функция для проверки доступности HTTP endpoint
wait_for_http() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=0

    log_info "Ожидание запуска $service_name на $url..."
    
    while [ $attempt -lt $max_attempts ]; do
        if command -v curl &> /dev/null && curl -s -f --connect-timeout 2 "$url" > /dev/null 2>&1; then
            log_success "$service_name доступен"
            return 0
        fi
        attempt=$((attempt + 1))
        echo -n "."
        sleep 2
    done
    
    log_error "$service_name не запустился за отведенное время"
    return 1
}

# Шаг 1: Запуск Docker Compose
log_info "========================================="
log_info "Шаг 1: Запуск Docker Compose сервисов"
log_info "========================================="

if command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE_CMD="docker-compose"
elif command -v docker &> /dev/null && docker compose version &> /dev/null; then
    DOCKER_COMPOSE_CMD="docker compose"
else
    log_error "Docker Compose не найден. Установите Docker Desktop."
    exit 1
fi

log_info "Запуск Kafka, Postgres и Kafka UI..."
$DOCKER_COMPOSE_CMD up -d

log_info "Ожидание запуска Docker контейнеров..."
sleep 5

# Проверка доступности Kafka
wait_for_port "localhost" "9092" "Kafka"

# Проверка доступности Postgres
wait_for_port "localhost" "5432" "Postgres"

# Шаг 2: Сборка проекта
log_info "========================================="
log_info "Шаг 2: Сборка проекта"
log_info "========================================="

# Создание директории для логов если её нет
mkdir -p logs

log_info "Сборка родительского проекта..."
mvn clean install -DskipTests -q

# Шаг 3: Запуск Config Server
log_info "========================================="
log_info "Шаг 3: Запуск Config Server"
log_info "========================================="

log_info "Запуск Config Server..."
cd infra/config-server
mvn spring-boot:run > ../../logs/config-server.log 2>&1 &
CONFIG_SERVER_PID=$!
cd ../..

# Ждем запуска Config Server
if wait_for_http "http://localhost:8888/actuator/health" "Config Server"; then
    log_success "Config Server запущен (PID: $CONFIG_SERVER_PID)"
else
    log_error "Config Server не запустился. Проверьте логи: logs/config-server.log"
    exit 1
fi

# Шаг 4: Запуск Collector
log_info "========================================="
log_info "Шаг 4: Запуск Collector"
log_info "========================================="

log_info "Запуск Collector..."
cd telemetry/collector
mvn spring-boot:run > ../../logs/collector.log 2>&1 &
COLLECTOR_PID=$!
cd ../..

log_info "Ожидание запуска Collector..."
sleep 10
log_success "Collector запущен (PID: $COLLECTOR_PID)"

# Шаг 5: Запуск Aggregator
log_info "========================================="
log_info "Шаг 5: Запуск Aggregator"
log_info "========================================="

log_info "Запуск Aggregator..."
cd telemetry/aggregator
mvn spring-boot:run > ../../logs/aggregator.log 2>&1 &
AGGREGATOR_PID=$!
cd ../..

log_info "Ожидание запуска Aggregator..."
sleep 10
log_success "Aggregator запущен (PID: $AGGREGATOR_PID)"

# Шаг 6: Запуск Analyzer
log_info "========================================="
log_info "Шаг 6: Запуск Analyzer"
log_info "========================================="

log_info "Запуск Analyzer..."
cd telemetry/analyzer
mvn spring-boot:run > ../../logs/analyzer.log 2>&1 &
ANALYZER_PID=$!
cd ../..

log_info "Ожидание запуска Analyzer..."
sleep 10
log_success "Analyzer запущен (PID: $ANALYZER_PID)"

# Создание файла с PID процессов для остановки
echo "$CONFIG_SERVER_PID" > logs/pids.txt
echo "$COLLECTOR_PID" >> logs/pids.txt
echo "$AGGREGATOR_PID" >> logs/pids.txt
echo "$ANALYZER_PID" >> logs/pids.txt

# Финальный вывод
log_info "========================================="
log_success "Все сервисы запущены!"
log_info "========================================="
echo ""
log_info "Запущенные сервисы:"
echo "  - Config Server: http://localhost:8888 (PID: $CONFIG_SERVER_PID)"
echo "  - Collector: http://localhost:8080 (PID: $COLLECTOR_PID)"
echo "  - Aggregator: (PID: $AGGREGATOR_PID)"
echo "  - Analyzer: (PID: $ANALYZER_PID)"
echo ""
log_info "Логи находятся в директории: logs/"
echo ""
log_warning "Для остановки всех сервисов используйте: ./stop-all.sh"
echo ""
