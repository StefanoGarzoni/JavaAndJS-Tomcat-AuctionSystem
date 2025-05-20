package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.SQLException;

public interface UtenteDAO {
    boolean areCredentialsCorrect(Connection conn, String username, String password) throws SQLException;
    
    Boolean userLastActionWasAddedAsta(Connection conn, String username) throws SQLException;
    
    void setUserLastActionWasAddedAsta(Connection conn, String username, Boolean flagValue) throws SQLException;
}