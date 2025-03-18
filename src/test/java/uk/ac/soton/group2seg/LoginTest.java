package uk.ac.soton.group2seg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import uk.ac.soton.group2seg.controller.LoginController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class LoginTest {

    private LoginController loginController;

    @BeforeEach
    void setUp() {
        loginController = new LoginController();
    }

    @Test
    void testHashPassword() {
        String password = "SecurePass123!";
        String hashedPassword = loginController.hashPassword(password);
        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertNotEquals(password, hashedPassword, "Hashed password should be different from plain text");
    }

    @Test
    void testCheckPasswordCorrect() {
        String password = "SecurePass123!";
        String hashedPassword = loginController.hashPassword(password);
        assertTrue(loginController.checkPassword(password, hashedPassword), "Password should match the hash");
    }

    @Test
    void testCheckPasswordIncorrect() {
        String password = "SecurePass123!";
        String wrongPassword = "WrongPass123!";
        String hashedPassword = loginController.hashPassword(password);
        assertFalse(loginController.checkPassword(wrongPassword, hashedPassword), "Wrong password should not match the hash");
    }

    @Test
    void usernameUniquenessTest() {
        String firstUsername = null;
        try (Connection connection = loginController.connectToDatabase()){
            String query = "SELECT Username FROM Users LIMIT 1";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            firstUsername = resultSet.getString("Username");
            if (firstUsername != null) {
                boolean isUnique = loginController.usernameUnique(firstUsername);
                assertFalse(isUnique, "Detects if username is not unique");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void inValidAirportInput(){
        String notAnAirport = "NOTANAIRPORT";
        boolean resultOfAirportCheck = loginController.validAirport(notAnAirport);
        assertFalse(resultOfAirportCheck, "Airport should not be valid");
    }
}
