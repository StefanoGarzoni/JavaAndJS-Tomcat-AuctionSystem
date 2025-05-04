package it.polimi.tiw.Servlets;

import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.OfferteDAOImpl;
import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.Beans.Asta;
import it.polimi.tiw.dao.Beans.Offerta;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class DettaglioAstaPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private OfferteDAOImpl offerteDAO;
    private AsteDAOImpl asteDAO;
    private TemplateEngine templateEngine;

    public void init() throws ServletException {

        ServletContext servletContext = getServletContext();
        offerteDAO = new OfferteDAOImpl();
        asteDAO    = new AsteDAOImpl();

        JakartaServletWebApplication webApplication=JakartaServletWebApplication.buildApplication(servletContext);
        WebApplicationTemplateResolver templateResolver =new WebApplicationTemplateResolver(webApplication);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Integer idAsta;
        
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            // response.sendRedirect(request.getContextPath() + "/login?loginError=true");
            return;
        }

         String idAstaParam = request.getParameter("idAsta");

        if (idAstaParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"ID asta non specificato");
            return;
        }

        try{
            idAsta = Integer.parseInt(idAstaParam);
            session.setAttribute("idAsta", idAsta);
        }catch(Exception e){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"ID asta non valido");
            return;
        }

        String username = (String)session.getAttribute("username");

        if (username == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"username non valido in sessione.");
            return;
        }

        Context ctx = new Context(request.getLocale());

        try (Connection conn = ConnectionManager.getConnection()) {
            boolean isCreator = asteDAO.checkCreatorOfAsta(conn, username, idAsta);
            if (!isCreator) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,"Non sei il creatore dell'asta.");
                return;
            }

            Asta openAsta = asteDAO.getOpenAstaById(conn, idAsta);
            if (openAsta != null) {
                ctx.setVariable("openAsta", openAsta);
                ctx.setVariable("canBeClosed", asteDAO.astaCanBeClosed(conn, idAsta));

                ArrayList<Offerta> offerte =offerteDAO.getOfferteInOpenAsta(conn, idAsta);
                ctx.setVariable("offerte", offerte);

            } else {
                Map<Asta, ArrayList<String>> closedInfo = asteDAO.getInfoFromAClosedAsta(conn, idAsta);
                Asta astaChiusa = closedInfo.keySet().iterator().next();
                ArrayList<String> info = closedInfo.get(astaChiusa);

                ctx.setVariable("astaChiusa", astaChiusa);
                ctx.setVariable("nomeAcquirente", info.get(0));
                ctx.setVariable("prezzo", info.get(1));
                ctx.setVariable("indirizzo", info.get(2));
            }
        } catch (SQLException e) {
            throw new ServletException("Errore durante il recupero dei dati dell'asta", e);
        }

        response.setContentType("text/html;charset=UTF-8");
        try (Writer writer = response.getWriter()) {
            templateEngine.process("dettaglioAsta", ctx, writer);
        }
    }
}
