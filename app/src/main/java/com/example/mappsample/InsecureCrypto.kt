package com.example.mappsample

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

// MAPP SAMPLE APP: INTENTIONALLY INSECURE demo material for the MAPP Scan scanner.
// Never ship this code. Each "MAPP demo:" comment names the rule it is meant to trigger.
object InsecureCrypto {

    fun encrypt(s: String): ByteArray {
        // MAPP demo: DEX-STATIC-IV-OR-KEY (hard-coded 8-byte key)
        val key = SecretKeySpec("12345678".toByteArray(), "DES")
        // MAPP demo: DEX-WEAK-CIPHER (DES in ECB mode)
        val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(s.toByteArray())
    }

    fun hash(s: String): ByteArray {
        // MAPP demo: DEX-WEAK-HASH (MD5)
        return MessageDigest.getInstance("MD5").digest(s.toByteArray())
    }

    // Never called: RSA with PKCS#1 v1.5 padding is a second DEX-WEAK-CIPHER finding.
    fun rsa(): Cipher {
        // MAPP demo: DEX-WEAK-CIPHER
        return Cipher.getInstance("RSA/ECB/PKCS1Padding")
    }
}
