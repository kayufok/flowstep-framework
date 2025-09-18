# FlowStep Framework Website

A modern, responsive website for the FlowStep Spring Boot Starter framework.

## 🌟 Features

- **Modern Design**: Clean, professional design with smooth animations
- **Responsive**: Fully responsive design that works on all devices
- **Interactive**: Interactive code examples and copy-to-clipboard functionality
- **SEO Optimized**: Proper meta tags and Open Graph tags for social sharing
- **Accessibility**: Keyboard navigation support and semantic HTML
- **Performance**: Optimized loading with preloading and lazy loading

## 🚀 Quick Start

### Local Development

1. Clone or download the website files
2. Open `index.html` in your browser
3. Or serve with a local server:

```bash
# Using Python
python -m http.server 8000

# Using Node.js (http-server)
npx http-server

# Using PHP
php -S localhost:8000
```

### Deployment

The website is built with vanilla HTML, CSS, and JavaScript - no build process required!

#### Deploy to Netlify
1. Connect your GitHub repository to Netlify
2. Set build command: (leave empty)
3. Set publish directory: `/website`
4. Deploy!

#### Deploy to Vercel
1. Connect your GitHub repository to Vercel
2. Set root directory: `website`
3. Deploy!

#### Deploy to GitHub Pages
1. Push the `website` folder to your repository
2. Go to repository Settings > Pages
3. Set source to the branch containing the website folder
4. Your site will be available at `https://username.github.io/repository-name`

#### Deploy to Custom Domain
1. Upload all files from the `website` folder to your web server
2. Configure your domain to point to the server
3. Ensure HTTPS is enabled

## 📁 File Structure

```
website/
├── index.html          # Main HTML file
├── styles.css          # All CSS styles
├── script.js           # JavaScript functionality
├── assets/             # Static assets (create as needed)
│   ├── favicon.ico     # Favicon
│   └── og-image.png    # Open Graph image for social sharing
└── README.md           # This file
```

## 🎨 Customization

### Colors
The website uses a consistent color scheme defined in CSS custom properties. Main colors:
- Primary: `#667eea` (gradient to `#764ba2`)
- Text: `#1a1a1a`
- Background: `#ffffff`
- Gray tones: Various shades for subtle elements

### Fonts
- Primary font: Inter (loaded from Google Fonts)
- Fallback: System fonts (-apple-system, BlinkMacSystemFont, etc.)

### Icons
- Font Awesome 6.4.0 for all icons
- Brand icons for GitHub, Java, etc.

## 🔧 Configuration

### Analytics
To add Google Analytics or other tracking:

1. Add your tracking script to the `<head>` section of `index.html`
2. Update the `trackEvent` function in `script.js` to use your analytics provider

### SEO
Update the meta tags in the `<head>` section:
- Title and description
- Open Graph tags
- Twitter Card tags
- Canonical URL

### Content Updates
- Hero section: Update text in the `.hero` section
- Features: Modify the `.features-grid` items
- Code examples: Update the code blocks in the examples section
- Documentation links: Update URLs in the documentation section

## 🌐 Browser Support

- Chrome 60+
- Firefox 60+
- Safari 12+
- Edge 79+

## 📱 Mobile Optimization

The website is fully responsive with breakpoints at:
- Desktop: 1024px+
- Tablet: 768px - 1023px
- Mobile: 320px - 767px

## ⚡ Performance

- Lazy loading for images
- Preloading of critical resources
- Minified external dependencies via CDN
- Efficient CSS with minimal repaints
- Debounced scroll events

## 🎯 Key Sections

1. **Navigation**: Fixed navigation with smooth scrolling
2. **Hero**: Eye-catching introduction with key stats
3. **Features**: Six key features with icons and descriptions
4. **Quick Start**: Tabbed interface for different Spring Boot versions
5. **Examples**: Interactive code examples with copy functionality
6. **Documentation**: Links to all documentation resources
7. **Footer**: Additional links and social media

## 🔒 Security

- All external resources loaded via HTTPS
- No inline scripts (CSP-friendly)
- Sanitized user interactions

## 📊 Analytics Events

The website tracks these events (when analytics is configured):
- Code copied
- External link clicks
- Tab switches
- Page views

## 🤝 Contributing

To contribute to the website:

1. Fork the repository
2. Make your changes
3. Test on multiple devices and browsers
4. Submit a pull request

## 📄 License

This website is part of the FlowStep framework and is licensed under the MIT License.

## 🆘 Support

For website issues:
- Open an issue in the main FlowStep repository
- Check browser console for JavaScript errors
- Verify all external resources are loading correctly

---

**Made with ❤️ for the FlowStep community**