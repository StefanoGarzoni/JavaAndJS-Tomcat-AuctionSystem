//Interfaccia Tabella Articoli
package DAO;
import java.util.*;

public interface ArticoliDAO{
	void insertNewArticolo(String usernameVenditore, String nomeArticolo, String descrizione, String imgPath, double prezzo);
	ArrayList<Articolo> getMyArticoli(String usernamaVenditore); //o facciamo un array di articoli?
	ArrayList<Articolo> getArticoliByIdAsta(int idAsta);
}