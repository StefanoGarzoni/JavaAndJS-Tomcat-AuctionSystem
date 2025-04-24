//Implementazione DAO Tabella Offerte

//package com.example.dao.impl;
//import com.example.dao.UsernameDAO;
//import com.example.util.ConnectionManager;
package DAO;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import DAO.OggettiEntita.Offerta;

public class OfferteDAOImpl implements OfferteDAO{

    @Override
    public ArrayList<Offerta> getOfferteMaxByUsername(String username) {
        ArrayList<Offerta> offerte = new ArrayList<>();
        String sql = "SELECT Offerte.* FROM Offerte JOIN Aste ON Offerte.id_offerta = Aste.offerta_max WHERE utente = ? ;";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
    public ArrayList<Offerta> getOfferteByIdAsta(int idAsta) {
        ArrayList<Offerta> offerte = new ArrayList<>();
        String sql = "SELECT * FROM Offerte WHERE id_asta = ?;";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
    public void insertNewOfferta(int idAsta, String username, double prezzo, Date data, Time ora) {
        String sql = "INSERT INTO Offerte (utente, id_asta, prezzo, data_offerta, ora_offerta) VALUES (?, ?, ?, CURDATE(), CURTIME());";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, idAsta);
            pstmt.setDouble(3, prezzo);
            //pstmt.setDate(4, data);
            //pstmt.setTime(5, ora);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    //richieste: SELECT utente, prezzo, data_offerta, ora_offerta
    public ArrayList<Offerta> getOfferteInOpenAsta(int idAsta) {
        ArrayList<Offerta> offerte = new ArrayList<>();
        String sql = "SELECT * FROM Offerte WHERE id_asta = ? AND chiusa = False ORDER BY data_offerta, ora_offerta;";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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