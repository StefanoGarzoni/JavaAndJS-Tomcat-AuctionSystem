//Implementazione DAO Tabella Aste

package it.polimi.tiw.dao;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import it.polimi.tiw.dao.Beans.Articolo;
import it.polimi.tiw.dao.Beans.Asta;

public class AsteDAOImpl implements AsteDAO{
	
	@Override
	public Double getRialzoMinimo(Connection conn, int idAsta) throws SQLException {
		String query = "SELECT rialzo_minimo FROM Aste WHERE id_asta = ?;";
        
		PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

    	ps.setInt(1, idAsta);
        ResultSet result = ps.executeQuery();
        if (result.next()) {
        	return result.getDouble("rialzo_minimo");
        }
        return null;
    }
	
    @Override
    public int insertNewAsta(Connection conn, String usernameCreatore, double prezzoIniziale, double rialzoMinimo, Date dataScadenza, Time oraScadenza) throws SQLException {
        String query = "INSERT INTO Aste (creatore, prezzo_iniziale, rialzo_minimo, data_scadenza, ora_scadenza) VALUES (?, ?, ?, ?, ?);";
        
        // impostando RETURN_GENERATED_KEYS ci viene restituito l'id dell'asta creato da AUTO_INCREMENT
    	PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    	
        ps.setString(1, usernameCreatore);
        ps.setDouble(2, prezzoIniziale);
        ps.setDouble(3, rialzoMinimo);
        ps.setDate(4, dataScadenza);
        ps.setTime(5, oraScadenza);
        
        int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Inserimento fallito, nessuna riga aggiunta.");
        }

        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next())
        	return generatedKeys.getInt(1);		//nuovo id AUTO_INCREMENT generato            	
        else
            throw new SQLException("Inserimento riuscito ma nessun ID ottenuto.");
    }

    @Override
    public ArrayList<Asta> getAllClosedAsteInfoByCreator(Connection conn, String usernameCreatore) throws SQLException {
    	String query = 
        		"SELECT id_asta, prezzo_iniziale, rialzo_minimo, data_scadenza, ora_scadenza, offerta_max "
        		+ "FROM Aste "
        		+ "WHERE creatore = ? AND chiusa = True "
        		+ "ORDER BY data_scadenza, ora_scadenza";
        
        ArrayList<Asta> aste = new ArrayList<>();
        
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, usernameCreatore);
        
        ResultSet result = ps.executeQuery();
        while (result.next()) {
        	ArrayList<Articolo> articoli = new ArticoliDAOImpl().getArticoliByIdAsta(conn, result.getInt("id_asta"));
        	
            Asta asta = new Asta(
                result.getInt("id_asta"),
                usernameCreatore,
                result.getDouble("prezzo_iniziale"),
                result.getDouble("rialzo_minimo"),
                result.getDate("data_scadenza"),
                result.getTime("ora_scadenza"),
                result.getInt("offerta_max"),
                true,
                articoli
            );
            aste.add(asta);
        }
        return aste;
    }

    @Override
    public ArrayList<Asta> getAllOpenAsteInfoByCreator(Connection conn, String usernameCreatore) throws SQLException {
        String query = 
        		"SELECT id_asta, prezzo_iniziale, rialzo_minimo, data_scadenza, ora_scadenza, offerta_max "
        		+ "FROM Aste "
        		+ "WHERE creatore = ? AND chiusa = False "
        		+ "ORDER BY data_scadenza, ora_scadenza;";
        
        ArrayList<Asta> aste = new ArrayList<>();
        
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, usernameCreatore);
        
        ResultSet result = ps.executeQuery();
        while (result.next()) {
        	ArrayList<Articolo> articoli = new ArticoliDAOImpl().getArticoliByIdAsta(conn, result.getInt("id_asta"));
        	
            Asta asta = new Asta(
                result.getInt("id_asta"),
                usernameCreatore,
                result.getDouble("prezzo_iniziale"),
                result.getDouble("rialzo_minimo"),
                result.getDate("data_scadenza"),
                result.getTime("ora_scadenza"),
                result.getInt("offerta_max"),
                false,
                articoli
            );
            aste.add(asta);
        }
        return aste;
    }
    
    @Override
    public ArrayList<Asta> getAsteByStringInArticoli(Connection conn, String stringaDiRicerca, String username) throws SQLException {
        String query = "SELECT Aste.*"
        		+ "FROM Aste "
        		+ "WHERE creatore NOT IN (?) AND"
        		+ "(data_scadenza > CURDATE() OR "
        		+ "		(data_scadenza = CURDATE() AND ora_scadenza > CURTIME())) AND "
        		+ "		EXISTS( "
        		+ "			SELECT * "
        		+ "			FROM Articoli "
        		+ "			WHERE Aste.id_asta = Articoli.id_asta AND "
        		+ "				(descrizione LIKE ? OR nome LIKE ?) "
        		+ "		) "
        		+ "ORDER BY data_scadenza DESC, ora_scadenza DESC;";
        ArrayList<Asta> aste = new ArrayList<>();

        PreparedStatement ps = conn.prepareStatement(query);
    	ps.setString(1, username);
        ps.setString(2, "%" + stringaDiRicerca + "%");
        ps.setString(3, "%" + stringaDiRicerca + "%");
        
        ResultSet result = ps.executeQuery();
        while (result.next()) {
        	ArrayList<Articolo> articoli = new ArticoliDAOImpl().getArticoliByIdAsta(conn, result.getInt("id_asta"));
        	
            Asta asta = new Asta(
                result.getInt("id_asta"),
                result.getString("creatore"),
                result.getDouble("prezzo_iniziale"),
                result.getDouble("rialzo_minimo"),
                result.getDate("data_scadenza"),
                result.getTime("ora_scadenza"),
                result.getInt("offerta_max"),
                result.getBoolean("chiusa"),
                articoli
            );
            aste.add(asta);
        }
        return aste;
    }

    @Override
    public void setOffertaMax(Connection conn, int idAsta, int idOfferta) throws SQLException {
        String query = "UPDATE Aste SET offerta_max = ? WHERE id_asta = ?;";
        
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, idOfferta);
        ps.setInt(2, idAsta);
        ps.executeUpdate();
    }

    @Override
    public Map<Double,Double> getPrezzoOffertaMaxANDRialzoMinimo(Connection conn, int idAsta) throws SQLException {
        String existsOffertaMaxQuery = "SELECT rialzo_minimo, offerta_max, prezzo_iniziale FROM Aste WHERE id_asta = ?";
    	
        // è necessaria una query a parte poichè, nel caso in cui un'asta non abbia ancora offerte, 
        // la join tra offerta e asta (che andrebbe inserita per unificare le due query) non restituirebbe una tupla
        String prezzoMaxOffertaQuery = "SELECT prezzo FROM Offerte WHERE id_offerta = ? ;";
    	
		PreparedStatement ps1 = conn.prepareStatement(existsOffertaMaxQuery);
        ps1.setInt(1, idAsta);
        
        ResultSet result = ps1.executeQuery();
        if (result.next()) {
        	Double rialzoMinimo = result.getDouble("rialzo_minimo");
        	Double prezzo = result.getDouble("prezzo_iniziale");
        	
        	if(result.getInt("offerta_max") != 0) {		// se è già stata fatta un'offerta (offerta_max == NULL, quindi getInt() = 0), estraiamo l'offerta massima
        		PreparedStatement ps2 = conn.prepareStatement(prezzoMaxOffertaQuery);
                ps2.setInt(1, result.getInt("offerta_max"));
                ResultSet result2 = ps2.executeQuery();
               
                if(result2.next()) {
                	prezzo = result2.getDouble("prezzo");	// sovrasciviamo il prezo_iniziale con l'offerta massima
                }
        	}
            
        	Map<Double, Double> results = new HashMap<>();
        	results.put(rialzoMinimo, prezzo);
            
            return results;
        }
        else {
        	return null;	// non esiste un'asta con quell'id      	
        }
    }

    @Override
    public Asta getOpenAstaById(Connection conn, int idAsta) throws SQLException {
        String query = "SELECT * FROM Aste WHERE id_asta = ? AND chiusa = False;";
        
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, idAsta);
        ResultSet result = ps.executeQuery();
        if (result.next()) {
            return new Asta(
                result.getInt("id_asta"),
                result.getString("creatore"),
                result.getDouble("prezzo_iniziale"),
                result.getDouble("rialzo_minimo"),
                result.getDate("data_scadenza"),
                result.getTime("ora_scadenza"),
                result.getInt("offerta_max"),
                result.getBoolean("chiusa"),
                null
            );
        }
        return null;
    }

    @Override
    public Asta getClosedAstaById(Connection conn, int idAsta) throws SQLException {
        String query = "SELECT * FROM Aste WHERE id_asta = ? AND chiusa = True;";
        
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, idAsta);
        ResultSet result = ps.executeQuery();
        if (result.next()) {
            return new Asta(
                result.getInt("id_asta"),
                result.getString("creatore"),
                result.getDouble("prezzo_iniziale"),
                result.getDouble("rialzo_minimo"),
                result.getDate("data_scadenza"),
                result.getTime("ora_scadenza"),
                result.getInt("offerta_max"),
                result.getBoolean("chiusa"),
                null
            );
        }
        return null;
    }

    @Override
    public boolean astaCanBeClosed(Connection conn, int idAsta) throws SQLException {
        String query = "SELECT * FROM Aste WHERE id_asta = ? AND chiusa = False AND (data_scadenza < CURDATE() OR (data_scadenza = CURDATE() AND ora_scadenza < CURTIME()));";
        
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, idAsta);
        ResultSet result = ps.executeQuery();
        if( result.next()) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    public void setAstaAsClosed(Connection conn, int idAsta, String username) throws SQLException {
        String query = "UPDATE Aste SET chiusa = True WHERE id_asta = ? AND creatore = ?;";
        
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, idAsta);
        ps.setString(2, username);
        ps.executeUpdate();
    }

    @Override
    public Map<Asta, ArrayList<String>> getInfoFromAClosedAsta(Connection conn, int idAsta) throws SQLException {
    	String queryCheckOfferte = "SELECT * FROM Offerte WHERE id_asta = ?";
    	String queryTakeAll = "SELECT Aste.*, Utenti.nome as nomeAggiudicatario, prezzo, indirizzo FROM Aste JOIN Offerte ON offerta_max = id_offerta JOIN Utenti ON utente = username WHERE Aste.id_asta = ? AND chiusa = True;";
    	String onlyAsta = "SELECT * FROM Aste WHERE id_asta = ?";
    	Map<Asta, ArrayList<String>> info = new HashMap<>();
    	try (PreparedStatement ps = conn.prepareStatement(queryCheckOfferte)) {
			ps.setInt(1, idAsta);
	        ResultSet result = ps.executeQuery();
			if (result.next()) {
				PreparedStatement ps1 = conn.prepareStatement(queryTakeAll);
				ps1.setInt(1, idAsta);
		        ResultSet result1 = ps1.executeQuery();
				
				if (result1.next()) {
	                Asta asta = new Asta(
	                    result1.getInt("id_asta"),
	                    result1.getString("creatore"),
	                    result1.getDouble("prezzo_iniziale"),
	                    result1.getDouble("rialzo_minimo"),
	                    result1.getDate("data_scadenza"),
	                    result1.getTime("ora_scadenza"),
	                    result1.getInt("offerta_max"),
	                    result1.getBoolean("chiusa"),
	                    null
	                );
	                ArrayList<String> altreInfo = new ArrayList<>();
	                altreInfo.add(result1.getString("nomeAggiudicatario"));
	                altreInfo.add(result1.getString("prezzo"));
	                altreInfo.add(result1.getString("indirizzo"));
	                info.put(asta, altreInfo);
	            }
    		}
			else {
				PreparedStatement ps1 = conn.prepareStatement(onlyAsta);
					ps1.setInt(1, idAsta);
			        ResultSet result1 = ps1.executeQuery();
					
					if (result1.next()) {
		                Asta asta = new Asta(
	                    result1.getInt("id_asta"),
	                    result1.getString("creatore"),
	                    result1.getDouble("prezzo_iniziale"),
	                    result1.getDouble("rialzo_minimo"),
	                    result1.getDate("data_scadenza"),
	                    result1.getTime("ora_scadenza"),
	                    result1.getInt("offerta_max"),
	                    result1.getBoolean("chiusa"),
	                    null
	                    );
		                ArrayList<String> altreInfo = new ArrayList<>();
		                altreInfo.add("Asta non venduta");
		                altreInfo.add("Prezzo non disponibile");
		                altreInfo.add("Indirizzo non disponibile");
		                info.put(asta, altreInfo);
					}
				}
			}
        return info;
    }

    @Override
    public boolean checkCreatorOfAsta(Connection conn, String username, int idAsta) throws SQLException {
        String query = "SELECT * FROM Aste WHERE id_asta = ? AND creatore = ?;";
        
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, idAsta);
        ps.setString(2, username);
        ResultSet result = ps.executeQuery();
        if (result.next()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean checkIfAstaIsOpen(Connection conn, int idAsta) throws SQLException {
        String query = "SELECT * FROM Aste WHERE id_asta = ? AND chiusa = False;";

        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, idAsta);
        ResultSet result = ps.executeQuery();
        if (result.next()) {
            return true;
        }

        return false;
    }

}
