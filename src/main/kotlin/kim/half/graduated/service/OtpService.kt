package kim.half.graduated.service

import java.security.SecureRandom

val otpMap: MutableMap<Pair<String, String>, String> = mutableMapOf()
val mfaStateMap: MutableMap<String, Boolean> = mutableMapOf()
val tokens: MutableMap<Pair<String, String>, String> = mutableMapOf()

fun saveToken(id: String, password: String, token: String): String {
    tokens[Pair(id, password)] = token
    return token
}

fun generateOTP(): String {
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
fun getOTP(id: String, password: String): String {
    val otp = otpMap[Pair(id, password)]
    checkNotNull(otp) { "No OTP found for $id" }
    return otp
}

// 특정 사용자와 세션에 대한 OTP 값 수정하기
fun updateOTP(id: String, password: String, newOTP: String): String {
    val key = Pair(id, password)
    otpMap[key] = newOTP  // OTP 값 덮어쓰기
    mfaStateMap[newOTP] = false
    return newOTP
}

// mfa 인증 상태 조회
fun getMFAStatus(id: String, password: String): MFAStatus {
    return otpMap[Pair(id, password)]
        ?.let { mfaStateMap[it] }
        ?.let { if (it) MFAStatus.PASSED else MFAStatus.NOT_PASSED }
        ?: MFAStatus.NOT_FOUND
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
    check(original == otp) { "MFA is not passed" }
    mfaStateMap[otp] = true
    return mfaStateMap[otp] ?: false
}


enum class MFAStatus {
    PASSED, NOT_PASSED, NOT_FOUND
}
