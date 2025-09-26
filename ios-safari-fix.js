/**
 * iOS Safari Input Field Fix
 * Solves the issue where keyboard doesn't appear when tapping input fields
 * Also maintains dropdown functionality
 */

class IOSSafariInputFix {
    constructor() {
        this.isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent);
        this.init();
    }

    init() {
        if (this.isIOS) {
            console.log('iOS device detected - applying iOS Safari fixes');
            this.applyIOSFixes();
        }
        
        // Apply fixes to all relevant inputs
        this.fixAllInputs();
        
        // Set up dropdown functionality if needed
        this.setupDropdowns();
    }

    applyIOSFixes() {
        // Prevent zoom on input focus
        document.addEventListener('touchstart', function() {}, { passive: true });
        
        // Handle orientation changes
        window.addEventListener('orientationchange', () => {
            setTimeout(() => {
                window.scrollTo(0, 0);
            }, 500);
        });

        // Add viewport meta tag if not present
        if (!document.querySelector('meta[name="viewport"]')) {
            const viewport = document.createElement('meta');
            viewport.name = 'viewport';
            viewport.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
            document.head.appendChild(viewport);
        }
    }

    fixAllInputs() {
        const inputs = document.querySelectorAll('input[type="text"], input[type="tel"], input[type="email"], input[type="search"], textarea');
        
        inputs.forEach(input => this.fixSingleInput(input));
        
        // Also fix dynamically created inputs
        const observer = new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                mutation.addedNodes.forEach((node) => {
                    if (node.nodeType === Node.ELEMENT_NODE) {
                        const newInputs = node.querySelectorAll('input[type="text"], input[type="tel"], input[type="email"], input[type="search"], textarea');
                        newInputs.forEach(input => this.fixSingleInput(input));
                    }
                });
            });
        });
        
        observer.observe(document.body, { childList: true, subtree: true });
    }

    fixSingleInput(input) {
        // Ensure font size is at least 16px
        const computedStyle = window.getComputedStyle(input);
        const fontSize = parseFloat(computedStyle.fontSize);
        
        if (fontSize < 16) {
            input.style.fontSize = '16px';
        }

        // Remove readonly attribute if present (common cause of keyboard not showing)
        if (input.hasAttribute('readonly')) {
            console.warn('Readonly attribute found on input - this prevents iOS keyboard. Consider using disabled instead if needed.');
        }

        // Ensure proper event handling
        input.addEventListener('focus', (e) => {
            this.handleInputFocus(e);
        });

        input.addEventListener('touchstart', (e) => {
            // Ensure the input can receive focus
            if (e.target.hasAttribute('readonly')) {
                e.target.removeAttribute('readonly');
            }
        });

        // Fix for inputs that might be programmatically focused
        input.addEventListener('click', (e) => {
            // Ensure focus happens as direct result of user action
            setTimeout(() => {
                if (document.activeElement !== e.target) {
                    e.target.focus();
                }
            }, 0);
        });
    }

    handleInputFocus(e) {
        const input = e.target;
        
        // Remove readonly if present
        input.removeAttribute('readonly');
        
        // Ensure input is visible and can receive input
        input.style.pointerEvents = 'auto';
        input.style.userSelect = 'text';
        input.style.webkitUserSelect = 'text';
        
        // Debug logging
        console.log('Input focused:', {
            id: input.id,
            type: input.type,
            readonly: input.hasAttribute('readonly'),
            disabled: input.disabled,
            fontSize: window.getComputedStyle(input).fontSize
        });
    }

    setupDropdowns() {
        // Find inputs that should have dropdown functionality
        const dropdownInputs = document.querySelectorAll('[data-dropdown="true"], .country-input, .phone-input');
        
        dropdownInputs.forEach(input => {
            this.createDropdown(input);
        });
    }

    createDropdown(input) {
        // Create dropdown container if not exists
        let container = input.parentElement;
        if (!container.classList.contains('dropdown-container')) {
            container = document.createElement('div');
            container.className = 'dropdown-container';
            input.parentNode.insertBefore(container, input);
            container.appendChild(input);
        }

        // Create dropdown list
        let dropdown = container.querySelector('.dropdown-list');
        if (!dropdown) {
            dropdown = document.createElement('div');
            dropdown.className = 'dropdown-list';
            container.appendChild(dropdown);
        }

        // Sample country data - replace with your actual data
        const countries = [
            { code: '+852', name: 'Hong Kong', flag: '🇭🇰' },
            { code: '+691', name: 'Micronesia', flag: '🇫🇲' },
            { code: '+692', name: 'Marshall Islands', flag: '🇲🇭' },
            { code: '+850', name: 'North Korea', flag: '🇰🇵' },
            { code: '+853', name: 'Macau', flag: '🇲🇴' }
        ];

        // Set up dropdown functionality
        this.initDropdownEvents(input, dropdown, countries);
    }

    initDropdownEvents(input, dropdown, data) {
        let isOpen = false;
        let selectedIndex = -1;

        // Input event - filter and show dropdown
        input.addEventListener('input', (e) => {
            const value = e.target.value.toLowerCase();
            const filteredData = data.filter(item => 
                item.name.toLowerCase().includes(value) || 
                item.code.includes(value)
            );
            
            this.renderDropdown(dropdown, filteredData);
            this.showDropdown(dropdown);
            isOpen = true;
            selectedIndex = -1;
        });

        // Focus event - show all options
        input.addEventListener('focus', (e) => {
            if (!e.target.value.trim()) {
                this.renderDropdown(dropdown, data);
                this.showDropdown(dropdown);
                isOpen = true;
            }
        });

        // Blur event - hide dropdown with delay
        input.addEventListener('blur', (e) => {
            setTimeout(() => {
                this.hideDropdown(dropdown);
                isOpen = false;
            }, 150);
        });

        // Keyboard navigation
        input.addEventListener('keydown', (e) => {
            if (!isOpen) return;

            const items = dropdown.querySelectorAll('.dropdown-item');
            
            switch (e.key) {
                case 'ArrowDown':
                    e.preventDefault();
                    selectedIndex = Math.min(selectedIndex + 1, items.length - 1);
                    this.highlightItem(items, selectedIndex);
                    break;
                case 'ArrowUp':
                    e.preventDefault();
                    selectedIndex = Math.max(selectedIndex - 1, -1);
                    this.highlightItem(items, selectedIndex);
                    break;
                case 'Enter':
                    e.preventDefault();
                    if (selectedIndex >= 0) {
                        this.selectItem(input, dropdown, items[selectedIndex]);
                        isOpen = false;
                    }
                    break;
                case 'Escape':
                    this.hideDropdown(dropdown);
                    isOpen = false;
                    break;
            }
        });

        // Click on dropdown items
        dropdown.addEventListener('mousedown', (e) => {
            e.preventDefault(); // Prevent blur
        });

        dropdown.addEventListener('click', (e) => {
            const item = e.target.closest('.dropdown-item');
            if (item) {
                this.selectItem(input, dropdown, item);
                isOpen = false;
                input.focus(); // Keep keyboard visible
            }
        });
    }

    renderDropdown(dropdown, data) {
        dropdown.innerHTML = data.map(item => 
            `<div class="dropdown-item" data-code="${item.code}" data-name="${item.name}">
                ${item.flag} ${item.code} ${item.name}
            </div>`
        ).join('');
    }

    showDropdown(dropdown) {
        dropdown.style.display = 'block';
    }

    hideDropdown(dropdown) {
        dropdown.style.display = 'none';
    }

    highlightItem(items, index) {
        items.forEach((item, i) => {
            item.classList.toggle('highlighted', i === index);
        });
    }

    selectItem(input, dropdown, item) {
        const code = item.dataset.code;
        const name = item.dataset.name;
        input.value = `${code} ${name}`;
        this.hideDropdown(dropdown);
    }
}

// Initialize the fix when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        new IOSSafariInputFix();
    });
} else {
    new IOSSafariInputFix();
}

// Export for manual initialization if needed
window.IOSSafariInputFix = IOSSafariInputFix;