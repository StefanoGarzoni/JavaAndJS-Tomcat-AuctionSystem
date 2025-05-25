package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtenteDAOImpl implements UtenteDAO{
	
	public boolean areCredentialsCorrect(Connection conn, String username, String password) throws SQLException{
		String queryString = "SELECT count(*) AS numRows"
				+ "	FROM Utenti"
				+ "	WHERE username = ? AND psw = ?";
		
		try(PreparedStatement pStatement = conn.prepareStatement(queryString)){
			pStatement.setString(1, username);
			pStatement.setString(2, password);
			
			try(ResultSet resSet = pStatement.executeQuery()){
				resSet.next();
				return resSet.getInt(1) > 0;				
			}
		}
	}

	public Boolean isUserPrimoAccesso(Connection conn, String username) throws SQLException {
		String queryString = "SELECT primo_accesso"
				+ "	FROM Utenti"
				+ "	WHERE username = ?";
		
		try(PreparedStatement pStatement = conn.prepareStatement(queryString)){
			pStatement.setString(1, username);
			
			try(ResultSet resSet = pStatement.executeQuery()){
				if(!resSet.next()) {
					throw new SQLException("Errore nell'accesso al DB");
				}
				else {
					boolean userLastActionWasAddedAsta = resSet.getBoolean("primo_accesso");
					return userLastActionWasAddedAsta;		
				}				
			}
		}
	}
	
	 public void setUserPrimoAccessoAtFalse(Connection conn, String username) throws SQLException {
		String queryString = "UPDATE utenti SET primo_accesso = FALSE WHERE username = ?";
			
		try(PreparedStatement pStatement = conn.prepareStatement(queryString)){
			pStatement.setString(1, username);
			
			pStatement.executeUpdate();			
		}
	 }
}


