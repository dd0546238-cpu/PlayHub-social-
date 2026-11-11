package com.example

import com.example.model.CoinRates
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCoinToUsdRate() {
        // User requirement: 1,000,000 coins = $1.00 USD
        val coins = 1_000_000L
        val usd = CoinRates.coinsToUsd(coins)
        assertEquals(1.0, usd, 0.0001)

        val coinsHalf = 500_000L
        assertEquals(0.5, CoinRates.coinsToUsd(coinsHalf), 0.0001)

        val coinsFiveM = 5_000_000L
        assertEquals(5.0, CoinRates.coinsToUsd(coinsFiveM), 0.0001)
    }

    @Test
    fun testCoinFormatting() {
        assertEquals("1,000,000", CoinRates.formatCoins(1000000L))
        assertEquals("$1.00", CoinRates.coinsToFormattedUsd(1000000L))
        assertEquals("$0.50", CoinRates.coinsToFormattedUsd(500000L))
    }
}
