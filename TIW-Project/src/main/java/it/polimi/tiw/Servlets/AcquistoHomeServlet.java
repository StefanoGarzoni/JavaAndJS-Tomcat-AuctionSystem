package it.polimi.tiw.Servlets;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.AsteDAOImpl;
import it.polimi.tiw.dao.Beans.Articolo;
import it.polimi.tiw.dao.Beans.Asta;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/**
 * Implements the login credential verification
 */
@WebServlet("/AcquistoHomeServlet")
public class AcquistoHomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private TemplateEngine templateEngine;
	
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(servletContext);
		WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);
		
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	// mostra la pagina base
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String vendoPath = "/acquisto.html";
		
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		templateEngine.process(vendoPath, ctx, response.getWriter());
	}
	
	// mostra la pagina con in aggiunta la tabella delle aste con quella keyword
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String vendoPath = "/acquisto.html";
		
		HttpSession session = request.getSession(false);
		
		if(session == null) {	// verify if the client is authenticated
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No valid session present");
			return;
		}
		
		String keyword = request.getParameter("parolaChiave");
		if(keyword == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		try(Connection conn = ConnectionManager.getConnection()){
			ArrayList<Asta> openAste = new AsteDAOImpl().getAsteByStringInArticoli(conn, keyword);
			
			JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
			WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
			ctx.setVariable("asteAperte", openAste);
			
			templateEngine.process(vendoPath, ctx, response.getWriter());
		}
		catch (SQLException e) {
			throw new ServletException("Errore di connessione al database", e);
		}
	}
}
