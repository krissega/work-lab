package main

import (
    "fmt"
    "github.com/oracle/graalvm/graalvm-enterprise-polyglot/go/polyglot"
)

func main() {
    ctx, _ := polyglot.NewContext(polyglot.WithHostAccessAll, polyglot.WithHostClassPath("java-lib/target/crypto-lib-1.0.0.jar"))
    defer ctx.Close()

    dummyClass := ctx.GetBindings("java").GetMember("type").Execute("com.example.crypto.DummyEncryptor")

    msg := "Grantly Rocks!"
    enc := dummyClass.GetMember("encrypt").Execute(msg).AsString()
    dec := dummyClass.GetMember("decrypt").Execute(enc).AsString()

    fmt.Println("Original :", msg)
    fmt.Println("Encrypted:", enc)
    fmt.Println("Decrypted:", dec)
}
