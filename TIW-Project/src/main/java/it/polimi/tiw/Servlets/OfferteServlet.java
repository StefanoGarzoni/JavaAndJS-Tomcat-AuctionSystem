package it.polimi.tiw.Servlets;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.ArticoliDAOImpl;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.OfferteDAOImpl;
import it.polimi.tiw.dao.Beans.Articolo;
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
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

public class OfferteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private OfferteDAOImpl offerteDAO;
    private ArticoliDAOImpl articoliDAO;
    private AsteDAOImpl asteDAO;
    private TemplateEngine templateEngine;

    public void init() throws ServletException {

        ServletContext servletContext = getServletContext();
        offerteDAO = new OfferteDAOImpl();
        articoliDAO = new ArticoliDAOImpl();
        asteDAO = new AsteDAOImpl();

        JakartaServletWebApplication webApp=JakartaServletWebApplication.buildApplication(servletContext);
        WebApplicationTemplateResolver resolver=new WebApplicationTemplateResolver(webApp);

        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
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
        

        ArrayList<Articolo> articoli;
        ArrayList<Offerta>  offerte;

        try (Connection conn = ConnectionManager.getConnection()) {
            articoli = articoliDAO.getArticoliByIdAsta(conn, idAsta);
            offerte  = offerteDAO.getOfferteByIdAsta(conn, idAsta);
        } catch (SQLException e) {
            throw new ServletException("Errore durante il recupero dei dati", e);
        }
        
        Double rialzo = 0.0;
        try (Connection conn = ConnectionManager.getConnection()) {
            rialzo = asteDAO.getRialzoMinimo(conn, idAsta);
           
        } catch (SQLException e) {
            throw new ServletException("Errore durante il recupero dei dati", e);
        }

        // Preparazione modello Thymeleaf
        JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
        ctx.setVariable("articoli", articoli);
        ctx.setVariable("offerte", offerte);
        ctx.setVariable("rialzo_minimo", rialzo);
        
        if(request.getParameter("PriceTooLow")!=null)
        	ctx.setVariable("PriceTooLow", request.getParameter("PriceTooLow"));

        // Render del template
        response.setContentType("text/html;charset=UTF-8");
        try (Writer writer = response.getWriter()) {
            templateEngine.process("offerta", ctx, writer);
        }
    }
}
