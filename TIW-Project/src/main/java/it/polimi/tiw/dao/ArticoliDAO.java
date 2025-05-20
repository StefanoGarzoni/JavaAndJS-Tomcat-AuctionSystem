//Interfaccia DAO Tabella Articoli

package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import it.polimi.tiw.dao.Beans.Articolo;

public interface ArticoliDAO{
	void insertNewArticolo(Connection conn, String usernameVenditore, String nomeArticolo, String descrizione, String imgPath, double prezzo) throws SQLException;
	ArrayList<Articolo> getMyArticoli(Connection conn, String usernamaVenditore) throws SQLException;
	ArrayList<Articolo> getArticoliByIdAsta(Connection conn, int idAsta) throws SQLException;
	boolean areAllArticlesOfUser(Connection conn, String usernameVenditore, ArrayList<Integer> idArticoli) throws SQLException;
	int getSumOfPrice(Connection conn, ArrayList<Integer> articles) throws SQLException;
	void updateIdAstaInArticles(Connection conn, ArrayList<Integer> articles, int idAsta) throws SQLException;
	boolean areAllArticlesFree(Connection conn, ArrayList<Integer> idArticoliToInsertInAsta) throws SQLException;
}