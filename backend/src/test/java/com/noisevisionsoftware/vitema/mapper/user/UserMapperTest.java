package com.noisevisionsoftware.vitema.mapper.user;

import com.noisevisionsoftware.vitema.dto.request.user.UserUpdateRequest;
import com.noisevisionsoftware.vitema.dto.response.UserResponse;
import com.noisevisionsoftware.vitema.model.user.Gender;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserMapper();
    }

    @Test
    void toResponse_WithValidUser_ShouldMapAllFields() {
        // given
        String id = "user-id-123";
        String email = "test@example.com";
        String nickname = "TestUser";
        Gender gender = Gender.MALE;
        Long birthDate = 946684800000L; // 2000-01-01
        Integer storedAge = 25;
        boolean profileCompleted = true;
        UserRole role = UserRole.USER;
        String note = "Test note";
        Long createdAt = 1614556800000L; // 2021-03-01

        User user = User.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .gender(gender)
                .birthDate(birthDate)
                .storedAge(storedAge)
                .profileCompleted(profileCompleted)
                .role(role)
                .note(note)
                .createdAt(createdAt)
                .build();

        // when
        UserResponse response = mapper.toResponse(user);

        // then
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(email, response.getEmail());
        assertEquals(nickname, response.getNickname());
        assertEquals(gender, response.getGender());
        assertEquals(birthDate, response.getBirthDate());
        assertEquals(storedAge, response.getStoredAge());
        assertEquals(profileCompleted, response.isProfileCompleted());
        assertEquals(role, response.getRole());
        assertEquals(note, response.getNote());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void toResponse_WithNullUser_ShouldReturnNull() {
        // when
        UserResponse response = mapper.toResponse(null);

        // then
        assertNull(response);
    }

    @Test
    void toResponse_WithNullFields_ShouldMapNullValues() {
        // given
        User user = User.builder()
                .id("user-id-123")
                .email("test@example.com")
                .profileCompleted(false)
                .role(UserRole.USER)
                .build();

        // when
        UserResponse response = mapper.toResponse(user);

        // then
        assertNotNull(response);
        assertEquals("user-id-123", response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertNull(response.getNickname());
        assertNull(response.getGender());
        assertNull(response.getBirthDate());
        assertNull(response.getStoredAge());
        assertFalse(response.isProfileCompleted());
        assertEquals(UserRole.USER, response.getRole());
        assertNull(response.getNote());
        assertNull(response.getCreatedAt());
    }

    @Test
    void updateUserFromRequest_ShouldUpdateAllSpecifiedFields() {
        // given
        User user = User.builder()
                .id("user-id-123")
                .email("test@example.com")
                .nickname("OldNickname")
                .gender(Gender.MALE)
                .birthDate(946684800000L) // 2000-01-01
                .note("Old note")
                .build();

        UserUpdateRequest request = new UserUpdateRequest();
        request.setNickname("NewNickname");
        request.setGender(Gender.FEMALE);
        request.setBirthDate(978307200000L); // 2001-01-01
        request.setNote("New note");

        // when
        mapper.updateUserFromRequest(user, request);

        // then
        assertEquals("NewNickname", user.getNickname());
        assertEquals(Gender.FEMALE, user.getGender());
        assertEquals(978307200000L, user.getBirthDate());
        assertEquals("New note", user.getNote());

        // Sprawdzamy, czy inne pola nie zostały zmienione
        assertEquals("user-id-123", user.getId());
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    void updateUserFromRequest_WithNullBirthDate_ShouldNotUpdateBirthDate() {
        // given
        User user = User.builder()
                .id("user-id-123")
                .email("test@example.com")
                .birthDate(946684800000L) // 2000-01-01
                .build();

        UserUpdateRequest request = new UserUpdateRequest();
        request.setNickname("NewNickname");
        request.setGender(Gender.FEMALE);
        request.setBirthDate(null); // Null birthDate
        request.setNote("New note");

        // when
        mapper.updateUserFromRequest(user, request);

        // then
        assertEquals(946684800000L, user.getBirthDate()); // Wartość powinna pozostać niezmieniona
    }

    @Test
    void updateUserFromRequest_WithNullFields_ShouldSetNulls() {
        // given
        User user = User.builder()
                .id("user-id-123")
                .email("test@example.com")
                .nickname("OldNickname")
                .gender(Gender.MALE)
                .birthDate(946684800000L) // 2000-01-01
                .note("Old note")
                .build();

        UserUpdateRequest request = new UserUpdateRequest();
        request.setNickname(null);
        request.setGender(null);
        request.setBirthDate(978307200000L); // 2001-01-01
        request.setNote(null);

        // when
        mapper.updateUserFromRequest(user, request);

        // then
        assertNull(user.getNickname());
        assertNull(user.getGender());
        assertEquals(978307200000L, user.getBirthDate());
        assertNull(user.getNote());
    }

    @Test
    void updateUserFromRequest_WithEmptyRequest_ShouldSetNulls() {
        // given
        User user = User.builder()
                .id("user-id-123")
                .email("test@example.com")
                .nickname("OldNickname")
                .gender(Gender.MALE)
                .birthDate(946684800000L) // 2000-01-01
                .note("Old note")
                .build();

        UserUpdateRequest request = new UserUpdateRequest();
        // Wszystkie pola są domyślnie null

        // when
        mapper.updateUserFromRequest(user, request);

        // then
        assertNull(user.getNickname());
        assertNull(user.getGender());
        assertEquals(946684800000L, user.getBirthDate()); // Nie powinno się zmienić
        assertNull(user.getNote());
    }
}