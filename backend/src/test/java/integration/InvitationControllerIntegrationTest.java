package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noisevisionsoftware.vitema.VitemaApplication;
import com.noisevisionsoftware.vitema.dto.request.invitation.AcceptInvitationRequest;
import com.noisevisionsoftware.vitema.dto.request.invitation.InvitationRequest;
import com.noisevisionsoftware.vitema.exception.*;
import com.noisevisionsoftware.vitema.model.invitation.Invitation;
import com.noisevisionsoftware.vitema.model.invitation.InvitationStatus;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import com.noisevisionsoftware.vitema.service.UserService;
import com.noisevisionsoftware.vitema.service.invitation.InvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = VitemaApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DisplayName("Invitation Controller Integration Tests")
class InvitationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvitationService invitationService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_TRAINER_ID = "trainer123";
    private static final String TEST_CLIENT_EMAIL = "client@example.com";
    private static final String TEST_USER_ID = "user123";
    private static final String TEST_CODE = "TR-ABC123";

    private Invitation testInvitation;
    private User trainerUser;

    @BeforeEach
    void setUp() {
        long now = Instant.now().toEpochMilli();
        long expiresAt = Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli();

        testInvitation = Invitation.builder()
                .id("inv123")
                .trainerId(TEST_TRAINER_ID)
                .clientEmail(TEST_CLIENT_EMAIL)
                .code(TEST_CODE)
                .status(InvitationStatus.PENDING)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        trainerUser = User.builder()
                .id(TEST_TRAINER_ID)
                .email("trainer@example.com")
                .role(UserRole.TRAINER)
                .build();
    }

    @Nested
    @DisplayName("POST /api/invitations/send - Send Invitation")
    class SendInvitationTests {

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("Should create invitation successfully with TRAINER role")
        void sendInvitation_WithTrainerRole_ShouldReturn201() throws Exception {
            // Arrange
            InvitationRequest request = new InvitationRequest();
            request.setEmail(TEST_CLIENT_EMAIL);

            when(invitationService.createInvitation(TEST_CLIENT_EMAIL)).thenReturn(testInvitation);

            // Act & Assert
            mockMvc.perform(post("/api/invitations/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value("inv123"))
                    .andExpect(jsonPath("$.trainerId").value(TEST_TRAINER_ID))
                    .andExpect(jsonPath("$.clientEmail").value(TEST_CLIENT_EMAIL))
                    .andExpect(jsonPath("$.code").value(TEST_CODE))
                    .andExpect(jsonPath("$.status").value("PENDING"));

            verify(invitationService, times(1)).createInvitation(TEST_CLIENT_EMAIL);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should create invitation successfully with ADMIN role")
        void sendInvitation_WithAdminRole_ShouldReturn201() throws Exception {
            // Arrange
            InvitationRequest request = new InvitationRequest();
            request.setEmail(TEST_CLIENT_EMAIL);

            when(invitationService.createInvitation(TEST_CLIENT_EMAIL)).thenReturn(testInvitation);

            // Act & Assert
            mockMvc.perform(post("/api/invitations/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(TEST_CODE));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 when USER role tries to send invitation")
        void sendInvitation_WithUserRole_ShouldReturn403() throws Exception {
            // Arrange
            InvitationRequest request = new InvitationRequest();
            request.setEmail(TEST_CLIENT_EMAIL);

            // Act & Assert
            mockMvc.perform(post("/api/invitations/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(invitationService, never()).createInvitation(anyString());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void sendInvitation_WithoutAuthentication_ShouldReturn401() throws Exception {
            // Arrange
            InvitationRequest request = new InvitationRequest();
            request.setEmail(TEST_CLIENT_EMAIL);

            // Act & Assert
            mockMvc.perform(post("/api/invitations/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("Should return 409 when invitation already exists for email")
        void sendInvitation_WhenAlreadyExists_ShouldReturn409() throws Exception {
            // Arrange
            InvitationRequest request = new InvitationRequest();
            request.setEmail(TEST_CLIENT_EMAIL);

            when(invitationService.createInvitation(TEST_CLIENT_EMAIL))
                    .thenThrow(new InvitationAlreadyExistsException("Zaproszenie już istnieje"));

            // Act & Assert
            mockMvc.perform(post("/api/invitations/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value(containsString("już istnieje")));
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("Should return 500 when email sending fails (rollback)")
        void sendInvitation_WhenEmailFails_ShouldReturn500() throws Exception {
            // Arrange
            InvitationRequest request = new InvitationRequest();
            request.setEmail(TEST_CLIENT_EMAIL);

            when(invitationService.createInvitation(TEST_CLIENT_EMAIL))
                    .thenThrow(new RuntimeException("Nie udało się wysłać emaila"));

            // Act & Assert
            mockMvc.perform(post("/api/invitations/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("Should return 400 when email format is invalid")
        void sendInvitation_WithInvalidEmail_ShouldReturn400() throws Exception {
            // Arrange
            InvitationRequest request = new InvitationRequest();
            request.setEmail("invalid-email");

            // Act & Assert
            mockMvc.perform(post("/api/invitations/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(invitationService, never()).createInvitation(anyString());
        }
    }

    @Nested
    @DisplayName("POST /api/invitations/accept - Accept Invitation")
    class AcceptInvitationTests {

        @Test
        @WithMockUser
        @DisplayName("Should accept invitation successfully")
        void acceptInvitation_WithValidCode_ShouldReturn200() throws Exception {
            // Arrange
            AcceptInvitationRequest request = new AcceptInvitationRequest();
            request.setCode(TEST_CODE);

            when(userService.getCurrentUserId()).thenReturn(TEST_USER_ID);
            when(invitationService.acceptInvitation(TEST_CODE, TEST_USER_ID)).thenReturn(testInvitation);

            // Act & Assert
            mockMvc.perform(post("/api/invitations/accept")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(containsString("zaakceptowane pomyślnie")));

            verify(invitationService, times(1)).acceptInvitation(TEST_CODE, TEST_USER_ID);
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 when invitation not found")
        void acceptInvitation_WithInvalidCode_ShouldReturn404() throws Exception {
            // Arrange
            AcceptInvitationRequest request = new AcceptInvitationRequest();
            request.setCode("TR-INVALID");

            when(userService.getCurrentUserId()).thenReturn(TEST_USER_ID);
            when(invitationService.acceptInvitation("TR-INVALID", TEST_USER_ID))
                    .thenThrow(new InvitationNotFoundException("Zaproszenie nie znalezione"));

            // Act & Assert
            mockMvc.perform(post("/api/invitations/accept")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value(containsString("nie znalezione")));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 410 when invitation expired")
        void acceptInvitation_WhenExpired_ShouldReturn410() throws Exception {
            // Arrange
            AcceptInvitationRequest request = new AcceptInvitationRequest();
            request.setCode(TEST_CODE);

            when(userService.getCurrentUserId()).thenReturn(TEST_USER_ID);
            when(invitationService.acceptInvitation(TEST_CODE, TEST_USER_ID))
                    .thenThrow(new InvitationExpiredException("Zaproszenie wygasło"));

            // Act & Assert
            mockMvc.perform(post("/api/invitations/accept")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isGone())
                    .andExpect(jsonPath("$.detail").value(containsString("wygasło")));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 409 when invitation already used")
        void acceptInvitation_WhenAlreadyUsed_ShouldReturn409() throws Exception {
            // Arrange
            AcceptInvitationRequest request = new AcceptInvitationRequest();
            request.setCode(TEST_CODE);

            when(userService.getCurrentUserId()).thenReturn(TEST_USER_ID);
            when(invitationService.acceptInvitation(TEST_CODE, TEST_USER_ID))
                    .thenThrow(new InvitationAlreadyUsedException("Zaproszenie zostało już użyte"));

            // Act & Assert
            mockMvc.perform(post("/api/invitations/accept")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value(containsString("już użyte")));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void acceptInvitation_WithoutAuthentication_ShouldReturn401() throws Exception {
            // Arrange
            AcceptInvitationRequest request = new AcceptInvitationRequest();
            request.setCode(TEST_CODE);

            // Act & Assert
            mockMvc.perform(post("/api/invitations/accept")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/invitations/my - Get My Invitations")
    class GetMyInvitationsTests {

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("Should return list of invitations for trainer")
        void getMyInvitations_WithTrainerRole_ShouldReturn200() throws Exception {
            // Arrange
            Invitation invitation2 = Invitation.builder()
                    .id("inv456")
                    .trainerId(TEST_TRAINER_ID)
                    .clientEmail("client2@example.com")
                    .code("TR-XYZ789")
                    .status(InvitationStatus.ACCEPTED)
                    .createdAt(Instant.now().toEpochMilli())
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli())
                    .build();

            List<Invitation> invitations = Arrays.asList(testInvitation, invitation2);

            when(invitationService.getMyInvitations()).thenReturn(invitations);

            // Act & Assert
            mockMvc.perform(get("/api/invitations/my"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value("inv123"))
                    .andExpect(jsonPath("$[0].code").value(TEST_CODE))
                    .andExpect(jsonPath("$[0].status").value("PENDING"))
                    .andExpect(jsonPath("$[1].id").value("inv456"))
                    .andExpect(jsonPath("$[1].code").value("TR-XYZ789"))
                    .andExpect(jsonPath("$[1].status").value("ACCEPTED"));

            verify(invitationService, times(1)).getMyInvitations();
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("Should return empty list when no invitations")
        void getMyInvitations_WhenEmpty_ShouldReturnEmptyList() throws Exception {
            // Arrange
            when(invitationService.getMyInvitations()).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/api/invitations/my"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 when USER role tries to get invitations")
        void getMyInvitations_WithUserRole_ShouldReturn403() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/invitations/my"))
                    .andExpect(status().isForbidden());

            verify(invitationService, never()).getMyInvitations();
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getMyInvitations_WithoutAuthentication_ShouldReturn401() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/invitations/my"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/invitations/{id} - Delete Invitation")
    class DeleteInvitationTests {

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("Should delete invitation successfully")
        void deleteInvitation_WithTrainerRole_ShouldReturn200() throws Exception {
            // Arrange
            doNothing().when(invitationService).deleteInvitation("inv123");

            // Act & Assert
            mockMvc.perform(delete("/api/invitations/inv123"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(containsString("usunięte")));

            verify(invitationService, times(1)).deleteInvitation("inv123");
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("Should return 404 when invitation not found")
        void deleteInvitation_WhenNotFound_ShouldReturn404() throws Exception {
            // Arrange
            doThrow(new InvitationNotFoundException("Zaproszenie nie znalezione"))
                    .when(invitationService).deleteInvitation("inv-invalid");

            // Act & Assert
            mockMvc.perform(delete("/api/invitations/inv-invalid"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value(containsString("nie znalezione")));
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("Should return 403 when trying to delete other trainer's invitation")
        void deleteInvitation_WhenNotOwner_ShouldReturn403() throws Exception {
            // Arrange
            doThrow(new UnauthorizedInvitationException("Nie masz uprawnień"))
                    .when(invitationService).deleteInvitation("inv123");

            // Act & Assert
            mockMvc.perform(delete("/api/invitations/inv123"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail").value(containsString("uprawnień")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 when USER role tries to delete")
        void deleteInvitation_WithUserRole_ShouldReturn403() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/invitations/inv123"))
                    .andExpect(status().isForbidden());

            verify(invitationService, never()).deleteInvitation(anyString());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void deleteInvitation_WithoutAuthentication_ShouldReturn401() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/invitations/inv123"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Complete Flow Tests")
    class CompleteFlowTests {

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("Should complete full invitation flow: create -> accept -> verify")
        void completeInvitationFlow_ShouldWorkEndToEnd() throws Exception {
            // Step 1: Trainer creates invitation
            InvitationRequest createRequest = new InvitationRequest();
            createRequest.setEmail(TEST_CLIENT_EMAIL);

            when(invitationService.createInvitation(TEST_CLIENT_EMAIL)).thenReturn(testInvitation);

            mockMvc.perform(post("/api/invitations/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(TEST_CODE));

            // Step 2: Client accepts invitation
            AcceptInvitationRequest acceptRequest = new AcceptInvitationRequest();
            acceptRequest.setCode(TEST_CODE);

            when(userService.getCurrentUserId()).thenReturn(TEST_USER_ID);
            when(invitationService.acceptInvitation(TEST_CODE, TEST_USER_ID))
                    .thenReturn(testInvitation);

            mockMvc.perform(post("/api/invitations/accept")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(acceptRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").exists());

            // Step 3: Trainer verifies invitations list
            testInvitation.setStatus(InvitationStatus.ACCEPTED);
            when(invitationService.getMyInvitations()).thenReturn(List.of(testInvitation));

            mockMvc.perform(get("/api/invitations/my"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("ACCEPTED"));

            // Verify all service calls
            verify(invitationService, times(1)).createInvitation(TEST_CLIENT_EMAIL);
            verify(invitationService, times(1)).acceptInvitation(TEST_CODE, TEST_USER_ID);
            verify(invitationService, times(1)).getMyInvitations();
        }
    }
}
