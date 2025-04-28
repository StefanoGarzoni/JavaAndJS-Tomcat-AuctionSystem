//Implementazione DAO Tabella Utenti

package it.polimi.tiw.DAO;
import java.sql.*;

public class UtentiDAOImpl implements UtentiDAO{

	@Override
	public String checkUsernameAndPsw(String username, String psw){
		String query = "SELECT username FROM Utenti WHERE username = ? AND psw = ? ;";
		try (
			Connection conn = ConnectionManager.getConnection();
	    PreparedStatement ps = conn.prepareStatement(query)
		    )
	    {
		    ps.setString(1, username);
		    ps.setString(2, psw);
		    try(
			    ResultSet result = ps.executeQuery()
			    )
			    {
				    if(result.next()){
					    return result.getString("username");
				    }
			    }
			 }
			 catch (SQLException e){
				 throw new RuntimeException("Errore in checkUsernameAndPsw", e);
			 }
			 return "";
	}
	
	
}