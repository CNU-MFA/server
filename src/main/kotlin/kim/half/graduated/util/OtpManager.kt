package kim.half.graduated.util

import java.security.SecureRandom

val otpMap: MutableMap<Pair<String, String>, String> = mutableMapOf()
val mfaStateMap: MutableMap<String, Boolean> = mutableMapOf()
val tokens: MutableMap<Pair<String, String>, String> = mutableMapOf()

fun saveToken(id: String, password: String, token: String): String {
    tokens[Pair(id, password)] = token
    return token
}

fun generateOtp(): String {
    val otpLength = 6
    val random = SecureRandom()
    val otp = StringBuilder()

    repeat(otpLength) {
        val digit = random.nextInt(10)  // 0부터 9까지의 숫자를 생성
        otp.append(digit)
    }

    return otp.toString()
}

// 특정 사용자와 세션에 해당하는 OTP 값 가져오기
fun getOtp(id: String, password: String): String {
    val otp = otpMap[Pair(id, password)]
    checkNotNull(otp) { "No OTP found for $id" }
    return otp
}

// 특정 사용자와 세션에 대한 OTP 값 수정하기
fun updateOtp(id: String, password: String, newOtp: String): String {
    val key = Pair(id, password)
    otpMap[key] = newOtp  // OTP 값 덮어쓰기
    mfaStateMap[newOtp] = false
    return newOtp
}


// mfa 인증 상태 조회
fun getMFAStatus(id: String, password: String): Boolean {
    val otp = otpMap[Pair(id, password)]
    return mfaStateMap[otp] ?: false
}

fun passMFA(id: String, password: String): Boolean {
    val otp = otpMap[Pair(id, password)]
    checkNotNull(otp) { "No OTP found for $id" }
    check(mfaStateMap[otp] == false) { "MFA is not passed" }
    mfaStateMap[otp] = true
    return mfaStateMap[otp] ?: false
}


fun checkOTP(id: String, password: String, otp: String): Boolean {
    val original = otpMap[Pair(id, password)]
    checkNotNull(otp) { "No OTP found for $id" }
    check(original == otp) { "MFA is not passed" }
    mfaStateMap[otp] = true
    return mfaStateMap[otp] ?: false
}
