// DOM Content Loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeNavigation();
    initializeTabs();
    initializeCopyButtons();
    initializeScrollAnimations();
    initializeSmoothScrolling();
});

// Navigation functionality
function initializeNavigation() {
    const navToggle = document.querySelector('.nav-toggle');
    const navLinks = document.querySelector('.nav-links');
    
    if (navToggle && navLinks) {
        navToggle.addEventListener('click', function() {
            navLinks.classList.toggle('active');
            navToggle.classList.toggle('active');
        });
    }
    
    // Close mobile menu when clicking on a link
    const navLinksItems = document.querySelectorAll('.nav-link');
    navLinksItems.forEach(link => {
        link.addEventListener('click', function() {
            navLinks.classList.remove('active');
            navToggle.classList.remove('active');
        });
    });
    
    // Navbar scroll effect
    window.addEventListener('scroll', function() {
        const navbar = document.querySelector('.navbar');
        if (window.scrollY > 50) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    });
}

// Tab functionality
function initializeTabs() {
    // Version tabs (Spring Boot 2 vs 3)
    const versionTabButtons = document.querySelectorAll('.tab-btn');
    const versionTabContents = document.querySelectorAll('.tab-content');
    
    versionTabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const targetTab = this.getAttribute('data-tab');
            
            // Remove active class from all buttons and contents
            versionTabButtons.forEach(btn => btn.classList.remove('active'));
            versionTabContents.forEach(content => content.classList.remove('active'));
            
            // Add active class to clicked button and corresponding content
            this.classList.add('active');
            document.getElementById(targetTab).classList.add('active');
        });
    });
    
    // Dependency tabs (Maven vs Gradle)
    const depTabButtons = document.querySelectorAll('.dep-tab-btn');
    const depTabContents = document.querySelectorAll('.dep-tab-content');
    
    depTabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const targetDep = this.getAttribute('data-dep');
            const parentTab = this.closest('.tab-content');
            
            // Remove active class from buttons and contents within the same parent
            parentTab.querySelectorAll('.dep-tab-btn').forEach(btn => btn.classList.remove('active'));
            parentTab.querySelectorAll('.dep-tab-content').forEach(content => content.classList.remove('active'));
            
            // Add active class to clicked button and corresponding content
            this.classList.add('active');
            document.getElementById(targetDep).classList.add('active');
        });
    });
    
    // Example tabs (Query vs Command)
    const exampleTabButtons = document.querySelectorAll('.example-tab-btn');
    const exampleContents = document.querySelectorAll('.example-content');
    
    exampleTabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const targetExample = this.getAttribute('data-example');
            
            // Remove active class from all buttons and contents
            exampleTabButtons.forEach(btn => btn.classList.remove('active'));
            exampleContents.forEach(content => content.classList.remove('active'));
            
            // Add active class to clicked button and corresponding content
            this.classList.add('active');
            document.getElementById(targetExample + '-example').classList.add('active');
        });
    });
}

// Copy to clipboard functionality
function initializeCopyButtons() {
    const copyButtons = document.querySelectorAll('.copy-btn');
    
    copyButtons.forEach(button => {
        button.addEventListener('click', async function() {
            const codeId = this.getAttribute('data-copy');
            const codeElement = document.getElementById(codeId);
            
            if (codeElement) {
                try {
                    const textToCopy = codeElement.textContent || codeElement.innerText;
                    await navigator.clipboard.writeText(textToCopy);
                    
                    // Show success feedback
                    const originalContent = this.innerHTML;
                    this.classList.add('copy-success');
                    
                    // Reset after 2 seconds
                    setTimeout(() => {
                        this.classList.remove('copy-success');
                        this.innerHTML = originalContent;
                    }, 2000);
                    
                } catch (err) {
                    console.error('Failed to copy text: ', err);
                    
                    // Fallback for older browsers
                    const textArea = document.createElement('textarea');
                    textArea.value = codeElement.textContent || codeElement.innerText;
                    document.body.appendChild(textArea);
                    textArea.select();
                    
                    try {
                        document.execCommand('copy');
                        
                        // Show success feedback
                        const originalContent = this.innerHTML;
                        this.classList.add('copy-success');
                        
                        setTimeout(() => {
                            this.classList.remove('copy-success');
                            this.innerHTML = originalContent;
                        }, 2000);
                        
                    } catch (fallbackErr) {
                        console.error('Fallback copy failed: ', fallbackErr);
                    }
                    
                    document.body.removeChild(textArea);
                }
            }
        });
    });
}

// Scroll animations
function initializeScrollAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);
    
    // Observe elements for animation
    const animatedElements = document.querySelectorAll('.feature-card, .doc-card, .section-header');
    animatedElements.forEach(el => {
        observer.observe(el);
    });
    
    // Counter animation for hero stats
    const statNumbers = document.querySelectorAll('.stat-number');
    const statsObserver = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const target = entry.target;
                const finalValue = target.textContent;
                
                // Only animate if it's a number
                if (!isNaN(finalValue) && finalValue !== '') {
                    animateCounter(target, 0, parseInt(finalValue), 1000);
                }
                
                statsObserver.unobserve(target);
            }
        });
    }, observerOptions);
    
    statNumbers.forEach(stat => {
        if (!isNaN(stat.textContent) && stat.textContent !== '') {
            statsObserver.observe(stat);
        }
    });
}

// Counter animation
function animateCounter(element, start, end, duration) {
    const range = end - start;
    const increment = range / (duration / 16);
    let current = start;
    
    const timer = setInterval(() => {
        current += increment;
        if (current >= end) {
            element.textContent = end;
            clearInterval(timer);
        } else {
            element.textContent = Math.floor(current);
        }
    }, 16);
}

// Smooth scrolling for anchor links
function initializeSmoothScrolling() {
    const anchorLinks = document.querySelectorAll('a[href^="#"]');
    
    anchorLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            
            // Skip if it's just "#" or empty
            if (href === '#' || href === '') {
                return;
            }
            
            const target = document.querySelector(href);
            
            if (target) {
                e.preventDefault();
                
                const navbarHeight = document.querySelector('.navbar').offsetHeight;
                const targetPosition = target.getBoundingClientRect().top + window.pageYOffset - navbarHeight - 20;
                
                window.scrollTo({
                    top: targetPosition,
                    behavior: 'smooth'
                });
            }
        });
    });
}

// Utility functions
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Performance optimization for scroll events
const debouncedScrollHandler = debounce(function() {
    // Any scroll-based functionality can be added here
}, 100);

window.addEventListener('scroll', debouncedScrollHandler);

// Keyboard navigation support
document.addEventListener('keydown', function(e) {
    // Escape key closes mobile menu
    if (e.key === 'Escape') {
        const navLinks = document.querySelector('.nav-links');
        const navToggle = document.querySelector('.nav-toggle');
        
        if (navLinks && navLinks.classList.contains('active')) {
            navLinks.classList.remove('active');
            navToggle.classList.remove('active');
        }
    }
});

// Handle focus for accessibility
document.addEventListener('DOMContentLoaded', function() {
    // Add focus styles for keyboard navigation
    const focusableElements = document.querySelectorAll('button, a, input, textarea, select, [tabindex]');
    
    focusableElements.forEach(element => {
        element.addEventListener('focus', function() {
            this.classList.add('keyboard-focus');
        });
        
        element.addEventListener('blur', function() {
            this.classList.remove('keyboard-focus');
        });
        
        element.addEventListener('mousedown', function() {
            this.classList.remove('keyboard-focus');
        });
    });
});

// Preload critical resources
function preloadResources() {
    const criticalResources = [
        'https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism-tomorrow.min.css',
        'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css',
        'https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap'
    ];
    
    criticalResources.forEach(resource => {
        const link = document.createElement('link');
        link.rel = 'preload';
        link.as = 'style';
        link.href = resource;
        document.head.appendChild(link);
    });
}

// Initialize preloading
preloadResources();

// Error handling for external resources
window.addEventListener('error', function(e) {
    console.warn('Resource failed to load:', e.target.src || e.target.href);
}, true);

// Service worker registration (for PWA capabilities)
if ('serviceWorker' in navigator) {
    window.addEventListener('load', function() {
        // Uncomment if you add a service worker
        // navigator.serviceWorker.register('/sw.js')
        //     .then(registration => console.log('SW registered'))
        //     .catch(error => console.log('SW registration failed'));
    });
}

// Analytics and tracking (placeholder)
function trackEvent(eventName, eventData = {}) {
    // Placeholder for analytics tracking
    console.log('Event tracked:', eventName, eventData);
    
    // Example: Google Analytics 4
    // if (typeof gtag !== 'undefined') {
    //     gtag('event', eventName, eventData);
    // }
}

// Track important interactions
document.addEventListener('DOMContentLoaded', function() {
    // Track copy button clicks
    document.querySelectorAll('.copy-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const codeType = this.getAttribute('data-copy');
            trackEvent('code_copied', { code_type: codeType });
        });
    });
    
    // Track external link clicks
    document.querySelectorAll('a[target="_blank"]').forEach(link => {
        link.addEventListener('click', function() {
            trackEvent('external_link_click', { url: this.href });
        });
    });
    
    // Track tab switches
    document.querySelectorAll('.tab-btn, .dep-tab-btn, .example-tab-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const tabType = this.closest('.version-tabs, .dependency-tabs, .example-tabs').className;
            trackEvent('tab_switched', { tab_type: tabType, tab_value: this.textContent });
        });
    });
});