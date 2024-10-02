# noone-android-core-tron

Kotlin implementation of Tron (TRX) components including ​​transaction serializer for the transfer TRX, TRC10 and TRC20 contracts interactions

## Deps: 

- [Noone android crypto core](https://github.com/noonewallet/noone-android-core-crypto)
- [Bouncy castle](https://www.bouncycastle.org/java.html)

## Tests

all test and examples included into ```io.noone.androidcore.tron.ExampleUnitTest```

## Docs

### Addresses 

The address is last 20 bytes of ```keccak256``` hash from the uncompressed public key

```kotlin
val seed = MnemonicCode.toSeed(
    listOf(
        "symptom",
        "sister",
        "young",
        "regular",
        "seat",
        "divorce",
        "foot",
        "gadget",
        "hospital",
        "action",
        "derive",
        "rude"
    ), ""
)
val DERIVATION_PATH = "44'/195'/0'/0/0"
val ecKey = DeterministicSeed(seed).getKeyFromSeed(DERIVATION_PATH)
val encodedPubKey = ecKey.pubKeyPoint.getEncoded(false)

val PREFIX = 0x41
val hash = encodedPubKey.copyOfRange(1, encodedPubKey.size)
            .keccak256
            .copyOfRange(12, 32)

val address = Base58.encodeChecked(PREFIX, hash)
```

### Transaction structure


A transaction consists of blocks that implement the ```Block``` interface. 
All blocks have a encoded type

There are two main types of blocks:

- `BytesBlock`  - encodes a sequence of bytes (for example, the recipient's address) and its length

```typescript
val bytesBlock = BytesBlock(byteArrayOf(0x12), "aaff".hex)
```

- `VarIntBlock` encodes numbers in varint format
```kotlin
val varintBlock = VarIntBlock(byteArrayOf(0x08), BigInteger.valueOf(31))
```

### Object block

```Block object```
Allows you to encode an array of other blocks.
This type can be encoded without specifying the type and length


```kotlin
val transaction = BytesBlock(byteArrayOf(0x0A), "0A0202...2064".hex)
val signature = BytesBlock(byteArrayOf(0x12), "8BF3...BB00".hex)

val signedTransaction = ObjectBlock()
signedTransaction.add(transaction)
signedTransaction.add(signature)

signedTransaction.encodeBlocksOnly() // encode without type and length
```

### Contracts

Contracts is a specific implementation of ```ObjectBlock``` that defines the transaction type, the method to be called and the input data.
This implementation has contracts for sending TRX/TRC10 and interacting with TRC20 tokens

```kotlin

val sender = "TBv...We"
val triggerSmartContract = TriggerSmartContract(
    Base58.decodeChecked(sender),
    "41977c20977f412c2a1aa4ef3d49fee5ec4c31cdfb".hex, // address decoded from Base58
    "095ea7b30000000000000000000000410fb357921dfb0e32cbc9d1b30f09aad13017f2cd0000000000000000000000000000000000000000000000000000000000000064".hex, // erc20 approve
)

const transferContract = new TransferContract(
    "418840e6c55b9ada326d211d818c34a994aeced808".hex,
    "41d3136787e667d1e055d2cd5db4b5f6c880563049".hex,
    BigInteger.valueOf(100L) // SUN
)
```


### Transaction

Transaction is a specific implementation of ```ObjectBlock``` that contains all the data, including the contract, needed to serialize the transaction

A transaction can be created using the ```Transactionbuilder``` class. 

```kotlin
val triggerSmartContract = TriggerSmartContract(
    "41977c20977f412c2a1aa4ef3d49fee5ec4c31cdfb".hex, // address decoded from Base58
    "419e62be7f4f103c36507cb2a753418791b1cdc182".hex,
    "095ea7b30000000000000000000000410fffffffffffffffffffffffffffaadff01ffff0000000000000000000000000000000000000000000000000000000000000064".hex, // ERC20 approve
)

val tx = TransactionBuilder(true)
    .setRefBlockBytes("1f90".hex)
    .setRefBlockHash("ead284d495f255ad".hex)
    .setExpirationTime(1694217105000)
    .setContract(triggerSmartContract)
    .setTimestamp(1694217046979L)
    .setEnergyLimit(BigInteger.valueOf(100000000L))
    .build()

println(tx.raw)
println(tx.txID)
```

If you do not want to set ```energyLimit```, pass ```false``` to the Transactionbuilder constructor

### What is the ```RefBlockBytes``` and the ```RefBlockHash```?

This is a component of the tron ​​tapos mechanism that can prevent a transaction from being replayed on forks that do not include a specified block.

You can get these fields using the [api](https://developers.tron.network/reference/getblock-2)

```kotlin
val response = api.getBlock()

val refBlockHash = response["blockID"].hex.copyOfRange(8, 16)
val blockBytes = response["block_header"].["raw_data"].["number"].toLong().toBytesBE().copyOfRange(6, 8)
```

### Signature


The signing mechanism included into Crypto core library,
there is an example of signing and serialization:

```kotlin

val tx = TransactionBuilder(true)
    .setRefBlockBytes("1f90".hex)
    //...
    .setEnergyLimit(BigInteger.valueOf(100000000L))
    .build()

val privateKey = "ff...aa"
val signature = ECKey.fromPrivate(privateKey).signWithV(tx.txID)

val encodedSignature = BytesBlock(byteArrayOf(0x12), signature)

val signedTransaction = ObjectBlock()
signedTransaction.add(tx)
signedTransaction.add(encodedSignature)

signedTransaction.encodeBlocksOnly() // ready for broadcast
```


