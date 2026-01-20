package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noisevisionsoftware.vitema.VitemaApplication;
import com.noisevisionsoftware.vitema.dto.request.LoginRequest;
import com.noisevisionsoftware.vitema.exception.AuthenticationException;
import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import com.noisevisionsoftware.vitema.service.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = VitemaApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_WithValidCredentials_ShouldReturnUser() throws Exception {
        // Arrange
        FirebaseUser adminUser = FirebaseUser.builder()
                .uid("test-uid")
                .email("admin@test.com")
                .role("ADMIN")
                .build();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@test.com");
        loginRequest.setPassword("password123");

        when(authService.authenticateAdmin(anyString())).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("test-uid"))
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_WithInvalidAuthorizationHeader_ShouldReturnAuthorizationError() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@test.com");
        loginRequest.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "InvalidFormat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid authorization header format"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnAuthenticationError() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@test.com");
        loginRequest.setPassword("wrong_password");

        when(authService.authenticateAdmin(anyString()))
                .thenThrow(new AuthenticationException("Authentication failed"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication failed"));
    }

    @Test
    void login_WithoutEmail_ShouldReturnValidationError() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPassword("password123");
        // Email is missing, which will cause validation error

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationResults[0].isValid").value(false))
                .andExpect(jsonPath("$.validationResults[0].message").value("Email jest wymagany"))
                .andExpect(jsonPath("$.validationResults[0].severity").value("ERROR"))
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void login_WithInvalidEmailFormat_ShouldReturnValidationError() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("invalid-format");
        loginRequest.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationResults[0].isValid").value(false))
                .andExpect(jsonPath("$.validationResults[0].message").value("Nieprawidłowy format adresu email"))
                .andExpect(jsonPath("$.validationResults[0].severity").value("ERROR"))
                .andExpect(jsonPath("$.valid").value(false));
    }
}