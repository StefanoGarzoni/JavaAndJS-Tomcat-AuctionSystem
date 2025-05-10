package it.polimi.tiw.Servlets;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.OfferteDAOImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AddOffertaServlet extends HttpServlet {
	
	//consigliato da Eclipse
    private static final long serialVersionUID = 1L;

    private OfferteDAOImpl offerteDAO;
    private AsteDAOImpl asteDAO;

    public void init() throws ServletException {
        offerteDAO = new OfferteDAOImpl();
        asteDAO    = new AsteDAOImpl();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        
        //controllo l'esistenza della sessione e se l'username è in sessione, altrimenti rimando a login
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        //estraggo i dati dalle sessioni (non faccio il parse dato che i dati sono in sessione (sicuri)
        Integer idAsta = (Integer) session.getAttribute("idAsta");
        String  username = (String) session.getAttribute("username");
        if (idAsta == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Sessione mancante di dati. -> non c'è l'idAsta");
            return;
        }

        //estraggo dal get il prezzo (se non c'è, errore)
        String prezzoStr = request.getParameter("prezzo");
        if (prezzoStr == null || prezzoStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Prezzo mancante.");
            return;
        }

        //tento il parsing del prezzo, da stringa estratta dal get a double
        double prezzo;
        try {
            prezzo = Double.parseDouble(prezzoStr);
        } catch (NumberFormatException e) {
            
        	//reindirizzo alla pagina precedente dicendo: prezzo non valido
        	response.sendRedirect(request.getContextPath() + "/offertePage?PriceTooLow=True&idAsta="+idAsta);
            return;
        }
        
        //inizio delle query
        try (Connection conn = ConnectionManager.getConnection()) {
            try {
            	//recupero i prezzi dall'asta
                Map<Double, Double> prezziInfo = asteDAO.getPrezzoOffertaMaxANDRialzoMinimo(conn, idAsta);
                if (prezziInfo == null || prezziInfo.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Errore nel recuperare prezzi asta.");
                    return;
                }
                
                //salvo i dati dei prezzi appena estratti
                double rialzoMinimo = prezziInfo.keySet().iterator().next();
                double prezzoAttuale  = prezziInfo.get(rialzoMinimo);

                //controllo se il nuovo prezzo inserito è valido
                //se non lo è, reindirizzo dicendo di inserire un prezzo valido all'utente
                if ((prezzo <= prezzoAttuale || (prezzo - prezzoAttuale) < rialzoMinimo)) {
                    response.sendRedirect(request.getContextPath() + "/offertePage?PriceTooLow=True&idAsta="+idAsta);
                    return;
                }

                int idOfferta;
                conn.setAutoCommit(false);
                try{
                	//inserisco la nuova offerta e controllo che vada tutto bene
                	 idOfferta = offerteDAO.insertNewOfferta(conn, idAsta, username, prezzo);
                	 
                	 if (idOfferta == -1) {
                		 //se l'inserimento non va a buon fine, si esegue il rollback e si comunica l'errore
                         conn.rollback();
                         response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Inserimento offerta fallito.");
                         return;
                     }
                	 
                	 //aggiorno l'offerta massima
                     asteDAO.setOffertaMax(conn, idAsta, idOfferta);
                     conn.commit();
                	 
                }catch(SQLException e) {
                	//se non va a buon fine annullo le modifiche precedenti e comunico un errore
                	conn.rollback();
                    throw new ServletException("Errore nella esecuzione dell'inserimento in db", e);
                }
                
                //reindirizzo alla servlet che ricostruisce la pagina
                response.sendRedirect(request.getContextPath() + "/offertePage?idAsta="+idAsta);
                
            } catch (Exception e) {
            	//se va male qualcosa annullo e mando l'errore
                conn.rollback();
                throw new ServletException("Errore nella gestione dell'offerta", e);
                
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
        	//se va male qualcosa annullo e mando l'errore
            throw new ServletException("Errore di connessione al database", e);
        }
    }
}
