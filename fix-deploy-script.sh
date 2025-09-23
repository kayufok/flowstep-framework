#!/bin/bash

# Script to fix the deploy.sh path issue
# Run this script from /home/github/mono-kit/ directory

echo "🔧 Fixing deploy.sh path issue..."

# Check if we're in the right directory
if [[ ! -f "docker-compose.yml" ]]; then
    echo "❌ ERROR: docker-compose.yml not found in current directory"
    echo "Please run this script from /home/github/mono-kit/ directory"
    exit 1
fi

# Backup original deploy.sh
if [[ -f "deploy.sh" ]]; then
    echo "📦 Backing up original deploy.sh to deploy.sh.backup"
    cp deploy.sh deploy.sh.backup
fi

# Create the fixed deploy.sh script
cat > deploy.sh << 'EOF'
#!/bin/bash

# Fixed deploy.sh script - uses current directory instead of /workspace/
# Set script directory as the working directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Configuration
CONFIG_FILE="config.env"
DOCKER_COMPOSE_FILE="docker-compose.yml"
LOG_FILE="deploy.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# Error handling
error_exit() {
    log "${RED}ERROR: $1${NC}"
    exit 1
}

# Success message
success() {
    log "${GREEN}SUCCESS: $1${NC}"
}

# Warning message
warning() {
    log "${YELLOW}WARNING: $1${NC}"
}

# Info message
info() {
    log "${BLUE}INFO: $1${NC}"
}

# Check if required files exist
check_requirements() {
    info "Checking requirements..."
    
    if [[ ! -f "$DOCKER_COMPOSE_FILE" ]]; then
        error_exit "Docker Compose file not found: $DOCKER_COMPOSE_FILE (looking in: $(pwd))"
    fi
    
    if [[ ! -f "$CONFIG_FILE" ]]; then
        warning "Configuration file not found: $CONFIG_FILE (looking in: $(pwd))"
    fi
    
    # Check if Docker is installed and running
    if ! command -v docker &> /dev/null; then
        error_exit "Docker is not installed"
    fi
    
    if ! docker info &> /dev/null; then
        error_exit "Docker is not running"
    fi
    
    # Check if Docker Compose is available
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        error_exit "Docker Compose is not available"
    fi
    
    success "All requirements met"
}

# Load configuration
load_config() {
    if [[ -f "$CONFIG_FILE" ]]; then
        info "Loading configuration from $CONFIG_FILE..."
        set -a
        source "$CONFIG_FILE"
        set +a
        success "Configuration loaded"
    else
        warning "Configuration file not found, using defaults"
    fi
}

# Function to use docker-compose or docker compose
docker_compose_cmd() {
    if command -v docker-compose &> /dev/null; then
        docker-compose "$@"
    else
        docker compose "$@"
    fi
}

# Start services
start_services() {
    info "Starting services..."
    
    # Load environment variables
    load_config
    
    # Start services
    info "Starting Docker containers..."
    if docker_compose_cmd up -d; then
        success "Services started successfully"
        
        # Show running containers
        info "Running containers:"
        docker_compose_cmd ps
        
    else
        error_exit "Failed to start services"
    fi
}

# Stop services
stop_services() {
    info "Stopping services..."
    
    if docker_compose_cmd down; then
        success "Services stopped successfully"
    else
        error_exit "Failed to stop services"
    fi
}

# Restart services
restart_services() {
    info "Restarting services..."
    stop_services
    start_services
}

# Show status
show_status() {
    info "Service status:"
    docker_compose_cmd ps
}

# Show logs
show_logs() {
    local service="$2"
    local tail_lines="${3:-100}"
    
    if [[ -n "$service" ]]; then
        info "Showing logs for service: $service"
        docker_compose_cmd logs --tail="$tail_lines" -f "$service"
    else
        info "Showing logs for all services:"
        docker_compose_cmd logs --tail="$tail_lines" -f
    fi
}

# Show help
show_help() {
    cat << 'HELP_EOF'
Usage: ./deploy.sh [COMMAND]

Commands:
    start           Start all services
    stop            Stop all services  
    restart         Restart all services
    status          Show service status
    logs [service]  Show logs (optionally for specific service)
    help            Show this help message

Examples:
    ./deploy.sh start          # Start all services
    ./deploy.sh logs nginx     # Show logs for nginx service
    ./deploy.sh status         # Show current status

HELP_EOF
}

# Main script logic
main() {
    local command="$1"
    
    # Create log file
    touch "$LOG_FILE"
    
    info "Working directory: $(pwd)"
    info "Looking for docker-compose.yml in: $(pwd)/$DOCKER_COMPOSE_FILE"
    
    case "$command" in
        "start")
            check_requirements
            start_services
            ;;
        "stop")
            stop_services
            ;;
        "restart")
            check_requirements
            restart_services
            ;;
        "status")
            show_status
            ;;
        "logs")
            show_logs "$@"
            ;;
        "help"|"--help"|"-h")
            show_help
            ;;
        "")
            warning "No command specified"
            show_help
            ;;
        *)
            error_exit "Unknown command: $command"
            ;;
    esac
}

# Run main function with all arguments
main "$@"
EOF

# Make the script executable
chmod +x deploy.sh

echo "✅ Fixed deploy.sh created successfully!"
echo ""
echo "🚀 You can now run:"
echo "   ./deploy.sh start"
echo ""
echo "📋 Available commands:"
echo "   ./deploy.sh start    - Start services"
echo "   ./deploy.sh stop     - Stop services"
echo "   ./deploy.sh status   - Show status"
echo "   ./deploy.sh logs     - Show logs"
echo "   ./deploy.sh help     - Show help"
echo ""
echo "📁 The script now correctly looks for files in the current directory instead of /workspace/"