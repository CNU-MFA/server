package kim.half.graduated.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class OTPServiceTests {

    @Test
    fun `saveToken should store token successfully`() {
        val id = "user1"
        val password = "password1"
        val token = "token1"

        val savedToken = saveToken(id, password, token)
        assertEquals(token, savedToken)
        assertEquals(token, tokens[Pair(id, password)])
    }

    @Test
    fun `generateOTP should generate 6 digit OTP`() {
        val otp = generateOTP()
        assertEquals(6, otp.length)
        assertTrue(otp.all { it.isDigit() })
    }

    @Test
    fun `updateOTP should successfully update OTP`() {
        val id = "user2"
        val password = "password2"
        val otp = generateOTP()

        val updatedOTP = updateOTP(id, password, otp)
        assertEquals(otp, updatedOTP)
        assertEquals(otp, otpMap[Pair(id, password)])
        assertFalse(mfaStateMap[otp]!!)  // 기본적으로 false로 설정
    }

    @Test
    fun `getOTP should return correct OTP`() {
        val id = "user3"
        val password = "password3"
        val otp = generateOTP()

        updateOTP(id, password, otp)
        val retrievedOTP = getOTP(id, password)
        assertEquals(otp, retrievedOTP)
    }

    @Test
    fun `getOTP should throw exception if OTP not found`() {
        val id = "nonexistentUser"
        val password = "password"

        val exception = assertThrows<IllegalStateException> {
            getOTP(id, password)
        }
        assertEquals("No OTP found for $id", exception.message)
    }

    @Test
    fun `checkOTP should pass MFA with correct OTP`() {
        val id = "user4"
        val password = "password4"
        val otp = generateOTP()

        updateOTP(id, password, otp)
        val isPassed = checkOTP(id, password, otp)
        assertTrue(isPassed)
        assertTrue(mfaStateMap[otp]!!)  // MFA 상태가 true로 업데이트
    }

    @Test
    fun `checkOTP should fail if incorrect OTP`() {
        val id = "user5"
        val password = "password5"
        val correctOTP = generateOTP()
        val wrongOTP = "123456"

        updateOTP(id, password, correctOTP)

        val exception = assertThrows<IllegalStateException> {
            checkOTP(id, password, wrongOTP)
        }
        assertEquals("MFA is not passed", exception.message)
    }

    @Test
    fun `should return NOT_FOUND when OTP does not exist`() {
        // Given
        val id = "user1"
        val password = "password1"

        // When
        val result = getMFAStatus(id, password)

        // Then
        assertEquals(MFAStatus.NOT_FOUND, result)
    }

    @Test
    fun `should return PASSED when MFA is passed`() {
        // Given
        val id = "user2"
        val password = "password2"
        val otp = "OTP123"

        otpMap[Pair(id, password)] = otp
        mfaStateMap[otp] = true

        // When
        val result = getMFAStatus(id, password)

        // Then
        assertEquals(MFAStatus.PASSED, result)
    }

    @Test
    fun `should return NOT_PASSED when MFA is not passed`() {
        // Given
        val id = "user3"
        val password = "password3"
        val otp = "OTP456"

        otpMap[Pair(id, password)] = otp
        mfaStateMap[otp] = false

        // When
        val result = getMFAStatus(id, password)

        // Then
        assertEquals(MFAStatus.NOT_PASSED, result)
    }

    @Test
    fun `passMFA should pass MFA manually`() {
        val id = "user8"
        val password = "password8"
        val otp = generateOTP()

        updateOTP(id, password, otp)
        val isPassed = passMFA(id, password)
        assertTrue(isPassed)
        assertTrue(mfaStateMap[otp]!!)  // MFA 상태가 true로 업데이트
    }
}
