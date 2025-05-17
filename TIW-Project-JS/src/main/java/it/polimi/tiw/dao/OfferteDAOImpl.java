//Implementazione DAO Tabella Offerte

package it.polimi.tiw.dao;

import java.sql.*;
import java.util.*;
import java.sql.Date;

import it.polimi.tiw.dao.Beans.Offerta;

public class OfferteDAOImpl implements OfferteDAO{

    @Override
    public ArrayList<Offerta> getOfferteMaxByUsername(Connection conn, String username) {
        ArrayList<Offerta> offerte = new ArrayList<>();
        String sql = "SELECT Offerte.* FROM Offerte JOIN Aste ON Offerte.id_offerta = Aste.offerta_max WHERE utente = ? ;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Offerta offerta = new Offerta(
                    rs.getInt("id_offerta"), 
                    rs.getString("username"), 
                    rs.getInt("id_asta"),
                    rs.getDouble("prezzo"), 
                    rs.getDate("data_offerta"), 
                    rs.getTime("ora_offerta")
                    );
                offerte.add(offerta);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return offerte;
    }
    
	public ArrayList<Offerta> getAsteAggiudicateByUsername(Connection conn, String username) throws SQLException{
		String query = "SELECT O.id_asta AS id_asta, O.prezzo AS prezzo "
				+ "FROM Aste A"
				+ "		JOIN Offerte O ON A.id_asta = O.id_asta "
				+ "WHERE A.chiusa = 1 AND "
				+ "		A.offerta_max = O.id_offerta "
				+ "		AND O.utente = ?";
		
		ArrayList<Offerta> offerteAggiudicate = new ArrayList<>();
	        
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, username);
        
        ResultSet result = ps.executeQuery();
        while (result.next()) {
            Offerta offerta = new Offerta(
            	-1,
            	null,
            	result.getInt("id_asta"),
            	result.getDouble("prezzo"),
            	null,
            	null
            );
            offerteAggiudicate.add(offerta);
        }
        
        return offerteAggiudicate;
	}

    @Override
    public ArrayList<Offerta> getOfferteByIdAsta(Connection conn, int idAsta) {
        ArrayList<Offerta> offerte = new ArrayList<>();
        String sql = "SELECT * FROM Offerte WHERE id_asta = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idAsta);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Offerta offerta = new Offerta(
                    rs.getInt("id_offerta"), 
                    rs.getString("utente"), 
                    rs.getInt("id_asta"),
                    rs.getDouble("prezzo"), 
                    rs.getDate("data_offerta"), 
                    rs.getTime("ora_offerta")
                    );
                offerte.add(offerta);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return offerte;
    }

    @Override
    public int insertNewOfferta(Connection conn, int idAsta, String username, double prezzo, Date data, Time ora) {
        String sql = "INSERT INTO Offerte (utente, id_asta, prezzo, data_offerta, ora_offerta) VALUES (?, ?, ?, ?, ?);";
        int idGenerato = -1;
        try (PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) { //Statement.RETURN_GENERATED_KEYS
            pstmt.setString(1, username);
            pstmt.setInt(2, idAsta);
            pstmt.setDouble(3, prezzo);
            pstmt.setDate(4, data);
            pstmt.setTime(5, ora);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserimento offerta fallito, nessuna riga inserita.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idGenerato = generatedKeys.getInt(1); // Prende l'id_offerta generato
                } else {
                    throw new SQLException("Inserimento offerta fallito, nessun ID generato.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(idGenerato!=-1){
            String sql2 = "UPDATE Aste SET offerta_max = ? WHERE id_asta = ?;";
            try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                pstmt2.setInt(1, idGenerato);
                pstmt2.setInt(2, idAsta);
                pstmt2.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return idGenerato;
    }

    @Override
    //richieste: SELECT utente, prezzo, data_offerta, ora_offerta
    public ArrayList<Offerta> getOfferteInOpenAsta(Connection conn, int idAsta) {
        ArrayList<Offerta> offerte = new ArrayList<>();
        String sql = "SELECT Offerte.* FROM "
        		+ "Offerte JOIN Aste ON Offerte.id_asta = Aste.id_asta "
        		+ "WHERE Aste.id_asta = ? AND chiusa = False "
        		+ "ORDER BY data_offerta, ora_offerta;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idAsta);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Offerta offerta = new Offerta(
                    rs.getInt("id_offerta"), 
                    rs.getString("utente"), 
                    rs.getInt("id_asta"),
                    rs.getDouble("prezzo"), 
                    rs.getDate("data_offerta"), 
                    rs.getTime("ora_offerta")
                    );
                offerte.add(offerta);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return offerte;
    }

 }