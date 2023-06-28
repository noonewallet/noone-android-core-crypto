![Noone core](https://github.com/noonewallet/noone-android-core-crypto/assets/111989613/c41f6424-8a07-4625-95af-7b128cda5d3e)
[![License](https://img.shields.io/badge/license-MIT-black.svg?style=flat)](https://mit-license.org)
[![Platform](https://img.shields.io/badge/platform-android-blue)](https://developer.apple.com/resources/)
[![Swift](https://img.shields.io/badge/kotlin-1.6.10-brightgreen.svg)](https://developer.apple.com/resources/)
[![Version](https://img.shields.io/badge/Version-1.5.0-orange.svg)]()
[![Version](https://img.shields.io/badge/min_sdk-21-blue.svg)]()
## noone-android-core-crypto
This library is an implementation of tools related to cryptography. Included:
 
 __Keys:__
 [Secp256k1, Secp256r1](https://github.com/noonewallet/noone-android-core-crypto/blob/master/crypto_core/src/main/java/io/noone/androidcore/ECKey.kt)
 
 __Signatures:__ 
 [ECDSA](https://github.com/noonewallet/noone-android-core-crypto/blob/master/crypto_core/src/main/java/io/noone/androidcore/ECKey.kt) 
 
 __Enconding:__ 
 [Base58](https://github.com/noonewallet/noone-android-core-crypto/blob/master/crypto_core/src/main/java/io/noone/androidcore/utils/extensions.kt) | [Bech32](https://github.com/noonewallet/noone-android-core-crypto/blob/master/crypto_core/src/main/java/io/noone/androidcore/utils/Bech32.java) 
 
 __Hash:__ 
 [Sha, RipeMD160, Keccak256, Blake2b](https://github.com/noonewallet/noone-android-core-crypto/blob/master/crypto_core/src/main/java/io/noone/androidcore/utils/extensions.kt)

 __Derivalion:__
[Mnemonic](https://github.com/noonewallet/noone-android-core-crypto/blob/master/crypto_core/src/main/java/io/noone/androidcore/hd/MnemonicCode.kt)
| [Keys derivation](https://github.com/noonewallet/noone-android-core-crypto/blob/master/crypto_core/src/main/java/io/noone/androidcore/hd/DeterministicKey.kt)
 
## Requirements
* Android Studio Flamingo | 2022.2.1 Patch 2
* Gradle 7.1.3
* Min SDK 21

## Installation
Via `JitPack`:

1. Add jitpack to root gradle file

```
buildscript {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```
  
2. Add the dependency:

```
dependencies {
  implementation 'https://github.com/noonewallet/noone-android-core-crypto:v1.5.0'
}
```

## Usage
#### Generate random mnemonic:

```kotlin 
val mc = MnemonicCode(wordlist.byteInputStream(), wordlistHash)
println(mc.getWordList())
```

#### Derive key:

```kotlin 
val entropy = Entropy()
val btcChainPath = "44'/0'/0'/0"
val btcInChainKey = DeterministicSeed(entropy).getKeyFromSeed(btcChainPath)
for (i in 0..100) {
    println(btcInChainKey.deriveChildKey(i, false).pubKey.hex)
}
```

#### Sign message:
```kotlin 
val entropy = Entropy()
val btcChainPath = "44'/0'/0'/0"
val message = "3180aaa8770d3fb796565e6ef657a58374ee066c0da28ee4c2e2d9efa29fb7f1".hex
val btcInChainKey = DeterministicSeed(entropy).getKeyFromSeed(btcChainPath)
val signature = btcInChainKey.sign(message).encodeToDER().hex
println(signature)
```


## Created using
* [_bouncycastle_](https://www.bouncycastle.org/)

## License
MIT. See the [_LICENSE_](LICENSE) file for more info.
