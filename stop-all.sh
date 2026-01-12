#!/bin/bash

# Скрипт для остановки всех сервисов проекта
# Использование: ./stop-all.sh

set -e

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

log_info "========================================="
log_info "Остановка всех сервисов"
log_info "========================================="

# Остановка Java процессов в обратном порядке запуска
if [ -f "logs/pids.txt" ]; then
    log_info "Остановка Java сервисов в обратном порядке запуска..."
    
    # Читаем PID из файла и останавливаем в обратном порядке
    # (файл уже содержит PID в обратном порядке запуска)
    while IFS= read -r pid; do
        if [ -n "$pid" ]; then
            # Проверка существования процесса (работает в Git Bash)
            if ps -p "$pid" > /dev/null 2>&1 || kill -0 "$pid" 2>/dev/null; then
                log_info "Остановка процесса PID: $pid"
                kill "$pid" 2>/dev/null || kill -9 "$pid" 2>/dev/null || true
                sleep 1
            fi
        fi
    done < logs/pids.txt
    rm -f logs/pids.txt
    log_success "Java сервисы остановлены"
else
    log_warning "Файл с PID процессов не найден. Попытка остановить по имени..."
    # Остановка процессов по имени в правильном порядке (обратном порядку запуска)
    log_info "Остановка Analyzer..."
    pkill -f "analyzer.*spring-boot:run" 2>/dev/null || true
    sleep 1
    log_info "Остановка Aggregator..."
    pkill -f "aggregator.*spring-boot:run" 2>/dev/null || true
    sleep 1
    log_info "Остановка Collector..."
    pkill -f "collector.*spring-boot:run" 2>/dev/null || true
    sleep 1
    log_info "Остановка Gateway..."
    pkill -f "gateway.*spring-boot:run" 2>/dev/null || true
    sleep 1
    log_info "Остановка Delivery..."
    pkill -f "delivery.*spring-boot:run" 2>/dev/null || true
    sleep 1
    log_info "Остановка Payment..."
    pkill -f "payment.*spring-boot:run" 2>/dev/null || true
    sleep 1
    log_info "Остановка Order..."
    pkill -f "order.*spring-boot:run" 2>/dev/null || true
    sleep 1
    log_info "Остановка Shopping Cart..."
    pkill -f "shopping-cart.*spring-boot:run" 2>/dev/null || true
    sleep 1
    log_info "Остановка Shopping Store..."
    pkill -f "shopping-store.*spring-boot:run" 2>/dev/null || true
    sleep 1
    log_info "Остановка Warehouse..."
    pkill -f "warehouse.*spring-boot:run" 2>/dev/null || true
    sleep 1
    log_info "Остановка Config Server..."
    pkill -f "config-server.*spring-boot:run" 2>/dev/null || true
    sleep 1
    log_info "Остановка Eureka Server..."
    pkill -f "discovery-server.*spring-boot:run" 2>/dev/null || true
    # Fallback для Windows
    taskkill //F //FI "WINDOWTITLE eq spring-boot:run*" 2>/dev/null || true
fi

# Остановка Docker Compose
log_info "Остановка Docker Compose сервисов..."
if command -v docker-compose &> /dev/null; then
    docker-compose down
elif command -v docker &> /dev/null && docker compose version &> /dev/null; then
    docker compose down
fi

log_success "Все сервисы остановлены"
