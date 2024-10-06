package kim.half.graduated.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import jakarta.servlet.ServletException
import kim.half.graduated.util.mfaStateMap
import kim.half.graduated.util.otpMap
import kim.half.graduated.util.tokens
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
class ControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var webLoginRequest: WebAuthController.LoginRequest
    private lateinit var mobileLoginRequest: MobileAuthController.LoginRequest

    @BeforeEach
    fun setUp() {
        // 테스트에 사용할 초기 데이터 설정
        webLoginRequest = WebAuthController.LoginRequest(id = "id", password = "password")
        mobileLoginRequest =
            MobileAuthController.LoginRequest(id = "id", password = "password", token = "token123")
    }

    @AfterEach
    fun tearDown() {
        otpMap.clear()
        mfaStateMap.clear()
        tokens.clear()
    }

    @Test
    fun `web login should return success response`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/web/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webLoginRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `mobile login should return success response`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/mobile/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mobileLoginRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `verify OTP should return success response`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/mobile/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mobileLoginRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/web/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webLoginRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseBody = result.response.getContentAsString();
        val otp = JsonPath.read<String>(responseBody, "$.otp");

        val otpRequest =
            MobileAuthController.OTPRequest(id = "id", password = "password", otp = otp)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/mobile/auth/otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `verify MFA should return MFA verification response`() {
        // 모바일 앱 로그인
        mockMvc.perform(
            MockMvcRequestBuilders.post("/mobile/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mobileLoginRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)

        // 웹앱 로그인
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/web/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webLoginRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseBody = result.response.getContentAsString();
        val otp = JsonPath.read<String>(responseBody, "$.otp");

        val otpRequest =
            MobileAuthController.OTPRequest(id = "id", password = "password", otp = otp)

        // otp 코드 인증
        mockMvc.perform(
            MockMvcRequestBuilders.post("/mobile/auth/otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/web/auth/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webLoginRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `biometric authentication should return confirmation message`() {
        // 모바일 앱 로그인
        mockMvc.perform(
            MockMvcRequestBuilders.post("/mobile/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mobileLoginRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)

        // 웹앱 로그인
        mockMvc.perform(
            MockMvcRequestBuilders.post("/web/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webLoginRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val biometricRequest = MobileAuthController.BiometricAuthenticationRequest(
            id = "id",
            password = "password",
            success = true,
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/mobile/auth/biometric")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(biometricRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `web login should throw ServletException with invalid credentials`() {
        val invalidLoginRequest =
            WebAuthController.LoginRequest(id = "wrongId", password = "wrongPassword")

        assertThrows(ServletException::class.java) {
            mockMvc.perform(
                MockMvcRequestBuilders.post("/web/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidLoginRequest))
            )
        }
    }

    @Test
    fun `mobile login should throw ServletException with invalid credentials`() {
        val invalidLoginRequest = MobileAuthController.LoginRequest(
            id = "wrongId",
            password = "wrongPassword",
            token = "invalidToken"
        )

        assertThrows(ServletException::class.java) {
            mockMvc.perform(
                MockMvcRequestBuilders.post("/mobile/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidLoginRequest))
            )
        }
    }

    @Test
    fun `verify OTP should throw ServletException with invalid OTP`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/mobile/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mobileLoginRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/web/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webLoginRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val otpRequest =
            MobileAuthController.OTPRequest(id = "id", password = "password", otp = "invalidOtp")

        assertThrows(ServletException::class.java) {
            mockMvc.perform(
                MockMvcRequestBuilders.post("/mobile/auth/otp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(otpRequest))
            )
        }
    }

    @Test
    fun `verify MFA should throw ServletException with incorrect OTP`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/mobile/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mobileLoginRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/web/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webLoginRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val otpRequest =
            MobileAuthController.OTPRequest(id = "id", password = "password", otp = "wrongOtp")

        assertThrows(ServletException::class.java) {
            mockMvc.perform(
                MockMvcRequestBuilders.post("/mobile/auth/otp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(otpRequest))
            )
        }
    }

    @Test
    fun `biometric authentication should throw ServletException with unsuccessful biometric`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/mobile/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mobileLoginRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)

        val biometricRequest = MobileAuthController.BiometricAuthenticationRequest(
            id = "id",
            password = "password",
            success = false
        )

        assertThrows(ServletException::class.java) {
            mockMvc.perform(
                MockMvcRequestBuilders.post("/mobile/auth/biometric")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(biometricRequest))
            )
        }
    }
}
