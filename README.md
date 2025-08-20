# GraalVM Polyglot Demo — Java Encryption Library

This project shows how to build a **Java library** (`DummyEncryptor`) and call it from multiple languages using **GraalVM Polyglot**.

## 📦 Java Library

`DummyEncryptor.java` implements Base64-based `encrypt` and `decrypt`.

Build:
```bash
cd java-lib
mvn clean package
```
Produces `target/crypto-lib-1.0.0.jar`.

---

## 🌍 Polyglot Usage

| Language   | GraalVM Support | Example Command | Notes |
|------------|-----------------|-----------------|-------|
| **Java**  | Native           | `java -cp target/crypto-lib-1.0.0.jar com.example.crypto.DummyEncryptor` | Baseline implementation |
| **Python (GraalPy)** | ✅ Supported | `graalpy --jvm --polyglot -cp java-lib/target/crypto-lib-1.0.0.jar use_crypto.py` | Full interop with Java classes |
| **Node.js** | ✅ Supported (GraalVM Node.js) | `node --jvm --polyglot -cp java-lib/target/crypto-lib-1.0.0.jar useCrypto.js` | Use `Java.type("...")` |
| **Go** | ⚠️ Experimental (via Graal Polyglot SDK) | `go run main.go` | Works but APIs are not as mature |
| **Ruby** | ✅ Supported | `ruby --jvm --polyglot -cp java-lib/target/crypto-lib-1.0.0.jar use_crypto.rb` | Similar interop |
| **R** | ✅ Supported | `Rscript --jvm --polyglot -cp java-lib/target/crypto-lib-1.0.0.jar use_crypto.R` | Java classes available |
| **C/C++** | ✅ Supported (via LLVM) | `lli --jvm --polyglot -cp java-lib/target/crypto-lib-1.0.0.jar program.bc` | Compile to LLVM bitcode first |
| **C# / .NET** | ❌ Not supported natively | N/A | Use REST/gRPC or JNI bridge |

---

## 🐍 Python Example

```bash
graalpy --jvm --polyglot -cp java-lib/target/crypto-lib-1.0.0.jar use_crypto.py
```

## 🟢 Node.js Example

```bash
node --jvm --polyglot -cp java-lib/target/crypto-lib-1.0.0.jar useCrypto.js
```

## 🦫 Go Example

```bash
go run main.go
```

---

## ✅ Conclusion

- **Java, Python, Node.js, Ruby, R, C/C++** can directly use the Java library under GraalVM.
- **Go** works experimentally.
- **C# / .NET** requires an API bridge.

Enjoy polyglot programming!
