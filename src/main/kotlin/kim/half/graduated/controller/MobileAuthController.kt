package kim.half.graduated.controller

import com.fasterxml.jackson.annotation.JsonProperty
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
        val isOk = checkOTP(otpRequest.id, otpRequest.password, otpRequest.otp)
        check(isOk) { "OTP 코드가 일치하지 않습니다." }
    }

    // 생체 인식 완료 처리
    @PostMapping("/biometric")
    @ResponseStatus(HttpStatus.OK)
    fun biometricAuthentication(@RequestBody request: BiometricAuthenticationRequest) {
        check(request.success) { "생체 인식 실패" }
        val result = passMFA(
            id = request.id,
            password = request.password
        )
        check(result) { "생체 인식 실패" }
    }

    data class LoginRequest(
        val id: String,
        val password: String,
        val token: String,
    )

    data class BiometricAuthenticationRequest(
        val id: String,
        val password: String,
        val success: Boolean
    )

    data class OTPRequest(
        val id: String,
        val password: String,
        val otp: String,
    )
}

