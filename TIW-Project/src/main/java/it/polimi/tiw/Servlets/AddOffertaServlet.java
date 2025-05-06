package it.polimi.tiw.Servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.ArticoliDAOImpl;
import it.polimi.tiw.dao.AsteDAO;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.OfferteDAOImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AddOffertaServlet extends HttpServlet {

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
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessione non valida.");
            return;
        }

        
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            // response.sendRedirect(request.getContextPath() + "/login?loginError=true");
            return;
        }

        Integer idAsta = (Integer) session.getAttribute("idAsta");
        String  username = (String) session.getAttribute("username");
        if (idAsta == null || username == null) {
            //response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Sessione mancante di dati.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String prezzoStr = request.getParameter("prezzo");
        if (prezzoStr == null || prezzoStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Prezzo mancante.");
            return;
        }

        double prezzo;
        try {
            prezzo = Double.parseDouble(prezzoStr);
        } catch (NumberFormatException e) {
            //response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Prezzo non valido.");
        	response.sendRedirect(request.getContextPath() + "/offertePage?PriceTooLow=True&idAsta="+idAsta);
            return;
        }

        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Map<Double, Double> prezziInfo =
                    asteDAO.getPrezzoOffertaMaxANDRialzoMinimo(conn, idAsta);
                if (prezziInfo == null || prezziInfo.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Errore nel recuperare prezzi asta.");
                    return;
                }

                double rialzoMinimo = prezziInfo.keySet().iterator().next();
                double prezzoAttuale  = prezziInfo.get(rialzoMinimo);

                if ((prezzo <= prezzoAttuale || (prezzo - prezzoAttuale) < rialzoMinimo)) {
                    conn.rollback();
                    response.sendRedirect(request.getContextPath() + "/offertePage?PriceTooLow=True&idAsta="+idAsta);
                    //response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Prezzo offerta troppo basso.");
                    return;
                }

                int idOfferta = offerteDAO.insertNewOfferta(conn, idAsta, username, prezzo);
                if (idOfferta == -1) {
                    conn.rollback();
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Inserimento offerta fallito.");
                    return;
                }

                asteDAO.setOffertaMax(conn, idAsta, idOfferta);
                conn.commit();

                response.sendRedirect(request.getContextPath() + "/offertePage?idAsta="+idAsta);
            } catch (Exception e) {
                conn.rollback();
                throw new ServletException("Errore nella gestione dell'offerta", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServletException("Errore di connessione al database", e);
        }
    }
}
