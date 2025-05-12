//Interfaccia DAO Tabella Offerte

package it.polimi.tiw.dao;


import java.util.*;
import it.polimi.tiw.dao.Beans.Offerta;
import java.sql.Connection;
import java.sql.Time;
import java.sql.Date;

public interface OfferteDAO{
    ArrayList<Offerta> getOfferteMaxByUsername(Connection conn, String username);
    ArrayList<Offerta> getOfferteByIdAsta(Connection conn, int idAsta);
    int insertNewOfferta(Connection conn, int idAsta, String username, double prezzo, Date data, Time ora);
    ArrayList<Offerta> getOfferteInOpenAsta(Connection conn, int idAsta); 

}