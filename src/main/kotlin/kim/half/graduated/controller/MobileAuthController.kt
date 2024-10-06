package kim.half.graduated.controller

import kim.half.graduated.util.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mobile/auth")
class MobileAuthController {

    @Value("\${auth.id}")
    lateinit var id: String

    @Value("\${auth.password}")
    lateinit var password: String

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(@RequestBody loginRequest: LoginRequest) {
        check(id == loginRequest.id && password == loginRequest.password) { "로그인 실패" }
        saveToken(
            id = loginRequest.id,
            password = loginRequest.password,
            token = loginRequest.token
        )
        updateOtp(id = loginRequest.id, password = loginRequest.password, newOtp = generateOtp())
    }

    // OTP 인증
    @PostMapping("/otp")
    @ResponseStatus(HttpStatus.OK)
    fun verifyOTP(@RequestBody otpRequest: OTPRequest) {
        val isOk = passMFA(otpRequest.id, otpRequest.password)
        check(isOk) { "OTP 코드가 일치하지 않습니다." }
    }

    // 생체 인식 완료 처리
    @PostMapping("/biometric")
    @ResponseStatus(HttpStatus.OK)
    fun biometricAuthentication(@RequestBody biometricAuthenticationRequest: BiometricAuthenticationRequest) {
        val result = passMFA(
            id = biometricAuthenticationRequest.id,
            password = biometricAuthenticationRequest.password
        )
        check(result == biometricAuthenticationRequest.isSuccess) { "생체 인식 실패" }
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

    data class OTPRequest(
        val id: String,
        val password: String,
        val otp: String,
    )
}

