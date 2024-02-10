package bg.sofia.uni.fmi.mjt.cryptowallet.database;

import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.cryptowallet.user.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class UserSet implements AutoCloseable {

    private final String usersFilePath;
    private final Set<User> users;

    public UserSet(String usersFilePath) {
        this.usersFilePath = usersFilePath;
        users = new HashSet<>();
        loadData(usersFilePath);
    }

    @SuppressWarnings("checkstyle:EmptyBlock")
    public void loadData(String path) {
        try {
            Files.createDirectories(Paths.get("database"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Path userPath = Path.of(path);
        if (Files.exists(userPath)) {
            try (var objectInputStream = new ObjectInputStream(Files.newInputStream(userPath))) {
                Object userObject;
                while ((userObject = objectInputStream.readObject()) != null) {
                    users.add((User) userObject);
                }
            } catch (EOFException e) {
                //otherwise it crashes when end of file is reached
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("The files does not exist", e);
            } catch (IOException e) {
                throw new IllegalStateException("A problem occurred while reading from a file", e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                Files.createFile(userPath);
            } catch (IOException e) {
                Logs.logErrorWithStackTrace(e.getStackTrace(), "Could not create file to save data to.");
            }
        }
    }

    private void saveData(String path) {
        Path userPath = Path.of(path);

        try (var objectOutputStream = new ObjectOutputStream(Files.newOutputStream(userPath))) {
            for (User user : users) {
                objectOutputStream.writeObject(user);
                objectOutputStream.flush();
            }
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while saving users to file", e);
        }
    }

    @Override
    public void close() {
        saveData(usersFilePath);
    }

    public void addUser(User toAdd) throws UserAlreadyExistsException {
        for (User u : users) {
            if (u.getUsername().equals(toAdd.getUsername())) {
                throw new UserAlreadyExistsException("User already exists.");
            }
        }

        users.add(toAdd);
    }

    public Set<User> getUsers() {
        return users;
    }
}
