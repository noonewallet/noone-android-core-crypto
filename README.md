![Noone core](https://github.com/noonewallet/noone-android-core-crypto/assets/111989613/1f062349-24d4-4824-9c00-b8f2724eca51)
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

 __Derivation:__
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


## Testing mnemonics generation for a test NIST SP 800-22

To verify the security of entropy generation, we will test the [SecureRandom](https://developer.android.com/reference/java/security/SecureRandom) library.
The verification test was sourced from the official NIST website at [csrc.nist.gov](https://csrc.nist.gov/projects/random-bit-generation/documentation-and-software).

How to build
------------
- Clone this repository. Then build `app-nist` module and run it on the test device
```console
git clone git@github.com:noonewallet/noone-android-core-crypto.git
```
- Download and unpack testing software from the NIST website [csrc.nist.gov](https://csrc.nist.gov/projects/random-bit-generation/documentation-and-software).
- Go to the `nist` folder
- Compile testing software. Run in console:
```console
$ ./make -f makefile
```

How to testing
------------
- In the app enter the file length and click generate. Recommended length no less than 1,507,328
- Copy the generated file to the `nist` folder.
- Calculate file length in bits devided by the number of byte streams: `bitsLength = fileLength * 8 / 10` (for example bit length can be 1200000)

Run test:
```console
$ ./assess 1200000
```

Choose the test method ([0] Input File):
```console
$ Enter Choice: 0
```

Enter the path to the file:
```console
$ User Prescribed Input File: ../bytes.pi
```

Choose the type of test ([01] Frequency):
```console
$ Enter Choice: 1
```

Enter the test parameters:
```console
$ Select Test (0 to continue): 0
$ How many bitstreams? 10
```

Choose a file type. In our case, this is Binary ([1] Binary - Each byte in data file contains 8 bits of data):
```console
$ Select input mode: 1
```

The test will conclude when the message 'Statistical Testing Complete!' appears in the terminal.

After that, the 'finalAnalysisReport.txt' containing the test results will be opened.
The results are written to the file `../nist/sts-*/experiments/AlgorithmTesting/finalAnalysisReport.txt`



## Created using
* [_bouncycastle_](https://www.bouncycastle.org/)

## License
MIT. See the [_LICENSE_](LICENSE) file for more info.
