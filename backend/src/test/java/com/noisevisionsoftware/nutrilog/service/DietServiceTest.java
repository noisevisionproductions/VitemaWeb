package com.noisevisionsoftware.nutrilog.service;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.dto.response.diet.DietInfo;
import com.noisevisionsoftware.nutrilog.exception.NotFoundException;
import com.noisevisionsoftware.nutrilog.model.diet.Day;
import com.noisevisionsoftware.nutrilog.model.diet.Diet;
import com.noisevisionsoftware.nutrilog.model.diet.DietMetadata;
import com.noisevisionsoftware.nutrilog.repository.DietRepository;
import com.noisevisionsoftware.nutrilog.service.diet.DietService;
import com.noisevisionsoftware.nutrilog.service.firebase.FirestoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DietServiceTest {

    @Mock
    private DietRepository dietRepository;

    @Mock
    private FirestoreService firestoreService;

    @InjectMocks
    private DietService dietService;

    private Diet testDiet;
    private static final String TEST_ID = "test123";
    private static final String TEST_USER_ID = "user123";
    private List<Diet> testDietList;

    @BeforeEach
    void setUp() {
        testDiet = Diet.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .createdAt(Timestamp.now())
                .updatedAt(Timestamp.now())
                .days(new ArrayList<>())
                .metadata(DietMetadata.builder().build())
                .build();

        testDietList = List.of(testDiet);
    }

    @Test
    void getAllDiets_ShouldReturnAllDiets() {
        // Arrange
        List<Diet> expectedDiets = Collections.singletonList(testDiet);
        when(dietRepository.findAll()).thenReturn(expectedDiets);

        // Act
        List<Diet> actualDiets = dietService.getAllDiets();

        // Assert
        assertEquals(expectedDiets, actualDiets);
        verify(dietRepository).findAll();
    }

    @Test
    void getDietById_WhenDietExists_ShouldReturnDiet() {
        // Arrange
        when(dietRepository.findById(TEST_ID)).thenReturn(Optional.of(testDiet));

        // Act
        Diet actualDiet = dietService.getDietById(TEST_ID);

        // Assert
        assertEquals(testDiet, actualDiet);
        verify(dietRepository).findById(TEST_ID);
    }

    @Test
    void getDietById_WhenDietDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(dietRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> dietService.getDietById(TEST_ID));
        verify(dietRepository).findById(TEST_ID);
    }

    @Test
    void getDietsByUserId_ShouldReturnUserDiets() {
        // Arrange
        List<Diet> expectedDiets = Collections.singletonList(testDiet);
        when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(expectedDiets);

        // Act
        List<Diet> actualDiets = dietService.getDietsByUserId(TEST_USER_ID);

        // Assert
        assertEquals(expectedDiets, actualDiets);
        verify(dietRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    void createDiet_ShouldSaveDietWithTimestamps() {
        // Arrange
        Diet dietToCreate = Diet.builder()
                .userId(TEST_USER_ID)
                .build();

        when(dietRepository.save(any(Diet.class))).thenReturn(testDiet);

        // Act
        Diet createdDiet = dietService.createDiet(dietToCreate);

        // Assert
        assertNotNull(createdDiet);
        verify(dietRepository).save(dietToCreate);
        assertNotNull(dietToCreate.getCreatedAt());
        assertNotNull(dietToCreate.getUpdatedAt());
    }

    @Test
    void updateDiet_WhenDietExists_ShouldUpdateDiet() {
        // Arrange
        Diet dietToUpdate = Diet.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .days(new ArrayList<>())
                .build();

        when(dietRepository.findById(TEST_ID)).thenReturn(Optional.of(testDiet));

        when(dietRepository.update(eq(TEST_ID), any(Diet.class))).thenAnswer(invocation -> {
            return invocation.<Diet>getArgument(1);
        });

        // Act
        Diet updatedDiet = dietService.updateDiet(dietToUpdate);

        // Assert
        assertNotNull(updatedDiet);
        verify(dietRepository).findById(TEST_ID);
        verify(dietRepository).update(eq(TEST_ID), any(Diet.class));
        assertEquals(testDiet.getCreatedAt(), dietToUpdate.getCreatedAt());
        assertNotNull(dietToUpdate.getUpdatedAt());
    }


    @Test
    void updateDiet_WhenDietDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        Diet dietToUpdate = Diet.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .build();

        when(dietRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> dietService.updateDiet(dietToUpdate));
        verify(dietRepository).findById(TEST_ID);
        verify(dietRepository, never()).save(any(Diet.class));
    }

    @Test
    void deleteDiet_ShouldDeleteDiet() {
        // Arrange
        doNothing().when(dietRepository).delete(TEST_ID);
        doNothing().when(firestoreService).deleteRelatedData(TEST_ID);

        // Act
        dietService.deleteDiet(TEST_ID);

        // Assert
        verify(dietRepository).delete(TEST_ID);
        verify(firestoreService).deleteRelatedData(TEST_ID);
    }


    @Test
    void createDiet_ShouldSetCurrentTimestamps() {
        // Arrange
        Diet dietToCreate = Diet.builder()
                .userId(TEST_USER_ID)
                .build();

        when(dietRepository.save(any(Diet.class))).thenAnswer(invocation -> invocation.<Diet>getArgument(0));

        // Act
        Diet createdDiet = dietService.createDiet(dietToCreate);

        // Assert
        assertNotNull(createdDiet.getCreatedAt());
        assertNotNull(createdDiet.getUpdatedAt());
        assertTrue(createdDiet.getCreatedAt().getSeconds() > 0);
        assertTrue(createdDiet.getUpdatedAt().getSeconds() > 0);
    }

    @Test
    void updateDiet_ShouldPreserveCreatedAtAndUpdateUpdatedAt() {
        // Arrange
        Timestamp originalCreatedAt = Timestamp.now();
        testDiet.setCreatedAt(originalCreatedAt);

        Diet dietToUpdate = Diet.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .days(new ArrayList<>())
                .build();

        when(dietRepository.findById(TEST_ID)).thenReturn(Optional.of(testDiet));

        // Poprawka: zwróć przekazaną dietę z metody update
        when(dietRepository.update(eq(TEST_ID), any(Diet.class))).thenAnswer(invocation -> {
            return invocation.<Diet>getArgument(1);
        });

        // Act
        Diet updatedDiet = dietService.updateDiet(dietToUpdate);

        // Assert
        assertEquals(originalCreatedAt, updatedDiet.getCreatedAt());
        assertNotNull(updatedDiet.getUpdatedAt());
        assertTrue(updatedDiet.getUpdatedAt().getSeconds() >= originalCreatedAt.getSeconds());
    }

    @Test
    void getDietsInfoForUsers_WhenUserHasDiets_ShouldReturnCorrectInfo() {
        // given
        Timestamp date1 = Timestamp.ofTimeSecondsAndNanos(1646092800, 0); // 2022-03-01
        Timestamp date2 = Timestamp.ofTimeSecondsAndNanos(1646179200, 0); // 2022-03-02

        Day day1 = new Day();
        day1.setDate(date1);

        Day day2 = new Day();
        day2.setDate(date2);

        testDiet.setDays(Arrays.asList(day1, day2));

        List<String> userIds = List.of(TEST_USER_ID);
        when(dietRepository.findByUserId(TEST_USER_ID)).thenReturn(testDietList);

        // when
        Map<String, DietInfo> result = dietService.getDietsInfoForUsers(userIds);

        // then
        assertEquals(1, result.size());
        assertTrue(result.containsKey(TEST_USER_ID));
        assertTrue(result.get(TEST_USER_ID).isHasDiet());
        assertNotNull(result.get(TEST_USER_ID).getStartDate());
        assertNotNull(result.get(TEST_USER_ID).getEndDate());
        assertEquals(date1, result.get(TEST_USER_ID).getStartDate());
        assertEquals(date2, result.get(TEST_USER_ID).getEndDate());
        verify(dietRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    void getDietsInfoForUsers_WhenUserHasNoDiets_ShouldReturnEmptyInfo() {
        // given
        String emptyUserId = "emptyUser";
        List<String> userIds = List.of(emptyUserId);
        when(dietRepository.findByUserId(emptyUserId)).thenReturn(Collections.emptyList());

        // when
        Map<String, DietInfo> result = dietService.getDietsInfoForUsers(userIds);

        // then
        assertEquals(1, result.size());
        assertTrue(result.containsKey(emptyUserId));
        assertFalse(result.get(emptyUserId).isHasDiet());
        assertNull(result.get(emptyUserId).getStartDate());
        assertNull(result.get(emptyUserId).getEndDate());
        verify(dietRepository).findByUserId(emptyUserId);
    }

    @Test
    void createDiet_ShouldSetTimestampsAndSaveDiet() {
        // given
        Diet newDiet = Diet.builder()
                .userId(TEST_USER_ID)
                .days(new ArrayList<>())
                .build();

        // Przechwyć wartość przekazaną do save
        when(dietRepository.save(any(Diet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Diet result = dietService.createDiet(newDiet);

        // then
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertEquals(TEST_USER_ID, result.getUserId());
        verify(dietRepository).save(newDiet);
    }

    @Test
    void updateDiet_WhenIdIsNull_ShouldThrowIllegalArgumentException() {
        // given
        Diet dietWithNullId = Diet.builder()
                .userId(TEST_USER_ID)
                .build();

        // when, then
        assertThrows(IllegalArgumentException.class, () -> dietService.updateDiet(dietWithNullId));
    }

    @Test
    void updateDiet_WhenUserIdDoesNotMatch_ShouldThrowAccessDeniedException() {
        // given
        String differentUserId = "differentUser";
        Diet dietWithDifferentUserId = Diet.builder()
                .id(TEST_ID)
                .userId(differentUserId)
                .build();

        when(dietRepository.findById(TEST_ID)).thenReturn(Optional.of(testDiet));

        // when, then
        assertThrows(AccessDeniedException.class, () -> dietService.updateDiet(dietWithDifferentUserId));
    }

    @Test
    void updateDiet_WhenValid_ShouldUpdateDietAndPreserveCreatedAt() {
        // given
        Diet updatedDiet = Diet.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .days(new ArrayList<>())
                .build();

        when(dietRepository.findById(TEST_ID)).thenReturn(Optional.of(testDiet));
        when(dietRepository.update(eq(TEST_ID), any(Diet.class))).thenAnswer(invocation -> invocation.getArgument(1));

        // when
        Diet result = dietService.updateDiet(updatedDiet);

        // then
        assertEquals(testDiet.getCreatedAt(), result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(dietRepository).findById(TEST_ID);
        verify(dietRepository).update(eq(TEST_ID), any(Diet.class));
    }

    @Test
    void updateDiet_WhenDayHasNoDate_ShouldSetCurrentDate() {
        // given
        Day dayWithoutDate = new Day();
        dayWithoutDate.setDate(null);

        Diet dietWithDayWithoutDate = Diet.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .days(List.of(dayWithoutDate))
                .build();

        when(dietRepository.findById(TEST_ID)).thenReturn(Optional.of(testDiet));
        when(dietRepository.update(eq(TEST_ID), any(Diet.class))).thenAnswer(invocation -> invocation.getArgument(1));

        // when
        Diet result = dietService.updateDiet(dietWithDayWithoutDate);

        // then
        assertNotNull(result.getDays().getFirst().getDate());
        verify(dietRepository).update(eq(TEST_ID), any(Diet.class));
    }

    @Test
    void deleteDiet_ShouldDeleteDietAndRelatedData() {
        // given
        doNothing().when(firestoreService).deleteRelatedData(TEST_ID);
        doNothing().when(dietRepository).delete(TEST_ID);

        // when
        dietService.deleteDiet(TEST_ID);

        // then
        verify(firestoreService).deleteRelatedData(TEST_ID);
        verify(dietRepository).delete(TEST_ID);
    }

    @Test
    void deleteDiet_WhenExceptionOccurs_ShouldPropagateException() {
        // given
        RuntimeException testException = new RuntimeException("Test exception");
        doThrow(testException).when(firestoreService).deleteRelatedData(TEST_ID);

        // when, then
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> dietService.deleteDiet(TEST_ID));
        assertEquals(testException, thrown);

        verify(firestoreService).deleteRelatedData(TEST_ID);
        verify(dietRepository, never()).delete(TEST_ID);
    }

    @Test
    void refreshDietsCache_ShouldNotThrowException() {
        // when, then
        assertDoesNotThrow(() -> dietService.refreshDietsCache());
    }
}