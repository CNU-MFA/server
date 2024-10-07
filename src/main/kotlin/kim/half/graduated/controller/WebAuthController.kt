package kim.half.graduated.controller

import kim.half.graduated.service.*
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

        val otp = updateOTP(
            id = loginRequest.id,
            password = loginRequest.password,
            newOTP = generateOTP()
        )
        return LoginResponse(otp = otp)
    }

    @PostMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    fun checkStatus(@RequestBody checkMFARequest: CheckMFARequest): CheckMFAResponse {
        val status = getMFAStatus(checkMFARequest.id, checkMFARequest.password)

        return CheckMFAResponse(
            status = status
        )
    }

    data class LoginRequest(
        val id: String,
        val password: String,
    )

    data class LoginResponse(
        val otp: String?,
    )

    data class CheckMFARequest(
        val id: String,
        val password: String,
    )

    data class CheckMFAResponse(
        val status: MFAStatus,
    )
}
