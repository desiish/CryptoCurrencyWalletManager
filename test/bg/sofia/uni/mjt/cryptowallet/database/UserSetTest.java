package bg.sofia.uni.mjt.cryptowallet.database;

import bg.sofia.uni.fmi.mjt.cryptowallet.database.UserSet;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.cryptowallet.user.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class UserSetTest {
    private static final String FILE_PATH = "usersTest.txt";
    private static final User U1 = new User("desi", "123");
    private static final User U2 = new User("test", "456");

    private static void createFile() {
        Path path = Path.of(FILE_PATH);
        try (var objectOutputStream = new ObjectOutputStream(Files.newOutputStream(path))) {
            objectOutputStream.writeObject(U1);
            objectOutputStream.flush();
            objectOutputStream.writeObject(U2);
            objectOutputStream.flush();
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while writing to a file", e);
        }
    }

    @BeforeAll
    public static void setup() {
        createFile();
    }

    @Test
    public void testLoadData() {
        try (UserSet set = new UserSet(FILE_PATH)) {
            assertNotNull(set.getUsers());
            assertEquals(2, set.getUsers().size());
            assertTrue(set.getUsers().contains(U1));
            assertTrue(set.getUsers().contains(U2));
        }
    }

    @Test
    public void testAddUser() {
        try (UserSet set = new UserSet(FILE_PATH)) {
            User userToAdd = new User("newUser", "789");
            set.addUser(userToAdd);
            assertTrue(set.getUsers().contains(userToAdd));
        } catch (UserAlreadyExistsException e) {
            fail();
        }
    }

    @Test
    public void testAddUserExisting() {
        try (UserSet set = new UserSet(FILE_PATH)) {
            assertThrows(UserAlreadyExistsException.class, () -> set.addUser(U1),
                "UserAlreadyExistsException was expected but was never thrown.");
        }
    }

    @AfterAll
    public static void teardown() {
        try {
            Files.delete(Path.of(FILE_PATH));
        } catch (IOException e) {
            throw new RuntimeException("Deleting of accounts.dat and wallets.dat failed.");
        }
    }
}
