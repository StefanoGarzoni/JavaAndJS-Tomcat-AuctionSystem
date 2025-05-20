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
		
		PreparedStatement pStatement = conn.prepareStatement(queryString);
		pStatement.setString(1, username);
		pStatement.setString(2, password);
	
		ResultSet resSet = pStatement.executeQuery();
		resSet.next();
		return resSet.getInt(1) > 0;
	}

	public Boolean userLastActionWasAddedAsta(Connection conn, String username) throws SQLException {
		String queryString = "SELECT ultima_azione_astaAggiunta"
				+ "	FROM Utenti"
				+ "	WHERE username = ?";
		
		PreparedStatement pStatement = conn.prepareStatement(queryString);
		pStatement.setString(1, username);
		
		ResultSet resSet = pStatement.executeQuery();
		if(!resSet.next()) {
			throw new SQLException("Errore nell'accesso al DB");
		}
		else {
			boolean userLastActionWasAddedAsta = resSet.getBoolean("ultima_azione_astaAggiunta");
			if(resSet.wasNull())
				return null;		// se l'azione è null => l'utente non ha mai utilizzato l'applicazione
			else
				return userLastActionWasAddedAsta;		// se l'azione è != null => restituisce il flag true/false
		}
	}
	
	 public void setUserLastActionWasAddedAsta(Connection conn, String username, Boolean flagValue) throws SQLException {
		String queryString = "UPDATE utenti SET ultima_azione_astaAggiunta = ? WHERE username = ?";
			
		PreparedStatement pStatement = conn.prepareStatement(queryString);
		pStatement.setBoolean(1, flagValue);
		pStatement.setString(2, username);
		
		pStatement.executeUpdate();
	 }
}


