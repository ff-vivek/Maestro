# How to Run the Flutter Web Identifier Test

## Prerequisites

1. **Maestro CLI installed**
   - If not installed, follow: https://maestro.mobile.dev/getting-started/installing-maestro

2. **Chrome/Chromium browser** (for web testing)

## Option 1: Using a Simple HTTP Server (Recommended)

### Step 1: Start a local web server

Choose one of these methods to serve the HTML file:

**Using Python 3:**
```bash
cd /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main/e2e/workspaces/flutter_web_identifier
python3 -m http.server 8080
```

**Using Python 2:**
```bash
cd /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main/e2e/workspaces/flutter_web_identifier
python -m SimpleHTTPServer 8080
```

**Using Node.js (http-server):**
```bash
# Install if needed: npm install -g http-server
cd /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main/e2e/workspaces/flutter_web_identifier
http-server -p 8080
```

**Using PHP:**
```bash
cd /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main/e2e/workspaces/flutter_web_identifier
php -S localhost:8080
```

### Step 2: Update the test file

Open `test_flutter_identifier.yaml` and change the first line to:

```yaml
appId: http://localhost:8080/test_app.html
```

### Step 3: Run the Maestro test

In a **new terminal window** (keep the server running in the first):

```bash
cd /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main
maestro test e2e/workspaces/flutter_web_identifier/test_flutter_identifier.yaml
```

## Option 2: Using file:// Protocol (Direct File Access)

This might have browser security restrictions, but you can try:

```bash
cd /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main

# The test file already uses file:// protocol with PWD variable
maestro test e2e/workspaces/flutter_web_identifier/test_flutter_identifier.yaml
```

## Option 3: Quick Build and Test Script

I can create a simple script to do everything:

```bash
#!/bin/bash
cd /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main/e2e/workspaces/flutter_web_identifier

# Start server in background
python3 -m http.server 8080 &
SERVER_PID=$!

# Wait for server to start
sleep 2

# Run the test (update appId first)
cd /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main
maestro test e2e/workspaces/flutter_web_identifier/test_flutter_identifier.yaml

# Kill the server
kill $SERVER_PID
```

## Expected Test Flow

When the test runs, you should see:

1. ✅ Browser opens with the test page
2. ✅ Fills in username field using `id: "username_field"`
3. ✅ Fills in email field using `id: "email_field"`
4. ✅ Clicks checkbox using `id: "agree_checkbox"`
5. ✅ Clicks submit button using `id: "submit_button"`
6. ✅ Verifies form submission message
7. ✅ Clicks cancel button using `id: "cancel_button"`
8. ✅ Tests tab selection with state verification

## Troubleshooting

### Issue: Test can't find elements
- **Cause**: The JavaScript changes haven't been loaded
- **Solution**: Rebuild Maestro or ensure you're using the modified `maestro-web.js`

### Issue: Browser doesn't open
- **Cause**: Web driver not configured
- **Solution**: Ensure Chrome/Chromium is installed

### Issue: Connection refused
- **Cause**: HTTP server not running
- **Solution**: Make sure the server is running on port 8080

### Issue: File protocol blocked
- **Cause**: Browser security restrictions
- **Solution**: Use Option 1 with HTTP server instead

## Verifying the Implementation

To verify the implementation is working:

1. **Check browser console** - You should see the test page
2. **Maestro logs** - Should show successful element finding by `flt-semantics-identifier`
3. **Test passes** - All assertions should succeed

## Manual Verification

You can also verify manually:

1. Open `test_app.html` in a browser
2. Open browser DevTools (F12)
3. Run in console:
   ```javascript
   // This should return elements with flt-semantics-identifier
   document.querySelectorAll('[flt-semantics-identifier]')
   ```
4. You should see all the semantic elements with identifiers

## Next Steps

After successful testing:

1. Build the full Maestro project to ensure no regressions
2. Try with a real Flutter web application
3. Share the implementation for review/merge

