//Interfaccia DAO Tabella Offerte

package DAO;
import java.sql.Time;
import java.util.*;
import DAO.Beans.Offerta;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.Date;

public interface OfferteDAO{
    ArrayList<Offerta> getOfferteMaxByUsername(Connection conn, String username);
    ArrayList<Offerta> getOfferteByIdAsta(Connection conn, int idAsta);
    int insertNewOfferta(Connection conn, int idAsta, String username, double prezzo);
    ArrayList<Offerta> getOfferteInOpenAsta(Connection conn, int idAsta); 

}