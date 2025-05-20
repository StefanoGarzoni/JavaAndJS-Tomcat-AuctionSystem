//Implementazione DAO Tabella Offerte

package it.polimi.tiw.dao;

import java.sql.*;
import java.util.*;
import it.polimi.tiw.dao.Beans.Offerta;

public class OfferteDAOImpl implements OfferteDAO{

    @Override
    public ArrayList<Offerta> getOfferteMaxByUsername(Connection conn, String username) throws SQLException {
        ArrayList<Offerta> offerte = new ArrayList<>();
        String sql = "SELECT Offerte.* FROM Offerte JOIN Aste ON Offerte.id_offerta = Aste.offerta_max WHERE utente = ? ;";
        PreparedStatement pstmt = conn.prepareStatement(sql);
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
        return offerte;
    }

    @Override
    public ArrayList<Offerta> getOfferteByIdAsta(Connection conn, int idAsta) throws SQLException {
        ArrayList<Offerta> offerte = new ArrayList<>();
        String sql = "SELECT * FROM Offerte WHERE id_asta = ?;";
        
        PreparedStatement pstmt = conn.prepareStatement(sql);
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
        return offerte;
    }

    @Override
    public int insertNewOfferta(Connection conn, int idAsta, String username, double prezzo) throws SQLException {
        String sql = "INSERT INTO Offerte (utente, id_asta, prezzo, data_offerta, ora_offerta) VALUES (?, ?, ?, CURDATE(), CURTIME());";
        int idGenerato = -1;
        
        PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, username);
        pstmt.setInt(2, idAsta);
        pstmt.setDouble(3, prezzo);

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
        return idGenerato;
    }

    @Override
    //richieste: SELECT utente, prezzo, data_offerta, ora_offerta
    public ArrayList<Offerta> getOfferteInOpenAsta(Connection conn, int idAsta) throws SQLException {
        ArrayList<Offerta> offerte = new ArrayList<>();
        String sql = "SELECT Offerte.* FROM "
        		+ "Offerte JOIN Aste ON Offerte.id_asta = Aste.id_asta "
        		+ "WHERE Aste.id_asta = ? AND chiusa = False "
        		+ "ORDER BY data_offerta, ora_offerta;";
        
        PreparedStatement pstmt = conn.prepareStatement(sql);
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
        return offerte;
    }

 }