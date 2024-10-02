![Noone core](https://github.com/noonewallet/noone-android-core-crypto/assets/111989613/1f062349-24d4-4824-9c00-b8f2724eca51)
[![License](https://img.shields.io/badge/license-MIT-black.svg?style=flat)](https://mit-license.org)
[![Platform](https://img.shields.io/badge/platform-android-blue)](https://developer.apple.com/resources/)
[![Swift](https://img.shields.io/badge/kotlin-1.6.10-brightgreen.svg)](https://developer.apple.com/resources/)
[![Version](https://img.shields.io/badge/Version-1.0.0-orange.svg)]()
[![Version](https://img.shields.io/badge/min_sdk-21-blue.svg)]()
## noone-android-core-bnbbep2
This library is an implementation of BNB beacon chain wallet. Includes address generator and transaction builder

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
  implementation 'https://github.com/noonewallet/noone-android-core-bnbbep2:v1.0.0'
  implementation 'https://github.com/noonewallet/noone-android-core-crypto:v1.5.0'
}
```

## Usage
#### Generate addresses:

```kotlin

val seed = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff".hex
val BNB_PATH = "44'/714'/0'/0/0"
val key = DeterministicSeed.createMasterPrivateKey(seed).getKeyFromSeed(BNB_PATH)
val address = Address(key)
println(address.toString())
    
```

#### Create transaction:

```kotlin
val seed = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff".hex
val key = DeterministicSeed.createMasterPrivateKey(seed).deriveChildKey(ChildNumber.ZERO)

/*Account number and sequence must be obtained via API*/
val builder = TransactionBuilder(
    Bep2NetworkParams.CHAIN_ID,
    accountNumber = 0L,
    sequence = 4L
)
val encodedTx: ByteArray = builder.assemble(
    memo = "hello!",
    source = 1L,
    data = null,
    from ="bnb1jfswr2pvpuu52j0t9a98n7w3tnjcd49t2ryvua",
    to = "bnb1jfswr2pvpuu52j0t9a98n7w3tnjcd49t2ryvua",
    amountRaw = 1337L,
    denom = "BNB",
    pk = key,
)

println(encodedTx.hex)

```

## Created using
* [_bouncycastle_](https://www.bouncycastle.org/)
* [_noone-android-core-crypto_](https://github.com/noonewallet/noone-android-core-crypto)

## License
MIT. See the [_LICENSE_](../noone-android-core-bnbbep2/LICENSE) file for more info.
