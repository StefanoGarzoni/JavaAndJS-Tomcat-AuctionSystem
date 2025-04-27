//Implementazione DAO Tabella Offerte

//package com.example.dao.impl;
//import com.example.dao.UsernameDAO;
//import com.example.util.ConnectionManager;
package DAO;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import DAO.Beans.Offerta;
import ConnectionManager;

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
    public int insertNewOfferta(Connection conn, int idAsta, String username, double prezzo) {
        String sql = "INSERT INTO Offerte (utente, id_asta, prezzo, data_offerta, ora_offerta) VALUES (?, ?, ?, CURDATE(), CURTIME());";
        int idGenerato = -1;
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idGenerato;
    }

    @Override
    //richieste: SELECT utente, prezzo, data_offerta, ora_offerta
    public ArrayList<Offerta> getOfferteInOpenAsta(Connection conn, int idAsta) {
        ArrayList<Offerta> offerte = new ArrayList<>();
        String sql = "SELECT * FROM Offerte WHERE id_asta = ? AND chiusa = False ORDER BY data_offerta, ora_offerta;";
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