//Interfaccia DAO Tabella Articoli

package it.polimi.tiw.dao;

import java.sql.Connection;
import java.util.*;
import it.polimi.tiw.dao.Beans.Articolo;

public interface ArticoliDAO{
	void insertNewArticolo(Connection conn, String usernameVenditore, String nomeArticolo, String descrizione, String imgPath, double prezzo);
	ArrayList<Articolo> getMyArticoli(Connection conn, String usernamaVenditore); //o facciamo un array di articoli?
	ArrayList<Articolo> getArticoliByIdAsta(Connection conn, int idAsta);
	boolean areAllArticlesOfUser(Connection conn, String usernameVenditore, ArrayList<Integer> idArticoli);
	int getSumOfPrice(Connection conn, ArrayList<Integer> articles);
	public void updateIdAstaInArticles(Connection conn, ArrayList<Integer> articles, int idAsta);
}