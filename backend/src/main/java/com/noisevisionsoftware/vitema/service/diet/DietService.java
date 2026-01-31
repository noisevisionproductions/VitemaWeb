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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DietService {
    private final DietRepository dietRepository;
    private final FirestoreService firestoreService;
    private final UserService userService;

    private static final String DIETS_CACHE = "dietsCache";
    private static final String DIETS_LIST_CACHE = "dietsListCache";

    @Cacheable(value = DIETS_CACHE, key = "'allDiets_' + @userService.getCurrentUserId()")
    public List<Diet> getAllDiets() {
        String currentUserId = userService.getCurrentUserId();
        boolean isAdminOrOwner = userService.isCurrentUserAdminOrOwner();

        if (isAdminOrOwner) {
            return dietRepository.findAll();
        } else {
            List<User> clients = userService.getClientsForTrainer(currentUserId);

            List<Diet> trainerDiets = new ArrayList<>();

            for (User client : clients) {
                trainerDiets.addAll(dietRepository.findByUserId(client.getId()));
            }

            trainerDiets.addAll(dietRepository.findByUserId(currentUserId));

            return trainerDiets;
        }
    }

    @Cacheable(value = DIETS_CACHE, key = "#id")
    public Diet getDietById(String id) {
        Diet diet = dietRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Diet not found with id: " + id));

        verifyDietAccess(diet);

        return diet;
    }

    @Cacheable(value = DIETS_LIST_CACHE, key = "#userId")
    public List<Diet> getDietsByUserId(String userId) {
        if (!userService.isCurrentUserAdminOrOwner()) {
            String currentUserId = userService.getCurrentUserId();
            if (!userId.equals(currentUserId)) {
                User targetUser = userService.getUserById(userId);
                if (targetUser.getTrainerId() == null || !targetUser.getTrainerId().equals(currentUserId)) {
                    return Collections.emptyList();
                }
            }
        }

        return dietRepository.findByUserId(userId);
    }

    public Map<String, DietInfo> getDietsInfoForUsers(List<String> userIds) {
        Map<String, DietInfo> dietInfoMap = new HashMap<>();

        for (String userId : userIds) {
            List<Diet> userDiets = getDietsByUserId(userId);

            if (!userDiets.isEmpty()) {
                Timestamp earliestDate = null;
                Timestamp latestDate = null;

                for (Diet diet : userDiets) {
                    if (diet.getDays() == null) continue;
                    for (Day day : diet.getDays()) {
                        Timestamp dayDate = day.getDate();

                        if (earliestDate == null || (dayDate != null && dayDate.compareTo(earliestDate) < 0)) {
                            earliestDate = dayDate;
                        }
                        if (latestDate == null || (dayDate != null && dayDate.compareTo(latestDate) > 0)) {
                            latestDate = dayDate;
                        }
                    }
                }

                dietInfoMap.put(userId, DietInfo.builder()
                        .hasDiet(true)
                        .startDate(earliestDate)
                        .endDate(latestDate)
                        .build());
            } else {
                dietInfoMap.put(userId, DietInfo.builder()
                        .hasDiet(false)
                        .startDate(null)
                        .endDate(null)
                        .build());
            }
        }

        return dietInfoMap;
    }

    @Caching(evict = {
            @CacheEvict(value = DIETS_CACHE, allEntries = true),
            @CacheEvict(value = DIETS_LIST_CACHE, allEntries = true)
    })
    public Diet createDiet(Diet diet) {
        diet.setCreatedAt(Timestamp.now());
        diet.setUpdatedAt(Timestamp.now());

        if (diet.getDays() == null || diet.getDays().isEmpty()) {
            throw new IllegalArgumentException("Dieta musi zawierać przynajmniej jeden dzień");
        }

        if (hasDietOverlapForUser(diet.getUserId(), getDietStartDate(diet), getDietEndDate(diet), null)) {
            throw new DietOverlapException("Użytkownik posiada już dietę w podanym okresie. " +
                    "Usuń istniejącą dietę lub zmień datę rozpoczęcia.");
        }

        Diet savedDiet = dietRepository.save(diet);
        refreshDietsCache();
        return savedDiet;
    }

    @Caching(evict = {
            @CacheEvict(value = DIETS_CACHE, allEntries = true),
            @CacheEvict(value = DIETS_LIST_CACHE, allEntries = true)
    })
    public Diet updateDiet(Diet diet) {
        if (diet.getId() == null) {
            throw new IllegalArgumentException("Diet ID cannot be null for update");
        }

        synchronized (this) {
            Diet existingDiet = getDietById(diet.getId());

            if (!existingDiet.getUserId().equals(diet.getUserId())) {
                throw new IllegalArgumentException("Nie można zmienić właściciela diety");
            }

            if (diet.getDays() == null || diet.getDays().isEmpty()) {
                throw new IllegalArgumentException("Dieta musi zawierać przynajmniej jeden dzień.");
            }

            if (hasDietOverlapForUser(diet.getUserId(), getDietStartDate(diet), getDietEndDate(diet), diet.getId())) {
                throw new DietOverlapException("Użytkownik posiada już dietę w podanym okresie. " +
                        "Usuń istniejącą dietę lub zmień datę rozpoczęcia.");
            }

            diet.setCreatedAt(existingDiet.getCreatedAt());
            diet.setUpdatedAt(Timestamp.now());

            diet.getDays().forEach(day -> {
                if (day.getDate() == null) {
                    day.setDate(Timestamp.now());
                }
            });

            Diet updatedDiet = dietRepository.update(diet.getId(), diet);
            refreshDietsCache();
            return updatedDiet;
        }
    }

    @Caching(evict = {
            @CacheEvict(value = DIETS_CACHE, allEntries = true),
            @CacheEvict(value = DIETS_LIST_CACHE, allEntries = true)
    })
    public void deleteDiet(String id) {
        try {
            getDietById(id);

            firestoreService.deleteRelatedData(id);
            dietRepository.delete(id);
            refreshDietsCache();
        } catch (Exception e) {
            log.error("Error deleting diet with id: {}", "Error deleting diet", e);
            throw e;
        }
    }

    public boolean hasDietOverlapForUser(String userId, Timestamp startDate, Timestamp endDate, String dietIdToExclude) {
        List<Diet> userDiets = getDietsByUserId(userId);

        for (Diet diet : userDiets) {
            if (diet.getId().equals(dietIdToExclude)) {
                continue;
            }

            Timestamp dietStartDate = getDietStartDate(diet);
            Timestamp dietEndDate = getDietEndDate(diet);

            if (dietStartDate == null || dietEndDate == null) {
                continue;
            }

            boolean overlap = !(endDate.compareTo(dietStartDate) < 0 || startDate.compareTo(dietEndDate) > 0);
            if (overlap) {
                return true;
            }
        }

        return false;
    }

    /**
     * Odświeża cache diet.
     * Ta metoda powinna być wywołana po operacjach, które zmieniają diety,
     * a których wyniki mogą nie być natychmiast widoczne z powodu cachowania.
     */
    @Caching(evict = {
            @CacheEvict(value = DIETS_CACHE, allEntries = true),
            @CacheEvict(value = DIETS_LIST_CACHE, allEntries = true)
    })
    public void refreshDietsCache() {
        log.debug("Odświeżenie cache diet");
    }

    public Timestamp getDietStartDate(Diet diet) {
        if (diet.getDays() == null || diet.getDays().isEmpty()) {
            return null;
        }
        return diet.getDays().getFirst().getDate();
    }

    public Timestamp getDietEndDate(Diet diet) {
        if (diet.getDays() == null || diet.getDays().isEmpty()) {
            return null;
        }
        return diet.getDays().getLast().getDate();
    }

    private void verifyDietAccess(Diet diet) {
        if (userService.isCurrentUserAdminOrOwner()) {
            return;
        }

        String currentUserId = userService.getCurrentUserId();

        if (diet.getUserId().equals(currentUserId)) {
            return;
        }

        User dietOwner = userService.getUserById(diet.getUserId());
        if (dietOwner.getTrainerId() != null && dietOwner.getTrainerId().equals(currentUserId)) {
            return;
        }

        throw new AccessDeniedException("Nie masz uprawnień do wyświetlania tej diety.");
    }
}