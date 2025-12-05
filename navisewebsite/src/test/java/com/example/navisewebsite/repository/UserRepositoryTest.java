package com.example.navisewebsite.repository;

import com.example.navisewebsite.domain.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserRepository CRUD operations.
 * Tests user authentication functionality including finding, adding, and updating users.
 * Uses in-memory SQLite database for testing.
 */
public class UserRepositoryTest {

    private UserRepository userRepository;

    @BeforeAll
    public static void setUpAll() {
        // Initialize in-memory test databases once for all tests
        TestDatabaseConfig.initializeTestDatabases();
    }

    @AfterAll
    public static void tearDownAll() {
        // Close test databases after all tests
        TestDatabaseConfig.closeTestDatabases();
    }

    @BeforeEach
    public void setUp() {
        // Clear all data before each test for test isolation
        TestDatabaseConfig.clearAllData();
        userRepository = new UserRepository();
    }

    @Test
    public void testAddStudentSuccessfully() {
        int userId = userRepository.addStudent("newstudent@university.edu", "studentPass789");
        assertTrue(userId > 0, "Newly added student should return valid user ID");
        Optional<User> userOpt = userRepository.findByEmail("newstudent@university.edu");
        assertTrue(userOpt.isPresent(), "Newly added student should exist");
        User user = userOpt.get();
        assertEquals("newstudent@university.edu", user.getEmail());
        assertEquals("student", user.getUserType());
        assertEquals("studentPass789", user.getPassword());
        assertTrue(user.isStudent());
        assertFalse(user.isAdmin());
    }

    @Test
    public void testAddStudentWithDuplicateEmail() {
        int firstId = userRepository.addStudent("duplicate@university.edu", "password123");
        assertTrue(firstId > 0, "First student should be added successfully");
        
        int secondId = userRepository.addStudent("duplicate@university.edu", "password456");
        assertEquals(-1, secondId, "Adding student with duplicate email should return -1");
    }

    @Test
    public void testAddAdminSuccessfully() {
        int adminId = userRepository.addAdmin("newadmin@university.edu", "adminPass123");
        assertTrue(adminId > 0, "Newly added admin should return valid user ID");
        Optional<User> userOpt = userRepository.findByEmail("newadmin@university.edu");
        assertTrue(userOpt.isPresent(), "Newly added admin should exist");
        User user = userOpt.get();
        assertEquals("newadmin@university.edu", user.getEmail());
        assertEquals("admin", user.getUserType());
        assertEquals("adminPass123", user.getPassword());
        assertTrue(user.isAdmin());
        assertFalse(user.isStudent());
    }

    @Test
    public void testAddAdminWithDuplicateEmail() {
        int firstId = userRepository.addAdmin("admindup@university.edu", "password123");
        assertTrue(firstId > 0, "First admin should be added successfully");
        
        int secondId = userRepository.addAdmin("admindup@university.edu", "password456");
        assertEquals(-1, secondId, "Adding admin with duplicate email should return -1");
    }

    @Test
    public void testAddMultipleStudents() {
        int id1 = userRepository.addStudent("student1@university.edu", "pass1");
        int id2 = userRepository.addStudent("student2@university.edu", "pass2");
        int id3 = userRepository.addStudent("student3@university.edu", "pass3");

        assertTrue(id1 > 0);
        assertTrue(id2 > 0);
        assertTrue(id3 > 0);
        assertNotEquals(id1, id2);
        assertNotEquals(id2, id3);
        assertNotEquals(id1, id3);
        
        assertTrue(userRepository.findByEmail("student1@university.edu").isPresent());
        assertTrue(userRepository.findByEmail("student2@university.edu").isPresent());
        assertTrue(userRepository.findByEmail("student3@university.edu").isPresent());
    }

    @Test
    public void testFindByEmailWithValidStudentEmail() {
        int userId = userRepository.addStudent("student@university.edu", "password123");
        assertTrue(userId > 0, "Student should be added successfully");

        Optional<User> userOpt = userRepository.findByEmail("student@university.edu");
        assertTrue(userOpt.isPresent(), "User should be found in database");
        User user = userOpt.get();
        assertEquals("student@university.edu", user.getEmail());
        assertEquals("student", user.getUserType());
        assertEquals("password123", user.getPassword());
        assertEquals(userId, user.getUserId());
    }

    @Test
    public void testFindByEmailWithNonExistentEmail() {
        Optional<User> userOpt = userRepository.findByEmail("nonexistent@university.edu");
        assertFalse(userOpt.isPresent(), "Non-existent user should return empty Optional");
    }

    @Test
    public void testFindByIdWithValidUserId() {
        int userId = userRepository.addStudent("student1@university.edu", "password123");
        assertTrue(userId > 0);

        Optional<User> userOpt = userRepository.findById(userId);
        assertTrue(userOpt.isPresent(), "User should be found by ID");
        User user = userOpt.get();
        assertEquals(userId, user.getUserId());
        assertEquals("student1@university.edu", user.getEmail());
        assertEquals("student", user.getUserType());
    }

    @Test
    public void testFindByIdWithNonExistentId() {
        Optional<User> userOpt = userRepository.findById(99999);
        assertFalse(userOpt.isPresent(), "Non-existent user ID should return empty Optional");
    }

    @Test
    public void testUpdatePasswordSuccessfully() {
        int userId = userRepository.addStudent("student@university.edu", "oldPassword");
        assertTrue(userId > 0, "Student should be added successfully");

        boolean updated = userRepository.updatePassword(userId, "newPassword123");
        assertTrue(updated, "Update should succeed");
        
        Optional<User> userOpt = userRepository.findByEmail("student@university.edu");
        assertTrue(userOpt.isPresent());
        assertEquals("newPassword123", userOpt.get().getPassword());
    }

    @Test
    public void testUpdatePasswordForNonExistentUser() {
        boolean updated = userRepository.updatePassword(99999, "newPassword");
        assertFalse(updated, "Updating password for non-existent user should return false");
    }

    @Test
    public void testUpdatePasswordMultipleTimes() {
        int userId = userRepository.addStudent("student@university.edu", "password1");
        assertTrue(userId > 0);

        boolean update1 = userRepository.updatePassword(userId, "password2");
        boolean update2 = userRepository.updatePassword(userId, "password3");
        boolean update3 = userRepository.updatePassword(userId, "passwordFinal");

        assertTrue(update1);
        assertTrue(update2);
        assertTrue(update3);
        
        Optional<User> userOpt = userRepository.findByEmail("student@university.edu");
        assertTrue(userOpt.isPresent());
        assertEquals("passwordFinal", userOpt.get().getPassword());
    }

    @Test
    public void testDeleteUserSuccessfully() {
        int userId = userRepository.addStudent("student@university.edu", "password123");
        assertTrue(userId > 0, "Student should be added successfully");

        boolean deleted = userRepository.deleteUser(userId);
        assertTrue(deleted, "Delete should succeed");
        
        Optional<User> userOpt = userRepository.findByEmail("student@university.edu");
        assertFalse(userOpt.isPresent(), "Deleted user should not be found");
    }

    @Test
    public void testDeleteNonExistentUser() {
        boolean deleted = userRepository.deleteUser(99999);
        assertFalse(deleted, "Deleting non-existent user should return false");
    }

    @Test
    public void testDeleteAndReaddUser() {
        int userId1 = userRepository.addStudent("student@university.edu", "password123");
        assertTrue(userId1 > 0);

        boolean deleted = userRepository.deleteUser(userId1);
        assertTrue(deleted, "Delete should succeed");
        
        int userId2 = userRepository.addStudent("student@university.edu", "newPassword");
        assertTrue(userId2 > 0, "Re-added user should get valid ID");
        
        Optional<User> userOpt = userRepository.findByEmail("student@university.edu");
        assertTrue(userOpt.isPresent());
        User user = userOpt.get();
        assertEquals("newPassword", user.getPassword());
        assertEquals(userId2, user.getUserId());
    }

    @Test
    public void testUserIsStudentHelper() {
        int userId = userRepository.addStudent("student@university.edu", "password");
        assertTrue(userId > 0);
        
        Optional<User> userOpt = userRepository.findByEmail("student@university.edu");
        assertTrue(userOpt.isPresent());
        User user = userOpt.get();

        assertTrue(user.isStudent());
        assertFalse(user.isAdmin());
    }

    @Test
    public void testUserIsAdminHelper() {
        int adminId = userRepository.addAdmin("admin@university.edu", "password");
        assertTrue(adminId > 0);
        
        Optional<User> userOpt = userRepository.findByEmail("admin@university.edu");
        assertTrue(userOpt.isPresent());
        User user = userOpt.get();

        assertTrue(user.isAdmin());
        assertFalse(user.isStudent());
    }

    @Test
    public void testEmailWithSpecialCharacters() {
        int userId = userRepository.addStudent("student+test@university.co.uk", "password");
        assertTrue(userId > 0);

        Optional<User> userOpt = userRepository.findByEmail("student+test@university.co.uk");
        assertTrue(userOpt.isPresent());
        assertEquals("student+test@university.co.uk", userOpt.get().getEmail());
    }

    @Test
    public void testPasswordWithSpecialCharacters() {
        String complexPassword = "P@$$w0rd!#%&*<>?";
        int userId = userRepository.addStudent("student@university.edu", complexPassword);
        assertTrue(userId > 0);

        Optional<User> userOpt = userRepository.findByEmail("student@university.edu");
        assertTrue(userOpt.isPresent());
        assertEquals(complexPassword, userOpt.get().getPassword());
    }
}
