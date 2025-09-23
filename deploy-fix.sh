#!/bin/bash

# Fixed deploy.sh script - corrects the path issue
# This script should be copied to /home/github/mono-kit/ to replace the existing deploy.sh

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
    echo -e "$(date '+%Y-%m-%d %H:%M:%S') $1" | tee -a "$LOG_FILE"
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
        error_exit "Docker Compose file not found: $DOCKER_COMPOSE_FILE"
    fi
    
    if [[ ! -f "$CONFIG_FILE" ]]; then
        error_exit "Configuration file not found: $CONFIG_FILE"
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
    info "Loading configuration from $CONFIG_FILE..."
    
    if [[ -f "$CONFIG_FILE" ]]; then
        # Export variables from config file
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
    
    # Pull latest images
    info "Pulling latest Docker images..."
    if docker_compose_cmd pull; then
        success "Images pulled successfully"
    else
        warning "Failed to pull some images, continuing with existing images"
    fi
    
    # Start services
    info "Starting Docker containers..."
    if docker_compose_cmd up -d; then
        success "Services started successfully"
        
        # Show running containers
        info "Running containers:"
        docker_compose_cmd ps
        
        # Show logs for a few seconds
        info "Recent logs:"
        docker_compose_cmd logs --tail=50
        
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
    
    info "System resources:"
    docker system df
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

# Update services
update_services() {
    info "Updating services..."
    
    # Pull latest images
    docker_compose_cmd pull
    
    # Restart with new images
    docker_compose_cmd up -d --force-recreate
    
    success "Services updated successfully"
}

# Backup data
backup_data() {
    local backup_dir="backups/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$backup_dir"
    
    info "Creating backup in $backup_dir..."
    
    # Backup volumes (customize based on your setup)
    docker_compose_cmd exec -T db mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" --all-databases > "$backup_dir/database.sql" 2>/dev/null || warning "Database backup failed"
    
    # Copy configuration files
    cp -r config* "$backup_dir/" 2>/dev/null || warning "Config backup failed"
    
    success "Backup created in $backup_dir"
}

# Clean up
cleanup() {
    info "Cleaning up..."
    
    # Remove stopped containers
    docker container prune -f
    
    # Remove unused images
    docker image prune -f
    
    # Remove unused volumes (be careful with this)
    # docker volume prune -f
    
    success "Cleanup completed"
}

# Show help
show_help() {
    cat << EOF
Usage: $0 [COMMAND] [OPTIONS]

Commands:
    start           Start all services
    stop            Stop all services  
    restart         Restart all services
    status          Show service status
    logs [service]  Show logs (optionally for specific service)
    update          Update services to latest images
    backup          Backup data
    cleanup         Clean up unused Docker resources
    help            Show this help message

Examples:
    $0 start                    # Start all services
    $0 logs nginx              # Show logs for nginx service
    $0 logs nginx 200          # Show last 200 lines of nginx logs
    $0 status                  # Show current status
    $0 update                  # Update and restart services

EOF
}

# Main script logic
main() {
    local command="$1"
    
    # Create log file
    touch "$LOG_FILE"
    
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
        "update")
            check_requirements
            update_services
            ;;
        "backup")
            backup_data
            ;;
        "cleanup")
            cleanup
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