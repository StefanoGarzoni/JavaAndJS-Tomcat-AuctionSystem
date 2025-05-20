package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.SQLException;

public interface LoginDAO {
    boolean areCredentialsCorrect(Connection conn, String username, String password) throws SQLException;
}
