#!/bin/bash

# ============================================
# 智慧养殖环境异常溯源与因果分析系统
# Smart Farming Environment Anomaly
# Traceability and Causal Analysis System
# ============================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 服务端口
FRONTEND_PORT=5173
CAUSAL_SERVICE_PORT=5000
BACKEND_PORT=8080

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${CYAN}============================================${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}============================================${NC}"
}

# 检查端口是否被占用
check_port() {
    local port=$1
    local service_name=$2

    if netstat -an | grep -q ":${port}.*LISTENING"; then
        print_warning "端口 $port 已被占用 ($service_name 可能已在运行)"
        return 1
    fi
    return 0
}

# 检查依赖是否安装
check_dependencies() {
    print_header "检查依赖"

    # 检查 Node.js
    if command -v node &> /dev/null; then
        print_success "Node.js: $(node -v)"
    else
        print_error "Node.js 未安装"
        exit 1
    fi

    # 检查 npm
    if command -v npm &> /dev/null; then
        print_success "npm: $(npm -v)"
    else
        print_error "npm 未安装"
        exit 1
    fi

    # 检查 Python
    if command -v python &> /dev/null; then
        print_success "Python: $(python --version)"
    elif command -v python3 &> /dev/null; then
        print_success "Python3: $(python3 --version)"
    else
        print_error "Python 未安装"
        exit 1
    fi

    # 检查 Java
    if command -v java &> /dev/null; then
        print_success "Java: $(java -version 2>&1 | head -n 1)"
    else
        print_error "Java 未安装"
        exit 1
    fi

    # 检查 Maven
    if command -v mvn &> /dev/null; then
        print_success "Maven: $(mvn -v 2>&1 | head -n 1)"
    else
        print_warning "Maven 未安装，将尝试使用 Maven Wrapper"
    fi

    echo ""
}

# 启动前端服务
start_frontend() {
    print_header "启动前端服务"

    cd "$PROJECT_ROOT/frontend"

    # 检查 node_modules 是否存在
    if [ ! -d "node_modules" ]; then
        print_info "安装前端依赖..."
        npm install
    fi

    # 检查端口
    check_port $FRONTEND_PORT "前端服务"

    print_info "启动 Vue.js 开发服务器..."
    npm run dev &
    FRONTEND_PID=$!

    sleep 3
    print_success "前端服务已启动: http://localhost:$FRONTEND_PORT"
    echo ""
}

# 启动因果分析服务
start_causal_service() {
    print_header "启动因果分析服务"

    cd "$PROJECT_ROOT/causal-service"

    # 检查 Python 虚拟环境
    if [ ! -d "venv" ]; then
        print_info "创建 Python 虚拟环境..."
        python -m venv venv
    fi

    # 激活虚拟环境
    source venv/Scripts/activate 2>/dev/null || source venv/bin/activate

    # 检查依赖是否安装
    if ! python -c "import fastapi" 2>/dev/null; then
        print_info "安装因果分析服务依赖..."
        pip install -r requirements.txt
    fi

    # 检查端口
    check_port $CAUSAL_SERVICE_PORT "因果分析服务"

    print_info "启动 FastAPI 服务..."
    python app.py &
    CAUSAL_SERVICE_PID=$!

    sleep 3
    print_success "因果分析服务已启动: http://localhost:$CAUSAL_SERVICE_PORT"
    echo ""
}

# 启动后端服务
start_backend() {
    print_header "启动后端服务"

    cd "$PROJECT_ROOT/smart-farming-backend"

    # 检查端口
    check_port $BACKEND_PORT "后端服务"

    print_info "启动 Spring Boot 服务..."

    # 尝试使用 Maven Wrapper，如果没有则使用系统 Maven
    if [ -f "mvnw" ]; then
        ./mvnw spring-boot:run &
    elif command -v mvn &> /dev/null; then
        mvn spring-boot:run &
    else
        print_error "未找到 Maven，请先安装 Maven"
        exit 1
    fi
    BACKEND_PID=$!

    sleep 5
    print_success "后端服务已启动: http://localhost:$BACKEND_PORT"
    echo ""
}

# 显示服务状态
show_status() {
    print_header "服务状态"

    echo -e "${GREEN}前端服务:${NC}      http://localhost:$FRONTEND_PORT"
    echo -e "${GREEN}因果分析服务:${NC}  http://localhost:$CAUSAL_SERVICE_PORT"
    echo -e "${GREEN}后端服务:${NC}      http://localhost:$BACKEND_PORT"
    echo -e "${GREEN}API 文档:${NC}      http://localhost:$BACKEND_PORT/doc.html"
    echo -e "${GREEN}H2 控制台:${NC}     http://localhost:$BACKEND_PORT/h2-console"
    echo ""

    echo -e "${YELLOW}按 Ctrl+C 停止所有服务${NC}"
    echo ""
}

# 停止所有服务
cleanup() {
    print_header "停止所有服务"

    if [ ! -z "$FRONTEND_PID" ]; then
        print_info "停止前端服务 (PID: $FRONTEND_PID)..."
        kill $FRONTEND_PID 2>/dev/null
    fi

    if [ ! -z "$CAUSAL_SERVICE_PID" ]; then
        print_info "停止因果分析服务 (PID: $CAUSAL_SERVICE_PID)..."
        kill $CAUSAL_SERVICE_PID 2>/dev/null
    fi

    if [ ! -z "$BACKEND_PID" ]; then
        print_info "停止后端服务 (PID: $BACKEND_PID)..."
        kill $BACKEND_PID 2>/dev/null
    fi

    print_success "所有服务已停止"
    exit 0
}

# 主函数
main() {
    print_header "智慧养殖环境异常溯源与因果分析系统"
    echo ""

    # 设置信号处理
    trap cleanup SIGINT SIGTERM

    # 检查依赖
    check_dependencies

    # 启动服务
    start_causal_service
    start_backend
    start_frontend

    # 显示状态
    show_status

    # 等待用户中断
    wait
}

# 运行主函数
main
