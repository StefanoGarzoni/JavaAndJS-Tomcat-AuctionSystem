//Implementazione DAO Tabella Articoli

//package com.example.dao.impl;
//import com.example.dao.UsernameDAO;
//import com.example.util.ConnectionManager;

package it.polimi.tiw.DAO;
import it.polimi.tiw.DAO.Beans.Articolo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ArticoliDAOImpl implements ArticoliDAO{

    @Override
    public void insertNewArticolo(Connection conn, String usernameVenditore, String nomeArticolo, String descrizione, String imgPath, double prezzo) {
        String query = "INSERT INTO Articoli (venditore, nome, descrizione, img, prezzo) VALUES (?, ?, ?, ?, ?);";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, usernameVenditore);
            ps.setString(2, nomeArticolo);
            ps.setString(3, descrizione);
            ps.setString(4, imgPath);
            ps.setDouble(5, prezzo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore in insertNewArticolo", e);
        }
    }

    @Override
    public ArrayList<Articolo> getMyArticoli(Connection conn, String usernameVenditore) {
        String query = "SELECT * FROM Articoli WHERE venditore = ? AND venduto = False AND id_asta IS NULL;";
        ArrayList<Articolo> articoli = new ArrayList<>();
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, usernameVenditore);
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                Articolo articolo = new Articolo(
                    result.getInt("cod"),
                    result.getString("nome"),
                    result.getString("descrizione"),
                    result.getString("img"),
                    result.getDouble("prezzo"),
                    result.getString("venditore"),
                    result.getBoolean("venduto"),
                    result.getInt("id_asta")
                );
                articoli.add(articolo);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getMyArticoli", e);
        }
        return articoli;
    }

    @Override
    public ArrayList<Articolo> getArticoliByIdAsta(Connection conn, int idAsta) {
        String query = "SELECT * FROM Articoli WHERE idAsta = ?;";
        ArrayList<Articolo> articoli = new ArrayList<>();
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, idAsta);
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                Articolo articolo = new Articolo(
                    result.getInt("cod"),
                    result.getString("nome"),
                    result.getString("descrizione"),
                    result.getString("img"),
                    result.getDouble("prezzo"),
                    result.getString("venditore"),
                    result.getBoolean("venduto"),
                    result.getInt("id_asta")
                );
                articoli.add(articolo);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getArticoliByIdAsta", e);
        }
        return articoli;
    }

 }