package com.noisevisionsoftware.vitema.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = {CacheConfig.class})
class CacheConfigTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private Cache<String, UserRole> roleCache;

    @Autowired
    private Cache<String, String> userEmailCache;

    @Test
    void cacheManager_ShouldBeConfiguredCorrectly() {
        assertNotNull(cacheManager);

        // Verify all configured caches exist
        assertNotNull(cacheManager.getCache("usersCache"));
        assertNotNull(cacheManager.getCache("dietsCache"));
        assertNotNull(cacheManager.getCache("dietsListCache"));
        assertNotNull(cacheManager.getCache("userRoles"));
        assertNotNull(cacheManager.getCache("userEmailCache"));
        assertNotNull(cacheManager.getCache("measurementsCache"));
        assertNotNull(cacheManager.getCache("changelogCache"));
        assertNotNull(cacheManager.getCache("categories"));
    }

    @Test
    void roleCache_ShouldBeConfiguredCorrectly() {
        assertNotNull(roleCache);

        String testKey = "testUser";
        UserRole testRole = UserRole.ADMIN;

        // Test put and get operations
        roleCache.put(testKey, testRole);
        assertEquals(testRole, roleCache.getIfPresent(testKey));

        // Test cache invalidation
        roleCache.invalidate(testKey);
        assertNull(roleCache.getIfPresent(testKey));
    }

    @Test
    void userEmailCache_ShouldBeConfiguredCorrectly() {
        assertNotNull(userEmailCache);

        String testKey = "userId123";
        String testEmail = "test@example.com";

        // Test put and get operations
        userEmailCache.put(testKey, testEmail);
        assertEquals(testEmail, userEmailCache.getIfPresent(testKey));

        // Test cache invalidation
        userEmailCache.invalidate(testKey);
        assertNull(userEmailCache.getIfPresent(testKey));
    }

    @Test
    void caches_ShouldHandleMultipleEntries() {
        // Test roleCache with multiple entries
        roleCache.put("user1", UserRole.ADMIN);
        roleCache.put("user2", UserRole.USER);

        assertEquals(UserRole.ADMIN, roleCache.getIfPresent("user1"));
        assertEquals(UserRole.USER, roleCache.getIfPresent("user2"));

        // Test userEmailCache with multiple entries
        userEmailCache.put("user1", "user1@example.com");
        userEmailCache.put("user2", "user2@example.com");

        assertEquals("user1@example.com", userEmailCache.getIfPresent("user1"));
        assertEquals("user2@example.com", userEmailCache.getIfPresent("user2"));
    }

    @Test
    void caches_ShouldHandleNonExistentKeys() {
        // Test roleCache with non-existent key
        assertNull(roleCache.getIfPresent("nonExistentUser"));

        // Test userEmailCache with non-existent key
        assertNull(userEmailCache.getIfPresent("nonExistentUser"));
    }

    @Test
    void caches_ShouldHandleInvalidationOfMultipleEntries() {
        // Setup test data
        roleCache.put("user1", UserRole.ADMIN);
        roleCache.put("user2", UserRole.USER);
        userEmailCache.put("user1", "user1@example.com");
        userEmailCache.put("user2", "user2@example.com");

        // Test bulk invalidation for roleCache
        roleCache.invalidateAll();
        assertNull(roleCache.getIfPresent("user1"));
        assertNull(roleCache.getIfPresent("user2"));

        // Test bulk invalidation for userEmailCache
        userEmailCache.invalidateAll();
        assertNull(userEmailCache.getIfPresent("user1"));
        assertNull(userEmailCache.getIfPresent("user2"));
    }
}