![Noone core](https://github.com/noonewallet/noone-android-core-crypto/assets/111989613/1f062349-24d4-4824-9c00-b8f2724eca51)
[![License](https://img.shields.io/badge/license-MIT-black.svg?style=flat)](https://mit-license.org)
[![Platform](https://img.shields.io/badge/platform-android-blue)](https://developer.apple.com/resources/)
[![Swift](https://img.shields.io/badge/kotlin-1.6.10-brightgreen.svg)](https://developer.apple.com/resources/)
[![Version](https://img.shields.io/badge/Version-1.0.0-orange.svg)]()
[![Version](https://img.shields.io/badge/min_sdk-21-blue.svg)]()
## noone-android-core-btclike
This library is an implementation of btc-based cryptocurrencies. Included:

__Currencies:__
[Bitcoin](https://github.com/noonewallet/noone-android-core-btclike/blob/master/crypto_btc_like/src/test/java/io/noone/adnroidcore/btclike/BtcTest.kt) |
[Litecoin](https://github.com/noonewallet/noone-android-core-btclike/blob/master/crypto_btc_like/src/test/java/io/noone/adnroidcore/btclike/LtcTest.kt) |
[Dogecoin](https://github.com/noonewallet/noone-android-core-btclike/blob/master/crypto_btc_like/src/test/java/io/noone/adnroidcore/btclike/DogeTest.kt) |
[Bitcoin cash](https://github.com/noonewallet/noone-android-core-btclike/blob/master/crypto_btc_like/src/test/java/io/noone/adnroidcore/btclike/BchTest.kt)
 
 __Addresses:__
 [Legacy](https://github.com/noonewallet/noone-android-core-btclike/blob/master/crypto_btc_like/src/main/java/io/noone/androidcore/btclike/addresses/LegacyAddress.kt) |
 [Segwit](https://github.com/noonewallet/noone-android-core-btclike/blob/master/crypto_btc_like/src/main/java/io/noone/androidcore/btclike/addresses/SegwitAddress.kt)
 
 __Transaction builder:__ 
 [Tx builder](https://github.com/noonewallet/noone-android-core-btclike/blob/master/crypto_btc_like/src/main/java/io/noone/androidcore/btclike/transaction/TransactionBuilder.kt) 

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
  implementation 'https://github.com/noonewallet/noone-android-core-btclike:v1.0.0'
  implementation 'https://github.com/noonewallet/noone-android-core-crypto:v1.5.0'
}
```

## Usage
#### Generate addresses:

```kotlin 
val seed = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff".hex

(0..100).forEach { index ->
    println(LegacyAddress.fromKey(BitcoinParams, key.deriveChildKey(index)).toBase58())
    println(LegacyAddress.fromKey(DogeParams, key.deriveChildKey(index)).toBase58())
    println(SegwitAddress.fromKey(BitcoinParams, key.deriveChildKey(index)).toBech32())
}
    
```

#### Create transaction:

```kotlin
val seed = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff".hex
val key = DeterministicSeed.createMasterPrivateKey(seed)
val senderKey = PrivateKey(key.deriveChildKey(ChildNumber.ZERO), BitcoinParams)

val receiverAddress = "1ELyiT6djUqaHqQKpVA48Yw3EoMnS7QD8w"
val changeAddress = "1BiBN4aPKupsKtxg6twyaH81wjKW8FKCDn"

val txBuilder = TransactionBuilder(
    WitnessProducer(),
    SigPreimageProducer(),
    ScriptSigProducer(),
    ScriptPubKeyProducer(),
    BitcoinParams
)
    .from(
        UnspentOutput(
            "8b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786",
            0,
            "76a9143f6d330ab1274be7ee7ec75a387e819874c6f9c688ac",
            100000L,
            senderKey,
        ),
        Sequence.MAX
    )
    .to(receiverAddress, 90000L)
    .withFee(1500L)
    .changeTo(changeAddress)

val tx = txBuilder.build()
println(tx.rawTransaction)
```

#### Create bitcoin cash transaction:
```kotlin
val seed = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff".hex
val key = DeterministicSeed.createMasterPrivateKey(seed)
val senderKey = PrivateKey(key.deriveChildKey(ChildNumber.ZERO), BchParams)

val receiverAddress = "1ELyiT6djUqaHqQKpVA48Yw3EoMnS7QD8w"
val changeAddress = "1BiBN4aPKupsKtxg6twyaH81wjKW8FKCDn"

val txBuilder = TransactionBuilder(
    WitnessProducer(),
    BchSigPreimageProducer(),
    BchScriptSigProducer(),
    ScriptPubKeyProducer(),
    BchParams,
)
    .from(
        UnspentOutput(
            "8b328a4768f6f168799a8509e4541d7bc64e632509143c5e27328029680b6786",
            0,
            "76a9143f6d330ab1274be7ee7ec75a387e819874c6f9c688ac",
            100000L,
            senderKey,
        ),
        Sequence.MAX
    )
    .to(receiverAddress, 9000)
    .withFee(1500L)
    .changeTo(changeAddress)

val tx = txBuilder.build()
println(tx.rawTransaction)
```


#### Custom currencies:
create a new ```NetworkParameters``` object:

```kotlin
object AnyCoinParams : NetworkParameters() { // this coin does not exist!
    override val dumpedPrivateKeyHeader = 152
    override val addressHeader = 1
    override val p2SHHeader = 6
    override val segwitAddressHrp = "any"
    override val hashType: HashType
        get() = HashType.DEFAULT
}
```
use it for address generation and transaction builder:
```kotlin
val address = LegacyAddress.fromKey(AnyCoinParams, key.deriveChildKey(index)).toBase58()
```

## Created using
* [_bouncycastle_](https://www.bouncycastle.org/)
* [_noone-android-core-crypto_](https://github.com/noonewallet/noone-android-core-crypto)

## License
MIT. See the [_LICENSE_](../noone-android-core-btclike/LICENSE) file for more info.
