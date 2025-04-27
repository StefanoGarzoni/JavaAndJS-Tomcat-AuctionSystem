package Servlet;

import DAO.ArticoliDAOImpl;
import DAO.AsteDAO;
import DAO.AsteDAOImpl;
import DAO.OfferteDAOImpl;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import ConnectionManager;

@WebServlet("/offerta/add")
public class addOffertaServlet extends HttpServlet {

    private OfferteDAOImpl offerteDAO;
    private ArticoliDAOImpl articoliDAO;
    private AsteDAO asteDAO;

    @Override
    public void init() throws ServletException {
        offerteDAO = new OfferteDAOImpl();
        articoliDAO = new ArticoliDAOImpl();
        asteDAO = new AsteDAOImpl();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessione non valida.");
            return;
        }

        // Prende l'idAsta e username dalla sessione
        Integer idAsta = (Integer) session.getAttribute("idAsta");
        String username = (String) session.getAttribute("username");

        if (idAsta == null || username == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Sessione mancante di dati.");
            return;
        }

        // Prende il prezzo dal form
        String prezzoStr = request.getParameter("prezzo");
        if (prezzoStr == null || prezzoStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Prezzo mancante.");
            return;
        }
        double prezzo;
        try {
            prezzo = Double.parseDouble(prezzoStr);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Prezzo non valido.");
            return;
        }

        // Connessione manuale per gestione transazione (atomicità)
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false); // inizio transazione

            try {
                Map<Double, Double> prezziInfo = asteDAO.getPrezzoOffertaMaxANDRialzoMinimo(conn, idAsta);
                if (prezziInfo == null || prezziInfo.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel recuperare prezzi asta.");
                    return;
                }

                double prezzoAttuale = prezziInfo.keySet().iterator().next();
                double rialzoMinimo = prezziInfo.get(prezzoAttuale);

                if (prezzo <= prezzoAttuale || (prezzo - prezzoAttuale) < rialzoMinimo) {
                    conn.rollback(); // rollback transazione
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Prezzo offerta troppo basso.");
                    return;
                }

                // Inserisce nuova offerta
                int idOfferta = offerteDAO.insertNewOfferta(conn, idAsta, username, prezzo);
                if (idOfferta == -1) {
                    conn.rollback();
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Inserimento offerta fallito.");
                    return;
                }

                // Aggiorna offerta massima
                asteDAO.setOffertaMax(conn, idAsta, idOfferta);

                conn.commit(); // commit transazione

                // Redirect o successo
                response.sendRedirect(request.getContextPath() + "/offerta/page");
            } catch (Exception e) {
                conn.rollback();
                throw new ServletException("Errore nella gestione dell'offerta", e);
            } finally {
                conn.setAutoCommit(true); // ripristina modalità normale
            }
        } catch (SQLException e) {
            throw new ServletException("Errore di connessione al database", e);
        }
    }
}
