using System;
using System.IO;
using System.Runtime.InteropServices;

class Program
{
    // ========= GraalVM isolate =========
    [DllImport("/Users/usr_rinner/work-lab/go/libBTransactionService.dylib", CallingConvention = CallingConvention.Cdecl)]
    public static extern int graal_create_isolate(IntPtr zero, out IntPtr isolate, out IntPtr thread);

    [DllImport("/Users/usr_rinner/work-lab/go/libBTransactionService.dylib", CallingConvention = CallingConvention.Cdecl)]
    public static extern void graal_tear_down_isolate(IntPtr thread);

    // ========= EntryPoints =========
    [DllImport("/Users/usr_rinner/work-lab/go/libBTransactionService.dylib", CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr getSecuredTransaction(IntPtr thread, string jsonPath);

    [DllImport("/Users/usr_rinner/work-lab/go/libBTransactionService.dylib", CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr secureAndWriteTransaction(IntPtr thread, string jsonPath, string keyPath, string outPath);

    [DllImport("/Users/usr_rinner/work-lab/go/libBTransactionService.dylib", CallingConvention = CallingConvention.Cdecl)]
    public static extern IntPtr decryptTransactionFile(IntPtr thread, string encPath, string keyPath);

    // ========= Helpers =========
    static string PtrToString(IntPtr p) => Marshal.PtrToStringAnsi(p);

    static void Main()
    {
        // Absolute paths en macOS
        string jsonPath = "/Users/usr_rinner/work-lab/transaccion_example.json";
        string keyPath  = "/Users/usr_rinner/work-lab/keys/aes256.key";
        string encPath  = "/Users/usr_rinner/work-lab/out/tx.enc";

        Directory.CreateDirectory("/Users/usr_rinner/work-lab/out");

        // Crear isolate
        IntPtr isolate;
        IntPtr thread;
        int rc = graal_create_isolate(IntPtr.Zero, out isolate, out thread);
        if (rc != 0)
        {
            Console.WriteLine($"Failed to create GraalVM isolate, rc={rc}");
            return;
        }
        Console.WriteLine("GraalVM isolate created (macOS)");

        // 1) getSecuredTransaction
        Console.WriteLine("\n[1] getSecuredTransaction");
        string r1 = PtrToString(getSecuredTransaction(thread, jsonPath));
        Console.WriteLine(r1);

        // 2) secureAndWriteTransaction
        Console.WriteLine("\n[2] secureAndWriteTransaction");
        string r2 = PtrToString(secureAndWriteTransaction(thread, jsonPath, keyPath, encPath));
        Console.WriteLine(r2);

        if (File.Exists(encPath))
        {
            Console.WriteLine($" Encrypted file: {encPath} (size={new FileInfo(encPath).Length} bytes)");
        }
        else
        {
            Console.WriteLine("⚠Encrypted file not created");
        }

        // 3) decryptTransactionFile
        Console.WriteLine("\n[3] decryptTransactionFile");
        string r3 = PtrToString(decryptTransactionFile(thread, encPath, keyPath));
        Console.WriteLine(r3);

        // Tear down
        graal_tear_down_isolate(thread);
        Console.WriteLine("\n Isolate closed");
    }
}