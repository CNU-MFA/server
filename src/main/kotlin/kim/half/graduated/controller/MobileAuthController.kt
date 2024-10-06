package kim.half.graduated.controller

import kim.half.graduated.util.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/mobile/auth")
class MobileAuthController {

    @Value("\${auth.id}")
    lateinit var id: String

    @Value("\${auth.password}")
    lateinit var password: String

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): LoginResponse {
        check(id == loginRequest.id && password == loginRequest.password) { "로그인 실패" }
        saveToken(
            id = loginRequest.id,
            password = loginRequest.password,
            token = loginRequest.token
        )
        updateOtp(id = loginRequest.id, password = loginRequest.password, newOtp = generateOtp())

        return LoginResponse(isOk = true)
    }

    // OTP 인증
    @PostMapping("/otp")
    fun verifyOTP(@RequestBody otpRequest: OTPRequest): OTPResponse {
        val isOk = passMFA(otpRequest.id, otpRequest.password)
        check(isOk) { "OTP 코드가 일치하지 않습니다." }
        return OTPResponse(isOk = isOk)
    }

    // 생체 인식 완료 처리
    @PostMapping("/biometric")
    fun biometricAuthentication(@RequestBody biometricAuthenticationRequest: BiometricAuthenticationRequest): BiometricAuthenticationResponse {
        val result = passMFA(
            id = biometricAuthenticationRequest.id,
            password = biometricAuthenticationRequest.password
        )
        return BiometricAuthenticationResponse(isOk = result)
    }

    data class LoginRequest(
        val id: String,
        val password: String,
        val token: String,
    )

    data class LoginResponse(
        val isOk: Boolean,
    )

    data class BiometricAuthenticationRequest(
        val id: String,
        val password: String,
        val isSuccess: Boolean,
    )

    data class BiometricAuthenticationResponse(
        val isOk: Boolean,
    )

    data class OTPRequest(
        val id: String,
        val password: String,
        val otp: String,
    )

    data class OTPResponse(
        val isOk: Boolean,
    )
}

