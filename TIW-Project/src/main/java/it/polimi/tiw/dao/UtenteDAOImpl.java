package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtenteDAOImpl{
	
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
}


