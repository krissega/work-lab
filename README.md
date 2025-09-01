# GraalVM Polyglot Demo — Java Encryption Library

⚠️ Important Notes on GraalVM Languages

Since GraalVM 22+, the polyglot languages are no longer bundled directly inside the GraalVM JDK distribution (both Community and Oracle builds).
That means when you download GraalVM JDK you will only see the standard tools in bin/ (java, javac, gu, native-image), but you will not see executables like graalpy, node, or groot.

🔎 Where to get each language

Java → Always included in GraalVM JDK.

Python → Install separately as GraalPy
.

This provides its own graalpy executable.

You can use it with --jvm --polyglot and the classpath to call Java code.

Node.js → Install via gu install nodejs (if supported by your build) or use graaljs
/ graalvm-nodejs releases
.

This provides a node executable that can run with --jvm --polyglot.

Go (groot) → Still experimental. Not part of Community or Oracle distributions. Only available in GraalVM Labs builds.

Other languages (Ruby, R, LLVM toolchain, WebAssembly) → also installed via gu if supported in your GraalVM build.

✅ What this means for this demo

To run the Java library (DummyEncryptor) in Python → install GraalPy standalone.

To run in Node.js → install graalvm-nodejs or enable it with gu.

To run in Go → experimental only, may not work in all environments.

This project works best if you want to prove interoperability with Java + Python + Node.js.

This project shows how to build a **Java library** (`DummyEncryptor`) and call it from multiple languages using **GraalVM Polyglot**.

## 📦 Java Library

`DummyEncryptor.java` implements Base64-based `encrypt` and `decrypt`.

Build:
```bash
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

---

## ✅ Conclusion

- **Java, Python, Node.js, Ruby, R, C/C++** can directly use the Java library under GraalVM.
- **Go** works experimentally.
- **C# / .NET** requires an API bridge.

Enjoy polyglot programming!
