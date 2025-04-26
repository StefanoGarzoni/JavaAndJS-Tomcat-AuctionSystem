//Interfaccia DAO Tabella Articoli

package DAO;
import java.sql.Connection;
import java.util.*;
import DAO.Beans.Articolo;

public interface ArticoliDAO{
	void insertNewArticolo(Connection conn, String usernameVenditore, String nomeArticolo, String descrizione, String imgPath, double prezzo);
	ArrayList<Articolo> getMyArticoli(Connection conn, String usernamaVenditore); //o facciamo un array di articoli?
	ArrayList<Articolo> getArticoliByIdAsta(Connection conn, int idAsta);
}