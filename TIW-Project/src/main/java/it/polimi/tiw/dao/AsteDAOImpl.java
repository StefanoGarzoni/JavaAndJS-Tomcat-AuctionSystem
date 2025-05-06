//Implementazione DAO Tabella Aste

package it.polimi.tiw.dao;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import it.polimi.tiw.dao.Beans.Articolo;
import it.polimi.tiw.dao.Beans.Asta;

public class AsteDAOImpl implements AsteDAO{
    @Override
    public int insertNewAsta(Connection conn, String usernameCreatore, double prezzoIniziale, double rialzoMinimo, Date dataScadenza, Time oraScadenza) throws SQLException{
        String query = "INSERT INTO Aste (creatore, prezzo_iniziale, rialzo_minimo, data_scadenza, ora_scadenza) VALUES (?, ?, ?, ?, ?);";
        try (
            PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, usernameCreatore);
            ps.setDouble(2, prezzoIniziale);
            ps.setDouble(3, rialzoMinimo);
            ps.setDate(4, dataScadenza);
            ps.setTime(5, oraScadenza);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserimento fallito, nessuna riga aggiunta.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);		//nuovo id AUTO_INCREMENT generato
                } else {
                    throw new SQLException("Inserimento riuscito ma nessun ID ottenuto.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in insertNewAsta", e);
        }
    }

    @Override
    public ArrayList<Asta> getAllClosedAsteInfoByCreator(Connection conn, String usernameCreatore) throws RuntimeException{
    	String query = 
        		"SELECT id_asta, prezzo_iniziale, rialzo_minimo, data_scadenza, ora_scadenza, offerta_max "
        		+ "FROM Aste "
        		+ "WHERE creatore = ? AND chiusa = True "
        		+ "ORDER BY data_scadenza, ora_scadenza";
        
        ArrayList<Asta> aste = new ArrayList<>();
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
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
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getAllClosedAsteInfoByCreator", e);
        }
        return aste;
    }

    @Override
    public ArrayList<Asta> getAllOpenAsteInfoByCreator(Connection conn, String usernameCreatore) {
        String query = 
        		"SELECT id_asta, prezzo_iniziale, rialzo_minimo, data_scadenza, ora_scadenza, offerta_max "
        		+ "FROM Aste "
        		+ "WHERE creatore = ? AND chiusa = False "
        		+ "ORDER BY data_scadenza, ora_scadenza;";
        
        ArrayList<Asta> aste = new ArrayList<>();
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
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
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getAllOpenAsteInfoByCreator", e);
        }
        return aste;
    }
    
    @Override
    public ArrayList<Asta> getAsteByStringInArticoli(Connection conn, String stringaDiRicerca, String username) {
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
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
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
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getAsteByStringInArticoli", e);
        }
        return aste;
    }

    @Override
    public void setOffertaMax(Connection conn, int idAsta, int idOfferta) {
        String query = "UPDATE Aste SET offerta_max = ? WHERE id_asta = ?;";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, idOfferta);
            ps.setInt(2, idAsta);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore in setOffertaMax", e);
        }
    }

    @Override
    public Map<Double,Double> getPrezzoOffertaMaxANDRialzoMinimo(Connection conn, int idAsta) {
        String existsOffertaMaxQuery = "SELECT rialzo_minimo, offerta_max, prezzo_iniziale FROM Aste WHERE id_asta = ?";
    	String prezzoMaxOffertaQuery = "SELECT prezzo FROM Offerte WHERE id_offerta = ? ;";
    	
    	try {
    		PreparedStatement ps1 = conn.prepareStatement(existsOffertaMaxQuery);
            ps1.setInt(1, idAsta);
            
            ResultSet result = ps1.executeQuery();
            if (result.next()) {
            	Double rialzoMinimo = result.getDouble("rialzo_minimo");
            	Double prezzo = result.getDouble("prezzo_iniziale");
            	
            	if(result.getInt("offerta_max") != 0) {
            		PreparedStatement ps2 = conn.prepareStatement(prezzoMaxOffertaQuery);
                    ps2.setInt(1, result.getInt("offerta_max"));
                    ResultSet result2 = ps2.executeQuery();
                   
                    if(result.next()) {
                    	prezzo = result2.getDouble("prezzo");
                    }
            	}
                
            	Map<Double, Double> results = new HashMap<>();
            	results.put(rialzoMinimo, prezzo);
                
                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getPrezzoOffertaMax", e);
        }
        return null;
    }

    @Override
    public Asta getOpenAstaById(Connection conn, int idAsta) {
        String query = "SELECT * FROM Aste WHERE id_asta = ? AND chiusa = False;";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
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
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getOpenAstaById", e);
        }
        return null;
    }

    @Override
    public Asta getClosedAstaById(Connection conn, int idAsta) {
        String query = "SELECT * FROM Aste WHERE id_asta = ? AND chiusa = True;";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
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
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getClosedAstaById", e);
        }
        return null;
    }

    @Override
    public boolean astaCanBeClosed(Connection conn, int idAsta) {
        String query = "SELECT * FROM Aste WHERE id_asta = ? AND chiusa = False AND (data_scadenza < CURDATE() OR (data_scadenza = CURDATE() AND ora_scadenza < CURTIME()));";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, idAsta);
            ResultSet result = ps.executeQuery();
            if( result.next()) {
                return true;
            }else {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in astaCanBeClosed", e);
        }
    }

    @Override
    public void setAstaAsClosed(Connection conn, int idAsta, String username) {
        String query = "UPDATE Aste SET chiusa = True WHERE id_asta = ? AND creatore = ?;";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, idAsta);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore in setAstaAsClosed", e);
        }
    }

    @Override
    public Map<Asta, ArrayList<String>> getInfoFromAClosedAsta(Connection conn, int idAsta) {
        String query = "SELECT Aste.*, Utenti.nome as nomeAggiudicatario, prezzo, indirizzo FROM Aste JOIN Offerte ON offerta_max = id_offerta JOIN Utenti ON utente = username WHERE Aste.id_asta = ? AND chiusa = True;";
        Map<Asta, ArrayList<String>> info = new HashMap<>();
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, idAsta);
            ResultSet result = ps.executeQuery();
            if (result.next()) {
                Asta asta = new Asta(
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
                ArrayList<String> altreInfo = new ArrayList<>();
                altreInfo.add(result.getString("nomeAggiudicatario"));
                altreInfo.add(result.getString("prezzo"));
                altreInfo.add(result.getString("indirizzo"));
                info.put(asta, altreInfo);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getInfoFromAClosedAsta", e);
        }
        return info;
    }

    @Override
    public boolean checkCreatorOfAsta(Connection conn, String username, int idAsta) {
        String query = "SELECT * FROM Aste WHERE id_asta = ? AND creatore = ?;";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, idAsta);
            ps.setString(2, username);
            ResultSet result = ps.executeQuery();
            if (result.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in checkCreatorOfAsta", e);
        }
        return false;
    }

}
