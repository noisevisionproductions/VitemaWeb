package com.noisevisionsoftware.vitema.service.diet;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.response.diet.DietInfo;
import com.noisevisionsoftware.vitema.exception.DietOverlapException;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.model.diet.Day;
import com.noisevisionsoftware.vitema.model.diet.Diet;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.repository.DietRepository;
import com.noisevisionsoftware.vitema.service.UserService;
import com.noisevisionsoftware.vitema.service.firebase.FirestoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DietService Tests")
class DietServiceTest {

    @Mock
    private DietRepository dietRepository;

    @Mock
    private FirestoreService firestoreService;

    @Mock
    private UserService userService;

    @InjectMocks
    private DietService dietService;

    private static final String TEST_USER_ID = "user123";
    private static final String TEST_DIET_ID = "diet123";
    private static final String TEST_TRAINER_ID = "trainer123";
    private static final String TEST_CLIENT_ID = "client123";

    private Diet testDiet;
    private User testClient;

    @BeforeEach
    void setUp() {
        testDiet = createTestDiet(TEST_DIET_ID, TEST_USER_ID);
        testClient = createTestUser(TEST_CLIENT_ID, TEST_TRAINER_ID);
    }

    @Nested
    @DisplayName("getAllDiets")
    class GetAllDietsTests {

        @Test
        @DisplayName("Should return all diets when user is admin or owner")
        void givenAdminUser_When_GetAllDiets_Then_ReturnAllDiets() {
            // Given
            List<Diet> allDiets = Arrays.asList(
                    createTestDiet("diet1", "user1"),
                    createTestDiet("diet2", "user2")
            );
            when(userService.getCurrentUserId()).thenReturn(TEST_USER_ID);
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);
            when(dietRepository.findAll()).thenReturn(allDiets);

            // When
            List<Diet> result = dietService.getAllDiets();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(allDiets);
            verify(dietRepository).findAll();
            verify(dietRepository, never()).findByUserId(anyString());
        }

        @Test
        @DisplayName("Should return trainer's diets and clients' diets when user is trainer")
        void givenTrainerUser_When_GetAllDiets_Then_ReturnTrainerAndClientsDiets() {
            // Given
            List<User> clients = Collections.singletonList(testClient);
            Diet trainerDiet = createTestDiet("diet1", TEST_TRAINER_ID);
            Diet clientDiet = createTestDiet("diet2", TEST_CLIENT_ID);

            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(false);
            when(userService.getClientsForTrainer(TEST_TRAINER_ID)).thenReturn(clients);
            when(dietRepository.findByUserId(TEST_CLIENT_ID)).thenReturn(Collections.singletonList(clientDiet));
            when(dietRepository.findByUserId(TEST_TRAINER_ID)).thenReturn(Collections.singletonList(trainerDiet));

            // When
            List<Diet> result = dietService.getAllDiets();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).contains(trainerDiet, clientDiet);
            verify(userService).getClientsForTrainer(TEST_TRAINER_ID);
            verify(dietRepository).findByUserId(TEST_CLIENT_ID);
            verify(dietRepository).findByUserId(TEST_TRAINER_ID);
        }

        @Test
        @DisplayName("Should return only user's own diets when user is regular user without clients")
        void givenRegularUser_When_GetAllDiets_Then_ReturnOnlyOwnDiets() {
            // Given
            Diet userDiet = createTestDiet("diet1", TEST_USER_ID);

            when(userService.getCurrentUserId()).thenReturn(TEST_USER_ID);
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(false);
            when(userService.getClientsForTrainer(TEST_USER_ID)).thenReturn(Collections.emptyList());
            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.singletonList(userDiet));

            // When
            List<Diet> result = dietService.getAllDiets();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result).contains(userDiet);
            verify(dietRepository).findByUserId(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should return empty list when trainer has no clients and no own diets")
        void givenTrainerWithNoClientsAndNoDiets_When_GetAllDiets_Then_ReturnEmptyList() {
            // Given
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(false);
            when(userService.getClientsForTrainer(TEST_TRAINER_ID)).thenReturn(Collections.emptyList());
            when(dietRepository.findByUserId(TEST_TRAINER_ID)).thenReturn(Collections.emptyList());

            // When
            List<Diet> result = dietService.getAllDiets();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getDietById")
    class GetDietByIdTests {

        @Test
        @DisplayName("Should return diet when found and user has access")
        void givenValidDietId_When_GetDietById_Then_ReturnDiet() {
            // Given
            when(dietRepository.findById(TEST_DIET_ID)).thenReturn(Optional.of(testDiet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            Diet result = dietService.getDietById(TEST_DIET_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(TEST_DIET_ID);
            verify(dietRepository).findById(TEST_DIET_ID);
        }

        @Test
        @DisplayName("Should throw NotFoundException when diet not found")
        void givenInvalidDietId_When_GetDietById_Then_ThrowNotFoundException() {
            // Given
            when(dietRepository.findById(TEST_DIET_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dietService.getDietById(TEST_DIET_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Diet not found with id: " + TEST_DIET_ID);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user has no access to diet")
        void givenUnauthorizedUser_When_GetDietById_Then_ThrowAccessDeniedException() {
            // Given
            String otherUserId = "otherUser";
            Diet otherUserDiet = createTestDiet(TEST_DIET_ID, otherUserId);
            User otherUser = createTestUser(otherUserId, null);

            when(dietRepository.findById(TEST_DIET_ID)).thenReturn(Optional.of(otherUserDiet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(false);
            when(userService.getCurrentUserId()).thenReturn(TEST_USER_ID);
            when(userService.getUserById(otherUserId)).thenReturn(otherUser);

            // When & Then
            assertThatThrownBy(() -> dietService.getDietById(TEST_DIET_ID))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Nie masz uprawnień do wyświetlania tej diety");
        }

        @Test
        @DisplayName("Should allow access when user is diet owner")
        void givenDietOwner_When_GetDietById_Then_ReturnDiet() {
            // Given
            when(dietRepository.findById(TEST_DIET_ID)).thenReturn(Optional.of(testDiet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(false);
            when(userService.getCurrentUserId()).thenReturn(TEST_USER_ID);

            // When
            Diet result = dietService.getDietById(TEST_DIET_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should allow access when user is diet owner's trainer")
        void givenTrainerOfDietOwner_When_GetDietById_Then_ReturnDiet() {
            // Given
            Diet clientDiet = createTestDiet(TEST_DIET_ID, TEST_CLIENT_ID);

            when(dietRepository.findById(TEST_DIET_ID)).thenReturn(Optional.of(clientDiet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(false);
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.getUserById(TEST_CLIENT_ID)).thenReturn(testClient);

            // When
            Diet result = dietService.getDietById(TEST_DIET_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(TEST_CLIENT_ID);
        }
    }

    @Nested
    @DisplayName("getDietsByUserId")
    class GetDietsByUserIdTests {

        @Test
        @DisplayName("Should return diets when admin or owner requests")
        void givenAdminUser_When_GetDietsByUserId_Then_ReturnDiets() {
            // Given
            List<Diet> userDiets = Collections.singletonList(testDiet);
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);
            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(userDiets);

            // When
            List<Diet> result = dietService.getDietsByUserId(TEST_USER_ID);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result).isEqualTo(userDiets);
        }

        @Test
        @DisplayName("Should return diets when user requests their own diets")
        void givenUserRequestsOwnDiets_When_GetDietsByUserId_Then_ReturnDiets() {
            // Given
            List<Diet> userDiets = Collections.singletonList(testDiet);
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(false);
            when(userService.getCurrentUserId()).thenReturn(TEST_USER_ID);
            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(userDiets);

            // When
            List<Diet> result = dietService.getDietsByUserId(TEST_USER_ID);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result).isEqualTo(userDiets);
        }

        @Test
        @DisplayName("Should return diets when trainer requests client's diets")
        void givenTrainerRequestsClientDiets_When_GetDietsByUserId_Then_ReturnDiets() {
            // Given
            List<Diet> clientDiets = Collections.singletonList(createTestDiet("diet1", TEST_CLIENT_ID));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(false);
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.getUserById(TEST_CLIENT_ID)).thenReturn(testClient);
            when(dietRepository.findByUserId(TEST_CLIENT_ID)).thenReturn(clientDiets);

            // When
            List<Diet> result = dietService.getDietsByUserId(TEST_CLIENT_ID);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result).isEqualTo(clientDiets);
        }

        @Test
        @DisplayName("Should return empty list when user has no access to requested diets")
        void givenUnauthorizedUser_When_GetDietsByUserId_Then_ReturnEmptyList() {
            // Given
            String otherUserId = "otherUser";
            User otherUser = createTestUser(otherUserId, null);

            when(userService.isCurrentUserAdminOrOwner()).thenReturn(false);
            when(userService.getCurrentUserId()).thenReturn(TEST_USER_ID);
            when(userService.getUserById(otherUserId)).thenReturn(otherUser);

            // When
            List<Diet> result = dietService.getDietsByUserId(otherUserId);

            // Then
            assertThat(result).isEmpty();
            verify(dietRepository, never()).findByUserId(otherUserId);
        }

        @Test
        @DisplayName("Should return empty list when user requests client's diets but is not their trainer")
        void givenUserRequestsNonClientDiets_When_GetDietsByUserId_Then_ReturnEmptyList() {
            // Given
            String otherTrainerId = "otherTrainer";
            User userWithDifferentTrainer = createTestUser(TEST_CLIENT_ID, otherTrainerId);

            when(userService.isCurrentUserAdminOrOwner()).thenReturn(false);
            when(userService.getCurrentUserId()).thenReturn(TEST_TRAINER_ID);
            when(userService.getUserById(TEST_CLIENT_ID)).thenReturn(userWithDifferentTrainer);

            // When
            List<Diet> result = dietService.getDietsByUserId(TEST_CLIENT_ID);

            // Then
            assertThat(result).isEmpty();
            verify(dietRepository, never()).findByUserId(TEST_CLIENT_ID);
        }
    }

    @Nested
    @DisplayName("getDietsInfoForUsers")
    class GetDietsInfoForUsersTests {

        @Test
        @DisplayName("Should return diet info with dates when user has diets")
        void givenUserWithDiets_When_GetDietsInfoForUsers_Then_ReturnDietInfoWithDates() {
            // Given
            Timestamp startDate = Timestamp.ofTimeSecondsAndNanos(1646092800, 0);
            Timestamp endDate = Timestamp.ofTimeSecondsAndNanos(1646179200, 0);

            Diet diet = createTestDietWithDates(TEST_DIET_ID, TEST_USER_ID, startDate, endDate);
            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.singletonList(diet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            Map<String, DietInfo> result = dietService.getDietsInfoForUsers(Collections.singletonList(TEST_USER_ID));

            // Then
            assertThat(result).containsKey(TEST_USER_ID);
            DietInfo dietInfo = result.get(TEST_USER_ID);
            assertThat(dietInfo.isHasDiet()).isTrue();
            assertThat(dietInfo.getStartDate()).isEqualTo(startDate);
            assertThat(dietInfo.getEndDate()).isEqualTo(endDate);
        }

        @Test
        @DisplayName("Should return diet info with hasDiet false when user has no diets")
        void givenUserWithoutDiets_When_GetDietsInfoForUsers_Then_ReturnDietInfoWithoutDates() {
            // Given
            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.emptyList());
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            Map<String, DietInfo> result = dietService.getDietsInfoForUsers(Collections.singletonList(TEST_USER_ID));

            // Then
            assertThat(result).containsKey(TEST_USER_ID);
            DietInfo dietInfo = result.get(TEST_USER_ID);
            assertThat(dietInfo.isHasDiet()).isFalse();
            assertThat(dietInfo.getStartDate()).isNull();
            assertThat(dietInfo.getEndDate()).isNull();
        }

        @Test
        @DisplayName("Should handle multiple users with different diet statuses")
        void givenMultipleUsers_When_GetDietsInfoForUsers_Then_ReturnDietInfoForAll() {
            // Given
            String user1 = "user1";
            String user2 = "user2";

            Timestamp startDate = Timestamp.ofTimeSecondsAndNanos(1646092800, 0);
            Timestamp endDate = Timestamp.ofTimeSecondsAndNanos(1646179200, 0);
            Diet diet1 = createTestDietWithDates("diet1", user1, startDate, endDate);

            when(dietRepository.findByUserId(user1)).thenReturn(Collections.singletonList(diet1));
            when(dietRepository.findByUserId(user2)).thenReturn(Collections.emptyList());
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            Map<String, DietInfo> result = dietService.getDietsInfoForUsers(Arrays.asList(user1, user2));

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(user1).isHasDiet()).isTrue();
            assertThat(result.get(user2).isHasDiet()).isFalse();
        }

        @Test
        @DisplayName("Should handle diets with null days gracefully")
        void givenDietWithNullDays_When_GetDietsInfoForUsers_Then_HandleGracefully() {
            // Given
            Diet diet = Diet.builder()
                    .id(TEST_DIET_ID)
                    .userId(TEST_USER_ID)
                    .days(null)
                    .build();

            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.singletonList(diet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            Map<String, DietInfo> result = dietService.getDietsInfoForUsers(Collections.singletonList(TEST_USER_ID));

            // Then
            assertThat(result).containsKey(TEST_USER_ID);
            DietInfo dietInfo = result.get(TEST_USER_ID);
            assertThat(dietInfo.isHasDiet()).isTrue();
            assertThat(dietInfo.getStartDate()).isNull();
            assertThat(dietInfo.getEndDate()).isNull();
        }

        @Test
        @DisplayName("Should handle days with null dates")
        void givenDietWithNullDates_When_GetDietsInfoForUsers_Then_SkipNullDates() {
            // Given
            Day day1 = Day.builder().date(null).build();
            Day day2 = Day.builder().date(Timestamp.ofTimeSecondsAndNanos(1646092800, 0)).build();

            Diet diet = Diet.builder()
                    .id(TEST_DIET_ID)
                    .userId(TEST_USER_ID)
                    .days(Arrays.asList(day1, day2))
                    .build();

            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.singletonList(diet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            Map<String, DietInfo> result = dietService.getDietsInfoForUsers(Collections.singletonList(TEST_USER_ID));

            // Then
            assertThat(result).containsKey(TEST_USER_ID);
            DietInfo dietInfo = result.get(TEST_USER_ID);
            assertThat(dietInfo.getStartDate()).isNotNull();
        }

        @Test
        @DisplayName("Should find earliest and latest dates across multiple diets")
        void givenUserWithMultipleDiets_When_GetDietsInfoForUsers_Then_FindCorrectDateRange() {
            // Given
            Timestamp earliest = Timestamp.ofTimeSecondsAndNanos(1646092800, 0); // March 1
            Timestamp middle = Timestamp.ofTimeSecondsAndNanos(1646179200, 0);   // March 2
            Timestamp latest = Timestamp.ofTimeSecondsAndNanos(1646265600, 0);   // March 3

            Diet diet1 = createTestDietWithDates("diet1", TEST_USER_ID, middle, latest);
            Diet diet2 = createTestDietWithDates("diet2", TEST_USER_ID, earliest, middle);

            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Arrays.asList(diet1, diet2));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            Map<String, DietInfo> result = dietService.getDietsInfoForUsers(Collections.singletonList(TEST_USER_ID));

            // Then
            assertThat(result).containsKey(TEST_USER_ID);
            DietInfo dietInfo = result.get(TEST_USER_ID);
            assertThat(dietInfo.getStartDate()).isEqualTo(earliest);
            assertThat(dietInfo.getEndDate()).isEqualTo(latest);
        }
    }

    @Nested
    @DisplayName("createDiet")
    class CreateDietTests {

        @Test
        @DisplayName("Should create diet successfully when all conditions are met")
        void givenValidDiet_When_CreateDiet_Then_SaveSuccessfully() {
            // Given
            Diet newDiet = createTestDiet(null, TEST_USER_ID);
            Diet savedDiet = createTestDiet(TEST_DIET_ID, TEST_USER_ID);

            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.emptyList());
            when(dietRepository.save(any(Diet.class))).thenReturn(savedDiet);
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            Diet result = dietService.createDiet(newDiet);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(TEST_DIET_ID);
            verify(dietRepository).save(any(Diet.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when diet has no days")
        void givenDietWithNoDays_When_CreateDiet_Then_ThrowIllegalArgumentException() {
            // Given
            Diet dietWithNoDays = Diet.builder()
                    .userId(TEST_USER_ID)
                    .days(Collections.emptyList())
                    .build();

            // When & Then
            assertThatThrownBy(() -> dietService.createDiet(dietWithNoDays))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Dieta musi zawierać przynajmniej jeden dzień");

            verify(dietRepository, never()).save(any(Diet.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when diet has null days")
        void givenDietWithNullDays_When_CreateDiet_Then_ThrowIllegalArgumentException() {
            // Given
            Diet dietWithNullDays = Diet.builder()
                    .userId(TEST_USER_ID)
                    .days(null)
                    .build();

            // When & Then
            assertThatThrownBy(() -> dietService.createDiet(dietWithNullDays))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Dieta musi zawierać przynajmniej jeden dzień");

            verify(dietRepository, never()).save(any(Diet.class));
        }

        @Test
        @DisplayName("Should throw DietOverlapException when diet overlaps with existing diet")
        void givenOverlappingDiet_When_CreateDiet_Then_ThrowDietOverlapException() {
            // Given
            Timestamp startDate = Timestamp.ofTimeSecondsAndNanos(1646092800, 0);
            Timestamp endDate = Timestamp.ofTimeSecondsAndNanos(1646179200, 0);

            Diet existingDiet = createTestDietWithDates("existing", TEST_USER_ID, startDate, endDate);
            Diet newDiet = createTestDietWithDates(null, TEST_USER_ID, startDate, endDate);

            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.singletonList(existingDiet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> dietService.createDiet(newDiet))
                    .isInstanceOf(DietOverlapException.class)
                    .hasMessageContaining("Użytkownik posiada już dietę w podanym okresie");

            verify(dietRepository, never()).save(any(Diet.class));
        }

        @Test
        @DisplayName("Should set createdAt and updatedAt timestamps")
        void givenValidDiet_When_CreateDiet_Then_SetTimestamps() {
            // Given
            Diet newDiet = createTestDiet(null, TEST_USER_ID);
            Diet savedDiet = createTestDiet(TEST_DIET_ID, TEST_USER_ID);

            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.emptyList());
            when(dietRepository.save(any(Diet.class))).thenReturn(savedDiet);
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            dietService.createDiet(newDiet);

            // Then
            assertThat(newDiet.getCreatedAt()).isNotNull();
            assertThat(newDiet.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("updateDiet")
    class UpdateDietTests {

        @Test
        @DisplayName("Should update diet successfully when all conditions are met")
        void givenValidDietUpdate_When_UpdateDiet_Then_UpdateSuccessfully() {
            // Given
            Diet existingDiet = createTestDiet(TEST_DIET_ID, TEST_USER_ID);
            Diet updatedDiet = createTestDiet(TEST_DIET_ID, TEST_USER_ID);

            when(dietRepository.findById(TEST_DIET_ID)).thenReturn(Optional.of(existingDiet));
            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.singletonList(existingDiet));
            when(dietRepository.update(eq(TEST_DIET_ID), any(Diet.class))).thenReturn(updatedDiet);
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            Diet result = dietService.updateDiet(updatedDiet);

            // Then
            assertThat(result).isNotNull();
            verify(dietRepository).update(eq(TEST_DIET_ID), any(Diet.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when diet ID is null")
        void givenDietWithNullId_When_UpdateDiet_Then_ThrowIllegalArgumentException() {
            // Given
            Diet dietWithoutId = createTestDiet(null, TEST_USER_ID);

            // When & Then
            assertThatThrownBy(() -> dietService.updateDiet(dietWithoutId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Diet ID cannot be null for update");

            verify(dietRepository, never()).update(anyString(), any(Diet.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when trying to change diet owner")
        void givenDietWithDifferentUserId_When_UpdateDiet_Then_ThrowIllegalArgumentException() {
            // Given
            Diet existingDiet = createTestDiet(TEST_DIET_ID, TEST_USER_ID);
            Diet updatedDiet = createTestDiet(TEST_DIET_ID, "differentUser");

            when(dietRepository.findById(TEST_DIET_ID)).thenReturn(Optional.of(existingDiet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> dietService.updateDiet(updatedDiet))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nie można zmienić właściciela diety");

            verify(dietRepository, never()).update(anyString(), any(Diet.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when diet has no days")
        void givenDietWithNoDays_When_UpdateDiet_Then_ThrowIllegalArgumentException() {
            // Given
            Diet existingDiet = createTestDiet(TEST_DIET_ID, TEST_USER_ID);
            Diet updatedDiet = Diet.builder()
                    .id(TEST_DIET_ID)
                    .userId(TEST_USER_ID)
                    .days(Collections.emptyList())
                    .build();

            when(dietRepository.findById(TEST_DIET_ID)).thenReturn(Optional.of(existingDiet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> dietService.updateDiet(updatedDiet))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Dieta musi zawierać przynajmniej jeden dzień");

            verify(dietRepository, never()).update(anyString(), any(Diet.class));
        }

        @Test
        @DisplayName("Should throw DietOverlapException when updated diet overlaps with another diet")
        void givenOverlappingUpdate_When_UpdateDiet_Then_ThrowDietOverlapException() {
            // Given
            Timestamp startDate1 = Timestamp.ofTimeSecondsAndNanos(1646092800, 0);
            Timestamp endDate1 = Timestamp.ofTimeSecondsAndNanos(1646179200, 0);
            Timestamp startDate2 = Timestamp.ofTimeSecondsAndNanos(1646265600, 0);
            Timestamp endDate2 = Timestamp.ofTimeSecondsAndNanos(1646352000, 0);

            Diet existingDiet1 = createTestDietWithDates(TEST_DIET_ID, TEST_USER_ID, startDate1, endDate1);
            Diet existingDiet2 = createTestDietWithDates("diet2", TEST_USER_ID, startDate2, endDate2);
            Diet updatedDiet = createTestDietWithDates(TEST_DIET_ID, TEST_USER_ID, startDate2, endDate2);

            when(dietRepository.findById(TEST_DIET_ID)).thenReturn(Optional.of(existingDiet1));
            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Arrays.asList(existingDiet1, existingDiet2));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> dietService.updateDiet(updatedDiet))
                    .isInstanceOf(DietOverlapException.class)
                    .hasMessageContaining("Użytkownik posiada już dietę w podanym okresie");

            verify(dietRepository, never()).update(anyString(), any(Diet.class));
        }

        @Test
        @DisplayName("Should preserve createdAt and update updatedAt")
        void givenValidUpdate_When_UpdateDiet_Then_PreserveCreatedAtAndUpdateUpdatedAt() {
            // Given
            Timestamp originalCreatedAt = Timestamp.ofTimeSecondsAndNanos(1646092800, 0);
            Diet existingDiet = createTestDiet(TEST_DIET_ID, TEST_USER_ID);
            existingDiet.setCreatedAt(originalCreatedAt);

            Diet updatedDiet = createTestDiet(TEST_DIET_ID, TEST_USER_ID);

            when(dietRepository.findById(TEST_DIET_ID)).thenReturn(Optional.of(existingDiet));
            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.singletonList(existingDiet));
            when(dietRepository.update(eq(TEST_DIET_ID), any(Diet.class))).thenReturn(updatedDiet);
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            dietService.updateDiet(updatedDiet);

            // Then
            assertThat(updatedDiet.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(updatedDiet.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should set date for days with null date")
        void givenDaysWithNullDates_When_UpdateDiet_Then_SetDates() {
            // Given
            Diet existingDiet = createTestDiet(TEST_DIET_ID, TEST_USER_ID);

            Day dayWithoutDate = Day.builder().date(null).build();
            Diet updatedDiet = Diet.builder()
                    .id(TEST_DIET_ID)
                    .userId(TEST_USER_ID)
                    .days(Collections.singletonList(dayWithoutDate))
                    .build();

            when(dietRepository.findById(TEST_DIET_ID)).thenReturn(Optional.of(existingDiet));
            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.singletonList(existingDiet));
            when(dietRepository.update(eq(TEST_DIET_ID), any(Diet.class))).thenReturn(updatedDiet);
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            dietService.updateDiet(updatedDiet);

            // Then
            assertThat(dayWithoutDate.getDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("deleteDiet")
    class DeleteDietTests {

        @Test
        @DisplayName("Should delete diet successfully when found")
        void givenValidDietId_When_DeleteDiet_Then_DeleteSuccessfully() {
            // Given
            when(dietRepository.findById(TEST_DIET_ID)).thenReturn(Optional.of(testDiet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);
            doNothing().when(firestoreService).deleteRelatedData(TEST_DIET_ID);
            doNothing().when(dietRepository).delete(TEST_DIET_ID);

            // When
            dietService.deleteDiet(TEST_DIET_ID);

            // Then
            verify(firestoreService).deleteRelatedData(TEST_DIET_ID);
            verify(dietRepository).delete(TEST_DIET_ID);
        }

        @Test
        @DisplayName("Should throw NotFoundException when diet not found")
        void givenInvalidDietId_When_DeleteDiet_Then_ThrowNotFoundException() {
            // Given
            when(dietRepository.findById(TEST_DIET_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dietService.deleteDiet(TEST_DIET_ID))
                    .isInstanceOf(NotFoundException.class);

            verify(firestoreService, never()).deleteRelatedData(anyString());
            verify(dietRepository, never()).delete(anyString());
        }

        @Test
        @DisplayName("Should throw exception when Firestore delete fails")
        void givenFirestoreFailure_When_DeleteDiet_Then_ThrowException() {
            // Given
            when(dietRepository.findById(TEST_DIET_ID)).thenReturn(Optional.of(testDiet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);
            doThrow(new RuntimeException("Firestore error")).when(firestoreService).deleteRelatedData(TEST_DIET_ID);

            // When & Then
            assertThatThrownBy(() -> dietService.deleteDiet(TEST_DIET_ID))
                    .isInstanceOf(RuntimeException.class);

            verify(firestoreService).deleteRelatedData(TEST_DIET_ID);
        }
    }

    @Nested
    @DisplayName("hasDietOverlapForUser")
    class HasDietOverlapForUserTests {

        @Test
        @DisplayName("Should return false when no existing diets")
        void givenNoExistingDiets_When_HasDietOverlapForUser_Then_ReturnFalse() {
            // Given
            Timestamp startDate = Timestamp.ofTimeSecondsAndNanos(1646092800, 0);
            Timestamp endDate = Timestamp.ofTimeSecondsAndNanos(1646179200, 0);

            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.emptyList());
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            boolean result = dietService.hasDietOverlapForUser(TEST_USER_ID, startDate, endDate, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return true when diets overlap")
        void givenOverlappingDiets_When_HasDietOverlapForUser_Then_ReturnTrue() {
            // Given
            Timestamp existingStart = Timestamp.ofTimeSecondsAndNanos(1646092800, 0);
            Timestamp existingEnd = Timestamp.ofTimeSecondsAndNanos(1646179200, 0);
            Timestamp newStart = Timestamp.ofTimeSecondsAndNanos(1646136000, 0); // overlaps
            Timestamp newEnd = Timestamp.ofTimeSecondsAndNanos(1646222400, 0);

            Diet existingDiet = createTestDietWithDates("existing", TEST_USER_ID, existingStart, existingEnd);

            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.singletonList(existingDiet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            boolean result = dietService.hasDietOverlapForUser(TEST_USER_ID, newStart, newEnd, null);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when diets do not overlap")
        void givenNonOverlappingDiets_When_HasDietOverlapForUser_Then_ReturnFalse() {
            // Given
            Timestamp existingStart = Timestamp.ofTimeSecondsAndNanos(1646092800, 0);
            Timestamp existingEnd = Timestamp.ofTimeSecondsAndNanos(1646179200, 0);
            Timestamp newStart = Timestamp.ofTimeSecondsAndNanos(1646265600, 0); // after existing
            Timestamp newEnd = Timestamp.ofTimeSecondsAndNanos(1646352000, 0);

            Diet existingDiet = createTestDietWithDates("existing", TEST_USER_ID, existingStart, existingEnd);

            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.singletonList(existingDiet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            boolean result = dietService.hasDietOverlapForUser(TEST_USER_ID, newStart, newEnd, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should exclude diet with given ID from overlap check")
        void givenDietIdToExclude_When_HasDietOverlapForUser_Then_ExcludeFromCheck() {
            // Given
            Timestamp startDate = Timestamp.ofTimeSecondsAndNanos(1646092800, 0);
            Timestamp endDate = Timestamp.ofTimeSecondsAndNanos(1646179200, 0);

            Diet existingDiet = createTestDietWithDates(TEST_DIET_ID, TEST_USER_ID, startDate, endDate);

            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.singletonList(existingDiet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            boolean result = dietService.hasDietOverlapForUser(TEST_USER_ID, startDate, endDate, TEST_DIET_ID);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should skip diets with null start or end dates")
        void givenDietWithNullDates_When_HasDietOverlapForUser_Then_SkipDiet() {
            // Given
            Timestamp startDate = Timestamp.ofTimeSecondsAndNanos(1646092800, 0);
            Timestamp endDate = Timestamp.ofTimeSecondsAndNanos(1646179200, 0);

            Diet dietWithNullDates = Diet.builder()
                    .id("diet1")
                    .userId(TEST_USER_ID)
                    .days(Collections.emptyList())
                    .build();

            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.singletonList(dietWithNullDates));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            boolean result = dietService.hasDietOverlapForUser(TEST_USER_ID, startDate, endDate, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return true when new diet completely contains existing diet")
        void givenNewDietContainsExisting_When_HasDietOverlapForUser_Then_ReturnTrue() {
            // Given
            Timestamp existingStart = Timestamp.ofTimeSecondsAndNanos(1646136000, 0);
            Timestamp existingEnd = Timestamp.ofTimeSecondsAndNanos(1646179200, 0);
            Timestamp newStart = Timestamp.ofTimeSecondsAndNanos(1646092800, 0); // before existing
            Timestamp newEnd = Timestamp.ofTimeSecondsAndNanos(1646265600, 0);   // after existing

            Diet existingDiet = createTestDietWithDates("existing", TEST_USER_ID, existingStart, existingEnd);

            when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.singletonList(existingDiet));
            when(userService.isCurrentUserAdminOrOwner()).thenReturn(true);

            // When
            boolean result = dietService.hasDietOverlapForUser(TEST_USER_ID, newStart, newEnd, null);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("getDietStartDate and getDietEndDate")
    class GetDietDatesTests {

        @Test
        @DisplayName("Should return start date from first day")
        void givenDietWithDays_When_GetDietStartDate_Then_ReturnFirstDayDate() {
            // Given
            Timestamp startDate = Timestamp.ofTimeSecondsAndNanos(1646092800, 0);
            Timestamp endDate = Timestamp.ofTimeSecondsAndNanos(1646179200, 0);
            Diet diet = createTestDietWithDates(TEST_DIET_ID, TEST_USER_ID, startDate, endDate);

            // When
            Timestamp result = dietService.getDietStartDate(diet);

            // Then
            assertThat(result).isEqualTo(startDate);
        }

        @Test
        @DisplayName("Should return end date from last day")
        void givenDietWithDays_When_GetDietEndDate_Then_ReturnLastDayDate() {
            // Given
            Timestamp startDate = Timestamp.ofTimeSecondsAndNanos(1646092800, 0);
            Timestamp endDate = Timestamp.ofTimeSecondsAndNanos(1646179200, 0);
            Diet diet = createTestDietWithDates(TEST_DIET_ID, TEST_USER_ID, startDate, endDate);

            // When
            Timestamp result = dietService.getDietEndDate(diet);

            // Then
            assertThat(result).isEqualTo(endDate);
        }

        @Test
        @DisplayName("Should return null when days list is null")
        void givenDietWithNullDays_When_GetDietStartDate_Then_ReturnNull() {
            // Given
            Diet diet = Diet.builder()
                    .id(TEST_DIET_ID)
                    .userId(TEST_USER_ID)
                    .days(null)
                    .build();

            // When
            Timestamp startResult = dietService.getDietStartDate(diet);
            Timestamp endResult = dietService.getDietEndDate(diet);

            // Then
            assertThat(startResult).isNull();
            assertThat(endResult).isNull();
        }

        @Test
        @DisplayName("Should return null when days list is empty")
        void givenDietWithEmptyDays_When_GetDietStartDate_Then_ReturnNull() {
            // Given
            Diet diet = Diet.builder()
                    .id(TEST_DIET_ID)
                    .userId(TEST_USER_ID)
                    .days(Collections.emptyList())
                    .build();

            // When
            Timestamp startResult = dietService.getDietStartDate(diet);
            Timestamp endResult = dietService.getDietEndDate(diet);

            // Then
            assertThat(startResult).isNull();
            assertThat(endResult).isNull();
        }
    }

    // Helper methods

    private Diet createTestDiet(String id, String userId) {
        Timestamp date = Timestamp.ofTimeSecondsAndNanos(1646092800, 0);
        Day day = Day.builder()
                .date(date)
                .build();

        return Diet.builder()
                .id(id)
                .userId(userId)
                .days(Collections.singletonList(day))
                .build();
    }

    private Diet createTestDietWithDates(String id, String userId, Timestamp startDate, Timestamp endDate) {
        Day startDay = Day.builder()
                .date(startDate)
                .build();

        Day endDay = Day.builder()
                .date(endDate)
                .build();

        return Diet.builder()
                .id(id)
                .userId(userId)
                .days(Arrays.asList(startDay, endDay))
                .build();
    }

    private User createTestUser(String userId, String trainerId) {
        return User.builder()
                .id(userId)
                .trainerId(trainerId)
                .build();
    }
}
