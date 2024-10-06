package kim.half.graduated.controller

import kim.half.graduated.util.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/web/auth")
class WebAuthController {

    @Value("\${auth.id}")
    lateinit var id: String

    @Value("\${auth.password}")
    lateinit var password: String

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): LoginResponse {
        check(id == loginRequest.id && password == loginRequest.password) { "로그인 실패" }

        val otp = updateOtp(
            id = loginRequest.id,
            password = loginRequest.password,
            newOtp = generateOtp()
        )
        return LoginResponse(otp = otp)
    }

    @PostMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    fun verifyMFA(@RequestBody verifyMFARequest: VerifyMFARequest) {
        val result = getMFAStatus(verifyMFARequest.id, verifyMFARequest.password)
        check(result) { "MFA 실패" }
    }

    data class LoginRequest(
        val id: String,
        val password: String,
    )

    data class LoginResponse(
        val otp: String?,
    )

    data class VerifyMFARequest(
        val id: String,
        val password: String,
    )
}
