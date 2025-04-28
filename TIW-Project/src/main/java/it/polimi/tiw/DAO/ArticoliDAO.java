//Interfaccia DAO Tabella Articoli

package it.polimi.tiw.DAO;
import it.polimi.tiw.DAO.Beans.Articolo;
import java.sql.Connection;
import java.util.*;


public interface ArticoliDAO{
	void insertNewArticolo(Connection conn, String usernameVenditore, String nomeArticolo, String descrizione, String imgPath, double prezzo);
	ArrayList<Articolo> getMyArticoli(Connection conn, String usernamaVenditore); //o facciamo un array di articoli?
	ArrayList<Articolo> getArticoliByIdAsta(Connection conn, int idAsta);
}