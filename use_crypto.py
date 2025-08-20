from java import type
DummyEncryptor = type('com.example.crypto.DummyEncryptor')

msg = "Grantly Rocks!"
enc = DummyEncryptor.encrypt(msg)
dec = DummyEncryptor.decrypt(enc)

print("Original :", msg)
print("Encrypted:", enc)
print("Decrypted:", dec)
