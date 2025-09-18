# FlowStep Website Deployment Guide

This guide covers different deployment options for the FlowStep website.

## 🚀 Deployment Options

### 1. Netlify (Recommended)

Netlify offers free hosting with automatic deployments from Git.

#### Steps:
1. **Connect Repository**
   - Go to [netlify.com](https://netlify.com)
   - Click "New site from Git"
   - Connect your GitHub/GitLab repository

2. **Configure Build Settings**
   - Build command: (leave empty)
   - Publish directory: `website`
   - Branch to deploy: `main` or `master`

3. **Custom Domain** (Optional)
   - Go to Site settings > Domain management
   - Add custom domain: `flowstep.xrftech.net`
   - Configure DNS records as instructed

4. **SSL Certificate**
   - Automatically provided by Netlify
   - Force HTTPS in site settings

#### Netlify Configuration File
Create `netlify.toml` in the root directory:

```toml
[build]
  publish = "website"

[[headers]]
  for = "/*"
  [headers.values]
    X-Frame-Options = "SAMEORIGIN"
    X-XSS-Protection = "1; mode=block"
    X-Content-Type-Options = "nosniff"
    Referrer-Policy = "strict-origin-when-cross-origin"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

### 2. Vercel

Vercel is another excellent option for static site hosting.

#### Steps:
1. **Import Project**
   - Go to [vercel.com](https://vercel.com)
   - Click "New Project"
   - Import from Git repository

2. **Configure Settings**
   - Root directory: `website`
   - Build command: (leave empty)
   - Output directory: (leave empty)

3. **Custom Domain**
   - Go to project settings
   - Add domain: `flowstep.xrftech.net`
   - Configure DNS as instructed

### 3. GitHub Pages

Free hosting directly from your GitHub repository.

#### Steps:
1. **Repository Setup**
   - Push website files to a GitHub repository
   - Ensure files are in a `website` folder or root

2. **Enable Pages**
   - Go to repository Settings > Pages
   - Source: Deploy from a branch
   - Branch: `main` / `website` folder
   - Save

3. **Custom Domain**
   - Add `CNAME` file with your domain
   - Configure DNS to point to `username.github.io`

### 4. AWS S3 + CloudFront

For enterprise hosting with AWS.

#### Steps:
1. **S3 Bucket Setup**
   ```bash
   aws s3 mb s3://flowstep-website
   aws s3 website s3://flowstep-website --index-document index.html
   ```

2. **Upload Files**
   ```bash
   aws s3 sync website/ s3://flowstep-website --delete
   ```

3. **CloudFront Distribution**
   - Create CloudFront distribution
   - Origin: S3 bucket
   - Default root object: `index.html`
   - Enable compression

4. **SSL Certificate**
   - Request certificate in AWS Certificate Manager
   - Add to CloudFront distribution

### 5. Traditional Web Hosting

For shared hosting or VPS.

#### Steps:
1. **Upload Files**
   - Upload all files from `website/` folder to your web root
   - Ensure `index.html` is in the root directory

2. **Configure Server**
   - Upload `.htaccess` file (for Apache)
   - Configure Nginx (see below)

#### Nginx Configuration
```nginx
server {
    listen 80;
    listen [::]:80;
    server_name flowstep.xrftech.net;
    
    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name flowstep.xrftech.net;
    
    # SSL Configuration
    ssl_certificate /path/to/certificate.crt;
    ssl_certificate_key /path/to/private.key;
    
    # Document root
    root /var/www/flowstep;
    index index.html;
    
    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    
    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/javascript
        application/xml+rss
        application/json;
    
    # Cache static assets
    location ~* \.(css|js|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
    
    # Handle SPA routing (if needed)
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

## 🔧 DNS Configuration

For custom domain `flowstep.xrftech.net`:

### A Records (if using IP)
```
Type: A
Name: @
Value: YOUR_SERVER_IP
TTL: 300

Type: A
Name: www
Value: YOUR_SERVER_IP
TTL: 300
```

### CNAME Records (if using hosting service)
```
Type: CNAME
Name: @
Value: your-site.netlify.app
TTL: 300

Type: CNAME
Name: www
Value: your-site.netlify.app
TTL: 300
```

## 📊 Performance Optimization

### 1. Image Optimization
- Use WebP format where possible
- Compress images (TinyPNG, ImageOptim)
- Add responsive images with `srcset`

### 2. Code Optimization
- Minify CSS and JavaScript
- Remove unused CSS
- Optimize font loading

### 3. CDN Configuration
- Use CDN for external libraries
- Enable gzip compression
- Set proper cache headers

## 🔒 Security Checklist

- [ ] HTTPS enabled
- [ ] Security headers configured
- [ ] CSP (Content Security Policy) implemented
- [ ] Regular dependency updates
- [ ] Error pages configured
- [ ] Monitoring and logging setup

## 📈 Analytics Setup

### Google Analytics 4
1. Create GA4 property
2. Add tracking code to `index.html`:
```html
<!-- Google tag (gtag.js) -->
<script async src="https://www.googletagmanager.com/gtag/js?id=GA_MEASUREMENT_ID"></script>
<script>
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());
  gtag('config', 'GA_MEASUREMENT_ID');
</script>
```

### Plausible Analytics (Privacy-friendly)
```html
<script defer data-domain="flowstep.xrftech.net" src="https://plausible.io/js/script.js"></script>
```

## 🧪 Testing

### Pre-deployment Testing
```bash
# Test locally
python -m http.server 8000

# Check all links
# Use tools like broken-link-checker

# Validate HTML
# Use W3C Markup Validator

# Test performance
# Use Lighthouse or PageSpeed Insights

# Test mobile responsiveness
# Use browser dev tools
```

### Post-deployment Testing
- [ ] All pages load correctly
- [ ] All links work
- [ ] Forms submit properly
- [ ] Mobile responsiveness
- [ ] Performance scores (Lighthouse)
- [ ] SSL certificate valid
- [ ] Analytics tracking works

## 🚨 Troubleshooting

### Common Issues

**404 Errors**
- Check file paths and case sensitivity
- Verify server configuration
- Ensure index.html is in the correct location

**CSS/JS Not Loading**
- Check MIME types
- Verify file permissions
- Check for CORS issues

**Performance Issues**
- Enable compression
- Optimize images
- Use CDN for external resources
- Minimize HTTP requests

**SSL Issues**
- Verify certificate installation
- Check certificate chain
- Force HTTPS redirects

## 📞 Support

For deployment issues:
1. Check hosting provider documentation
2. Review server logs
3. Test in browser developer tools
4. Contact hosting support if needed

---

**Happy Deploying! 🚀**