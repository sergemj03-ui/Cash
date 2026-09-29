package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PaymentMethod
import com.example.data.model.UserEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CashLink RDC", appName)
    }

    @Test
    fun `verify user entity level calculation`() {
        val user = UserEntity(level = 2, balanceUsd = 8.50)
        assertEquals("Actif (Argent)", user.getLevelTitle())
        assertTrue(user.balanceUsd > 0.0)
    }

    @Test
    fun `verify payment methods configured for RDC`() {
        val mpesa = PaymentMethod.MPESA
        assertEquals("Vodacom M-Pesa", mpesa.title)
        assertEquals("Vodacom RDC", mpesa.providerName)
        assertEquals(3.0, mpesa.minWithdrawalUsd, 0.001)

        val airtel = PaymentMethod.AIRTEL_MONEY
        assertEquals("Airtel Money", airtel.title)

        val orange = PaymentMethod.ORANGE_MONEY
        assertEquals("Orange Money", orange.title)
    }
}
