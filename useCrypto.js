const DummyEncryptor = Java.type("com.example.crypto.DummyEncryptor");

const msg = "Grantly Rocks!";
const enc = DummyEncryptor.encrypt(msg);
const dec = DummyEncryptor.decrypt(enc);

console.log("Original :", msg);
console.log("Encrypted:", enc);
console.log("Decrypted:", dec);
