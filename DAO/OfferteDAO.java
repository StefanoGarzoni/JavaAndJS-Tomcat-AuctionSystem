//Interfaccia DAO Tabella Offerte

package DAO;
import java.sql.Time;
import java.util.*;
import DAO.OggettiEntita.Offerta;
import java.lang.reflect.Array;
import java.sql.Date;

public interface OfferteDAO{
    ArrayList<Offerta> getOfferteMaxByUsername(String username);
    ArrayList<Offerta> getOfferteByIdAsta(int idAsta);
    void insertNewOfferta(int idAsta, String username, double prezzo, Date data, Time ora);
    ArrayList<Offerta> getOfferteInOpenAsta(int idAsta); 

}