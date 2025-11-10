# Building and Using Local Maestro with Changes

This guide shows how to build and use your local version of Maestro that includes the `flt-semantics-identifier` support.

## Prerequisites

- **Java 17** (required by Maestro)
- **Gradle** (included via wrapper)

Check your Java version:
```bash
java -version
# Should show version 17 or higher
```

If you need to install Java 17:
```bash
# macOS (using Homebrew)
brew install openjdk@17

# Or download from: https://adoptium.net/
```

---

## Method 1: Install Locally (Recommended)

This method replaces your system-wide Maestro installation with your local build.

### Step 1: Build and Install

```bash
cd /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main

# Make the install script executable (if needed)
chmod +x installLocally.sh

# Build and install
./installLocally.sh
```

This script:
1. Builds the project using Gradle
2. Removes the old `~/.maestro/bin` and `~/.maestro/lib` directories
3. Copies the new build to `~/.maestro/`

### Step 2: Verify Installation

```bash
# Check Maestro is using the local version
maestro --version

# Test with our example
cd /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main/e2e/workspaces/flutter_web_identifier
python3 -m http.server 8080 &

# In another terminal
maestro test test_flutter_identifier.yaml
```

---

## Method 2: Use Build Directly (Without Installing)

This keeps your system Maestro separate from your local build.

### Step 1: Build the Project

```bash
cd /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main

# Build using Gradle wrapper
./gradlew :maestro-cli:installDist
```

This creates the build at: `maestro-cli/build/install/maestro/`

### Step 2: Run Using the Local Build

```bash
# Use the local maestro binary directly
/Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main/maestro-cli/build/install/maestro/bin/maestro test <your-test.yaml>

# Or create an alias for convenience
alias maestro-local="/Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main/maestro-cli/build/install/maestro/bin/maestro"

# Then use it
maestro-local test test_flutter_identifier.yaml
```

---

## Method 3: Create a Wrapper Script

Create a script to easily switch between versions:

```bash
cat > /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main/maestro-local << 'EOF'
#!/bin/bash
MAESTRO_HOME="/Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main/maestro-cli/build/install/maestro"
"$MAESTRO_HOME/bin/maestro" "$@"
EOF

chmod +x /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main/maestro-local
```

Now you can run:
```bash
./maestro-local test <your-test.yaml>
```

---

## Verifying Your Changes Are Active

To confirm your `flt-semantics-identifier` changes are working:

### Test 1: Check the maestro-web.js file is included

```bash
# Check if the modified JavaScript is in the build
cd /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main

# Extract and check the maestro-web.js from the JAR
jar -xf maestro-cli/build/install/maestro/lib/maestro-client-*.jar maestro-web.js

# Look for our change
grep "flt-semantics-identifier" maestro-web.js
```

You should see:
```javascript
const fltSemanticsId = node.getAttribute ? node.getAttribute('flt-semantics-identifier') : null
```

### Test 2: Run the Test

```bash
cd /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main/e2e/workspaces/flutter_web_identifier

# Start server
python3 -m http.server 8080 &
SERVER_PID=$!

# Update test file to use localhost
sed -i.bak 's|file:///${PWD}/test_app.html|http://localhost:8080/test_app.html|' test_flutter_identifier.yaml

# Run test
maestro test test_flutter_identifier.yaml

# Kill server
kill $SERVER_PID

# Restore original
mv test_flutter_identifier.yaml.bak test_flutter_identifier.yaml
```

If the test passes, your changes are working! ✅

### Test 3: Add Debug Logging

You can add temporary logging to verify:

```javascript
// In maestro-web.js, add after line 97:
console.log('Found flt-semantics-identifier:', fltSemanticsId);
```

Then rebuild and check browser console during test.

---

## Troubleshooting

### Issue: Build fails with "Could not find or load main class"
**Solution**: Ensure Java 17 is set:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew :maestro-cli:installDist
```

### Issue: Changes not appearing
**Solution**: Clean and rebuild:
```bash
./gradlew clean
./gradlew :maestro-cli:installDist
./installLocally.sh
```

### Issue: Tests still can't find elements by identifier
**Solution**: Verify the JS file is updated:
```bash
# Check the source file
grep "flt-semantics-identifier" maestro-client/src/main/resources/maestro-web.js

# Check the built JAR
cd maestro-cli/build/install/maestro/lib
jar -tf maestro-client-*.jar | grep maestro-web.js
jar -xf maestro-client-*.jar maestro-web.js
grep "flt-semantics-identifier" maestro-web.js
```

### Issue: Permission denied on installLocally.sh
**Solution**: Make it executable:
```bash
chmod +x installLocally.sh
./installLocally.sh
```

---

## Quick Test Script

Here's a complete script to build and test:

```bash
#!/bin/bash
set -e

echo "🔨 Building Maestro..."
cd /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main
./gradlew :maestro-cli:installDist

echo "📦 Installing locally..."
./installLocally.sh

echo "✅ Verifying installation..."
maestro --version

echo "🧪 Running test..."
cd e2e/workspaces/flutter_web_identifier

# Start server
python3 -m http.server 8080 &
SERVER_PID=$!
sleep 2

# Update test config
sed -i.bak 's|file:///${PWD}/test_app.html|http://localhost:8080/test_app.html|' test_flutter_identifier.yaml

# Run test
maestro test test_flutter_identifier.yaml
TEST_RESULT=$?

# Cleanup
kill $SERVER_PID
mv test_flutter_identifier.yaml.bak test_flutter_identifier.yaml

if [ $TEST_RESULT -eq 0 ]; then
  echo "✅ Test passed! Your changes are working!"
else
  echo "❌ Test failed. Check the logs above."
fi

exit $TEST_RESULT
```

Save this as `test_local_build.sh` and run:
```bash
chmod +x test_local_build.sh
./test_local_build.sh
```

---

## Summary

**Quickest method:**
```bash
cd /Users/vivekyadav/Documents/Projects/externals/maestro/Maestro-main
./installLocally.sh
maestro test e2e/workspaces/flutter_web_identifier/test_flutter_identifier.yaml
```

This builds and installs your local version, then you can use `maestro` command normally with all your changes! 🎉

