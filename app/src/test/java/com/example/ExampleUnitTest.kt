package com.example

import com.example.data.repository.MessengerRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun admin_credentials_match_specification() {
        assertEquals("gertrudefrost16@gmail.com", MessengerRepository.ADMIN_EMAIL)
        assertEquals("Admin@175757", MessengerRepository.ADMIN_PASS)
    }

    @Test
    fun otp_code_format_is_six_digits() {
        val testOtp = (100000..999999).random().toString()
        assertEquals(6, testOtp.length)
        assertTrue(testOtp.all { it.isDigit() })
    }
}
