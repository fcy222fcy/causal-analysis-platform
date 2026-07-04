#!/bin/bash

# ============================================
# 智慧养殖环境异常溯源与因果分析系统
# 停止所有服务
# ============================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

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

print_header() {
    echo -e "${CYAN}============================================${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}============================================${NC}"
}

# 主函数
main() {
    print_header "停止所有服务"

    # 停止前端服务
    print_info "停止前端服务..."
    pkill -f "vite" 2>/dev/null || true
    print_success "前端服务已停止"

    # 停止因果分析服务
    print_info "停止因果分析服务..."
    pkill -f "python app.py" 2>/dev/null || true
    pkill -f "uvicorn" 2>/dev/null || true
    print_success "因果分析服务已停止"

    # 停止后端服务
    print_info "停止后端服务..."
    pkill -f "spring-boot:run" 2>/dev/null || true
    pkill -f "java.*smart-farming" 2>/dev/null || true
    print_success "后端服务已停止"

    echo ""
    print_success "所有服务已停止"
    echo ""
}

# 运行主函数
main
