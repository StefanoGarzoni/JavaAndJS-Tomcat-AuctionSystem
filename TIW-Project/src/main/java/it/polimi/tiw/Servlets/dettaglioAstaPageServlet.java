package it.polimi.tiw.Servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.OfferteDAOImpl;
import it.polimi.tiw.dao.Beans.Asta;
import it.polimi.tiw.dao.Beans.Offerta;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/dettaglioAsta/page")
public class dettaglioAstaPageServlet extends HttpServlet {

    private OfferteDAOImpl offerteDAO;
    private AsteDAOImpl asteDAO;

    @Override
    public void init() throws ServletException {
        offerteDAO = new OfferteDAOImpl();
        asteDAO = new AsteDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        Integer idAsta = (Integer) session.getAttribute("idAsta");
        String username = (String) session.getAttribute("username");

        if (idAsta == null || username == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID asta o username non trovati in sessione.");
            return;
        }

        try (Connection conn = ConnectionManager.getConnection()) {

            // Controlla se l'utente è il creatore dell'asta
            boolean isCreator = asteDAO.checkCreatorOfAsta(conn, username, idAsta);

            if (isCreator) {
                
                Asta asta = asteDAO.getOpenAstaById(conn, idAsta);

                if (asta != null) {
                    // Verifica se l'asta può essere chiusa
                    request.setAttribute("openAsta", asta);
                    boolean canBeClosed = asteDAO.astaCanBeClosed(conn, idAsta);
                    if(canBeClosed)
                        request.setAttribute("canBeClosed", canBeClosed);

                    //asta aperta -> recupero le offerte
                    ArrayList<Offerta> offerte = offerteDAO.getOfferteInOpenAsta(conn, idAsta);
                    request.setAttribute("offerte", offerte);

                } else {
                    // recupero info per asta chiusa
                    Map<Asta, ArrayList<String>> astaChiusaInfo = asteDAO.getInfoFromAClosedAsta(conn, idAsta);
                    Asta astaChiusa = astaChiusaInfo.keySet().iterator().next();
                    request.setAttribute("astaChiusaInfo", astaChiusa);
                    request.setAttribute("nomeAcquirente", astaChiusaInfo.get(astaChiusa).get(0));
                    request.setAttribute("prezzo", astaChiusaInfo.get(astaChiusa).get(1));
                    request.setAttribute("indirizzo", astaChiusaInfo.get(astaChiusa).get(2));
                }
            }else{
                //errore: non sei il creatore dell'asta
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Non sei il creatore dell'asta.");
            }

            // Forward alla pagina Thymeleaf
            RequestDispatcher dispatcher = request.getRequestDispatcher("dettaglioAsta.html"); ///WEB-INF/views/dettaglioAsta.html
            dispatcher.forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Errore durante il recupero dei dati dell'asta", e);
        }
    }
}
