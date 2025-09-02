# GraalVM Polyglot Demo — Java Encryption Library

This project shows how to build a **Java library** (`DummyEncryptor`) and call it from multiple languages using **GraalVM Polyglot**.

---

## 🍏 Installing GraalVM on macOS

To run this demo on macOS, you need **GraalVM CE JDK 21** with Python/Node.js components.

### 1. Download GraalVM
- Go to [GraalVM Downloads](https://www.graalvm.org/downloads/)
- Choose **GraalVM JDK 21 Community** for your architecture:
    - **Intel Mac** → `x64` build
    - **Apple Silicon (M1/M2/M3)** → `aarch64` build
- Extract to a folder, e.g. `/Library/Java/JavaVirtualMachines/graalvm-jdk-21`

### 2. Set Environment Variables
Add to your shell config (`~/.zshrc` or `~/.bashrc`):

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-jdk-21/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
```

Reload:
```bash
source ~/.zshrc
```

Verify:
```bash
java -version
```
Expected:
```
openjdk version "21.0.x"
GraalVM Community ...
```

### 3. Install Extra Languages
Use GraalVM’s updater `gu`:

```bash
gu install python nodejs
```

Check installed components:
```bash
gu list
```

You should now see:
```
Installed Components:
   GraalVM Core
   Python
   Node.js
```

### 4. Run the Demo
Build the Java lib:
```bash
cd java-lib
mvn clean package
```

Then run:
```bash
graalpy --jvm --polyglot -cp java-lib/target/crypto-lib-1.0.0.jar use_crypto.py
node --jvm --polyglot -cp java-lib/target/crypto-lib-1.0.0.jar useCrypto.js
```

---

## 🌍 Polyglot Usage

See examples in:
- `use_crypto.py` (Python)
- `useCrypto.js` (Node.js)
- `main.go` (Go — experimental)
