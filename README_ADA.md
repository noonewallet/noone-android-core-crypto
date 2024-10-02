![Noone core](https://github.com/noonewallet/noone-android-core-crypto/assets/111989613/1f062349-24d4-4824-9c00-b8f2724eca51)
[![License](https://img.shields.io/badge/license-MIT-black.svg?style=flat)](https://mit-license.org)
[![Platform](https://img.shields.io/badge/platform-android-blue)](https://developer.apple.com/resources/)
[![Swift](https://img.shields.io/badge/kotlin-1.6.10-brightgreen.svg)](https://developer.apple.com/resources/)
[![Version](https://img.shields.io/badge/Version-1.0.0-orange.svg)]()
[![Version](https://img.shields.io/badge/min_sdk-21-blue.svg)]()
## noone-android-core-cardano
This library is an implementation of cardano currency. Includes:

[Cardano Shelly address](https://github.com/noonewallet/noone-android-core-cardano/blob/master/crypto_cardano/src/main/java/io/noone/adnroidcore/cardano/address/CardanoAddress.kt) |
[Cardano key wrapper](https://github.com/noonewallet/noone-android-core-cardano/blob/master/crypto_cardano/src/main/java/io/noone/adnroidcore/cardano/crypto/CardanoKey.kt) |
[Native ed25519 cryptography](https://github.com/noonewallet/noone-android-core-cardano/blob/master/crypto_cardano/src/main/java/io/noone/adnroidcore/cardano/crypto/Native.kt) |
[Transaction builder](https://github.com/noonewallet/noone-android-core-cardano/blob/master/crypto_cardano/src/main/java/io/noone/adnroidcore/cardano/transaction/CardanoTransaction.kt) 

## Requirements
* Android Studio Flamingo | 2022.2.1 Patch 2
* Gradle 7.1.3
* Min SDK 21
* Cmake 3.10.2

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
  implementation 'https://github.com/noonewallet/noone-android-core-cardano:v1.0.0'
  implementation 'https://github.com/noonewallet/noone-android-core-crypto:v1.5.0'
}
```

## Usage
#### generate keys and addresses:

```kotlin 
val CARDANO_EXTERNAL_INDEX = 0
val CARDANO_INTERNAL_INDEX = 1
val CARDANO_STAKE_INDEX = 2
val mKeyCardano = CardanoKey.fromEntropy(MnemonicCode().toEntropy(mnemonic.split(" "), false))

val walletChain = mKeyCardano.deriveByPath("1852'/1815'/0'")

val externalChain = walletKey.deriveChild(CARDANO_EXTERNAL_INDEX, false)    
val stakeChain = walletKey.deriveChild(CARDANO_STAKE_INDEX, false)
val internalChain = walletKey.deriveChild(CARDANO_STAKE_INDEX, false)

val externalKey = externalChain.deriveChild(0, false)
val internalKey = internalChain.deriveChild(0, false)
val stakeKey = stakeChain.deriveChild(0, false)

val externalAddress = CardanoAddress.fromPublicKeys(externalKey.publicKey, stakeKey.publicKey)
val internalAddress = CardanoAddress.fromPublicKeys(internalKey.publicKey, stakeKey.publicKey)

println(externalAddress.address)
println(internalAddress.address)
```

#### Create transaction:

```kotlin

val amount = 1000L

val input = CardanoTransaction.Input(
    utxo.txHash.safeToByteArray(),
    utxo.outputIndex,
    utxo.amount.map { utxo ->
        CardanoTransaction.Input.Amount(
            utxo.unit,
            utxo.quantity.toLong()
        )
    },
    utxo.address
)

val changeOutput = CardanoTransaction.Output(
    amount = calculateMinAdaValue(tokenOutputData), // not implemented
    payload = changeAddress.extractPayload(),
    tokens = tokenOutputData.values.toMutableList(),
    type = CardanoTransaction.Output.Type.CHANGE,
    address = changeAddress.address
)

val output = CardanoTransaction.Output(
    amount = amount,
    payload = addressTo.extractPayload(),
    tokens = emptyList(),
    type = CardanoTransaction.Output.Type.ORDINARY,
    address = changeAddress.address
)

val transaction = CardanoTransaction(
    inputs = listOf(input),
    outputs = listOf(changeOutput, output),
    slot.getTTLslot()
)
transaction.setFeeParams(
    baseFee.toLovelace(),
    feePerByte.toLovelace(),
)
transaction.calculateChangeAmount("lovelace")

val signature = Native.cardanoCryptoEd25519sign(
    transaction.hash,
    32,
    input.paymentKey.privateKey
)

transaction.addSignature(CardanoTransaction.Signature(signature, input.paymentKey.publicKey))

println(transaction.build().hex)
```


## Created using
* [_bouncycastle_](https://www.bouncycastle.org/)
* [_noone-android-core-crypto_](https://github.com/noonewallet/noone-android-core-crypto)

## License
MIT. See the [_LICENSE_](../../noone-android-core-cardano/LICENSE) file for more info.
