//Implementazione DAO Tabella Articoli

//package com.example.dao.impl;
//import com.example.dao.UsernameDAO;
//import com.example.util.ConnectionManager;
package it.polimi.tiw.dao;

import java.sql.*;
import java.util.*;
import it.polimi.tiw.dao.Beans.Articolo;

public class ArticoliDAOImpl implements ArticoliDAO{

    public boolean areAllArticlesOfUser(Connection conn, String usernameVenditore, ArrayList<Integer> idArticoli) {
    	String query = "SELECT cod FROM Articoli WHERE venditore = ?";
        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
           ps.setString(1, usernameVenditore);
           ResultSet resultSet = ps.executeQuery();
           
           // trasformo il result set in un'array di integer (lista di codici dell'utente)
           Set<Integer> userArticles = new HashSet<>();
           while (resultSet.next()) {
        	   userArticles.add(resultSet.getInt("cod"));
           }

           // appena uno degli articoli da inserire non è tra quelli dell'utente, restituisce false
           for (int idArticolo : idArticoli) {
               if (!userArticles.contains(idArticolo)) {
                   return false;
               }
           }
           return true;

           
           
        } catch (SQLException e) {
            throw new RuntimeException("Errore in insertNewArticolo", e);
        }
    }
    
    // restituisce true se tutti gli articoli non sono ancora in un'asta
    public boolean areAllArticlesFree(Connection conn, ArrayList<Integer> idArticoliToInsertInAsta) throws SQLException {
    	String query = "SELECT count(*) AS notFreeArticles "
    			+ "FROM Articoli "
    			+ "WHERE id_asta IS NOT NULL "
    			+ "AND cod IN (";
    	for(int i = 0; i < idArticoliToInsertInAsta.size(); i++) {		// i dati presenti in idArticoliToInsertInAsta sono sanificati e non si rischia SQL injection
    		query += idArticoliToInsertInAsta.get(i);
    		if(i < idArticoliToInsertInAsta.size() - 1) {
    			query += ", ";
    		}
    	}
    	query += ")";
    	
    	PreparedStatement ps = conn.prepareStatement(query);
    	ResultSet resultSet = ps.executeQuery();
       
    	
    	if (resultSet.next() && resultSet.getInt("notFreeArticles") > 0) {	// false se almeno un articolo non è libero
    		return false;
    	}
    	return true;
    }
	
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
	
	public int getSumOfPrice(Connection conn, ArrayList<Integer> articles) {
		if (articles.isEmpty()) {
	        return 0; 	// nessun elemento da sommare
	    }
		
	    StringBuilder query = new StringBuilder("SELECT SUM(prezzo) FROM Articoli WHERE cod IN (");
	    
	    for (int i = 0; i < articles.size(); i++) {
	        query.append(articles.get(i));
	        if (i < articles.size() - 1) {
	            query.append(", ");
	        }
	    }
	    query.append(");");
		
	    try (
            PreparedStatement stmt = conn.prepareStatement(query.toString());
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getSumOfPrice", e);
        }
	}

	public void updateIdAstaInArticles(Connection conn, ArrayList<Integer> articles, int idAsta) {
		StringBuilder query = new StringBuilder("UPDATE Articoli SET id_asta = ? WHERE cod IN (");
			    
	    for (int i = 0; i < articles.size(); i++) {
	        query.append(articles.get(i));
	        if (i < articles.size() - 1) {
	            query.append(", ");
	        }
	    }
	    query.append(");");
		
	    try (
            PreparedStatement stmt = conn.prepareStatement(query.toString());
        ) {
	    	stmt.setInt(1, idAsta);
	    	stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore in getSumOfPrice", e);
        }
	}
	
    @Override
    public ArrayList<Articolo> getMyArticoli(Connection conn, String usernameVenditore) throws RuntimeException{
        String query = "SELECT * FROM Articoli WHERE venditore = ? AND venduto = False AND id_asta IS NULL;";
        ArrayList<Articolo> articoli = new ArrayList<Articolo>();
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
                    result.getDouble("prezzo")
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
        String query = "SELECT * FROM Articoli WHERE id_asta = ?;";
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