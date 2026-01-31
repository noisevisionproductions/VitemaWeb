package com.noisevisionsoftware.vitema.mapper.user;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.noisevisionsoftware.vitema.model.user.Gender;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirestoreUserMapperTest {

    private FirestoreUserMapper mapper;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new FirestoreUserMapper();
    }

    @Test
    void toUser_WithValidDocument_ShouldMapAllFields() {
        // given
        String id = "user-id-123";
        String email = "test@example.com";
        String nickname = "TestUser";
        String genderValue = "MALE";
        long birthDate = 946684800000L; // 2000-01-01
        int storedAge = 25;
        boolean profileCompleted = true;
        String roleValue = "USER";
        String note = "Test note";
        long createdAt = 1614556800000L; // 2021-03-01

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("nickname", nickname);
        data.put("gender", genderValue);
        data.put("birthDate", birthDate);
        data.put("storedAge", storedAge);
        data.put("profileCompleted", profileCompleted);
        data.put("role", roleValue);
        data.put("note", note);
        data.put("createdAt", createdAt);

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn(id);
        when(documentSnapshot.getData()).thenReturn(data);

        // when
        User result = mapper.toUser(documentSnapshot);

        // then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(email, result.getEmail());
        assertEquals(nickname, result.getNickname());
        assertEquals(Gender.MALE, result.getGender());
        assertEquals(birthDate, result.getBirthDate());
        assertEquals(storedAge, result.getStoredAge());
        assertEquals(profileCompleted, result.isProfileCompleted());
        assertEquals(UserRole.USER, result.getRole());
        assertEquals(note, result.getNote());
        assertEquals(createdAt, result.getCreatedAt());
    }

    @Test
    void toUser_WithNullDocument_ShouldReturnNull() {
        // when
        User result = mapper.toUser(null);

        // then
        assertNull(result);
    }

    @Test
    void toUser_WithNonExistentDocument_ShouldReturnNull() {
        // given
        when(documentSnapshot.exists()).thenReturn(false);

        // when
        User result = mapper.toUser(documentSnapshot);

        // then
        assertNull(result);
    }

    @Test
    void toUser_WithNullData_ShouldReturnNull() {
        // given
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getData()).thenReturn(null);

        // when
        User result = mapper.toUser(documentSnapshot);

        // then
        assertNull(result);
    }

    @Test
    void toUser_WithTimestampForDates_ShouldConvertToLong() {
        // given
        Timestamp birthDateTimestamp = Timestamp.ofTimeSecondsAndNanos(946684800L, 0); // 2000-01-01
        Timestamp createdAtTimestamp = Timestamp.ofTimeSecondsAndNanos(1614556800L, 0); // 2021-03-01

        Map<String, Object> data = new HashMap<>();
        data.put("email", "test@example.com");
        data.put("role", "USER");
        data.put("birthDate", birthDateTimestamp);
        data.put("createdAt", createdAtTimestamp);

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn("user-id-123");
        when(documentSnapshot.getData()).thenReturn(data);

        // when
        User result = mapper.toUser(documentSnapshot);

        // then
        assertNotNull(result);
        assertEquals(946684800000L, result.getBirthDate());
        assertEquals(1614556800000L, result.getCreatedAt());
    }

    @Test
    void toUser_WithNumberForDates_ShouldConvertToLong() {
        // given
        Integer birthDate = 946684800;
        Double createdAt = 1614556800.0;

        Map<String, Object> data = new HashMap<>();
        data.put("email", "test@example.com");
        data.put("role", "USER");
        data.put("birthDate", birthDate);
        data.put("createdAt", createdAt);

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn("user-id-123");
        when(documentSnapshot.getData()).thenReturn(data);

        // when
        User result = mapper.toUser(documentSnapshot);

        // then
        assertNotNull(result);
        assertEquals(946684800L, result.getBirthDate());
        assertEquals(1614556800L, result.getCreatedAt());
    }

    @Test
    void toUser_WithNullOptionalFields_ShouldMapCorrectly() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("email", "test@example.com");
        data.put("role", "USER");
        data.put("gender", null);
        data.put("birthDate", null);
        data.put("storedAge", null);
        data.put("note", null);
        data.put("createdAt", null);

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getId()).thenReturn("user-id-123");
        when(documentSnapshot.getData()).thenReturn(data);

        // when
        User result = mapper.toUser(documentSnapshot);

        // then
        assertNotNull(result);
        assertNull(result.getGender());
        assertNull(result.getBirthDate());
        assertNull(result.getStoredAge());
        assertNull(result.getNote());
        assertNull(result.getCreatedAt());
    }

    @Test
    void toUser_WithAllGenderTypes_ShouldMapCorrectly() {
        // given
        testGenderMapping("MALE", Gender.MALE);
        testGenderMapping("FEMALE", Gender.FEMALE);
    }

    private void testGenderMapping(String firestoreGender, Gender expectedGender) {
        // Setup mock data
        Map<String, Object> data = new HashMap<>();
        data.put("email", "test@example.com");
        data.put("role", "USER");
        data.put("gender", firestoreGender);
        data.put("profileCompleted", false); // Dodane pole

        DocumentSnapshot mockDoc = mock(DocumentSnapshot.class);
        when(mockDoc.exists()).thenReturn(true);
        when(mockDoc.getId()).thenReturn("test-id");
        when(mockDoc.getData()).thenReturn(data);

        // Map and verify
        User result = mapper.toUser(mockDoc);
        assertEquals(expectedGender, result.getGender());
    }

    @Test
    void toUser_WithAllUserRoleTypes_ShouldMapCorrectly() {
        // given
        testUserRoleMapping("ADMIN", UserRole.ADMIN);
        testUserRoleMapping("USER", UserRole.USER);
    }

    private void testUserRoleMapping(String firestoreRole, UserRole expectedRole) {
        // Setup mock data
        Map<String, Object> data = new HashMap<>();
        data.put("email", "test@example.com");
        data.put("role", firestoreRole);
        data.put("profileCompleted", false); // Dodane pole

        DocumentSnapshot mockDoc = mock(DocumentSnapshot.class);
        when(mockDoc.exists()).thenReturn(true);
        when(mockDoc.getId()).thenReturn("test-id");
        when(mockDoc.getData()).thenReturn(data);

        // Map and verify
        User result = mapper.toUser(mockDoc);
        assertEquals(expectedRole, result.getRole());
    }

    @Test
    void toFirestoreMap_WithValidUser_ShouldMapAllFields() {
        // given
        String id = "user-id-456";
        String email = "another@example.com";
        String nickname = "AnotherUser";
        Gender gender = Gender.FEMALE;
        Long birthDate = 978307200000L; // 2001-01-01
        Integer storedAge = 22;
        boolean profileCompleted = true;
        UserRole role = UserRole.ADMIN;
        String note = "Admin user";
        Long createdAt = 1609459200000L; // 2021-01-01
        String trainerId = "trainer-123";

        User user = User.builder()
                .id(id) // id nie powinno być mapowane do mapy Firestore
                .email(email)
                .nickname(nickname)
                .gender(gender)
                .birthDate(birthDate)
                .storedAge(storedAge)
                .profileCompleted(profileCompleted)
                .role(role)
                .note(note)
                .createdAt(createdAt)
                .trainerId(trainerId)
                .build();

        // when
        Map<String, Object> result = mapper.toFirestoreMap(user);

        // then
        assertNotNull(result);
        assertEquals(10, result.size()); // wszystkie pola oprócz id: email, nickname, gender, birthDate, storedAge, profileCompleted, role, note, createdAt, trainerId
        assertEquals(email, result.get("email"));
        assertEquals(nickname, result.get("nickname"));
        assertEquals(gender.name(), result.get("gender"));
        assertEquals(birthDate, result.get("birthDate"));
        assertEquals(storedAge, result.get("storedAge"));
        assertEquals(profileCompleted, result.get("profileCompleted"));
        assertEquals(role.name(), result.get("role"));
        assertEquals(note, result.get("note"));
        assertEquals(createdAt, result.get("createdAt"));
        assertEquals(trainerId, result.get("trainerId"));

        // ID nie powinno być w mapie
        assertFalse(result.containsKey("id"));
    }

    @Test
    void toFirestoreMap_WithNullGender_ShouldMapNullValue() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .gender(null)
                .role(UserRole.USER)
                .build();

        // when
        Map<String, Object> result = mapper.toFirestoreMap(user);

        // then
        assertNull(result.get("gender"));
    }

    @Test
    void toFirestoreMap_WithNullOptionalFields_ShouldMapNullValues() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .role(UserRole.USER)
                .nickname(null)
                .birthDate(null)
                .storedAge(null)
                .note(null)
                .createdAt(null)
                .build();

        // when
        Map<String, Object> result = mapper.toFirestoreMap(user);

        // then
        assertNull(result.get("nickname"));
        assertNull(result.get("birthDate"));
        assertNull(result.get("storedAge"));
        assertNull(result.get("note"));
        assertNull(result.get("createdAt"));
    }
}