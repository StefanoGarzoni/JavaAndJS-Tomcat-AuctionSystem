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
import it.polimi.tiw.dao.Beans.Asta;
import jakarta.servlet.*;
import jakarta.servlet.http.*;


public class AcquistoHomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private TemplateEngine templateEngine;
	private JakartaServletWebApplication webApplication;
	private WebApplicationTemplateResolver templateResolver;
	private String vendoPath ;
	private AsteDAOImpl astaDAO;
	
	public void init() throws ServletException {
		vendoPath = "/acquisto.html";
		astaDAO = new AsteDAOImpl();
		webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		templateResolver = new WebApplicationTemplateResolver(webApplication);
		
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	// mostra la pagina base
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if(request.getSession(false) == null) {	// verify if the client is authenticated
            response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		templateEngine.process(vendoPath, ctx, response.getWriter());
	}
	
	// mostra la pagina con in aggiunta la tabella delle aste con la parola chiave
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession(false);
		
		if(session == null) {	// verify if the client is authenticated
            response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		
		String keyword = request.getParameter("parolaChiave");
		if(keyword == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		String username = (String) session.getAttribute("username");
		
		try(Connection conn = ConnectionManager.getConnection()){
			ArrayList<Asta> openAste = astaDAO.getAsteByStringInArticoli(conn, keyword, username);
			
			WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
			ctx.setVariable("asteAperte", openAste);
			
			templateEngine.process(vendoPath, ctx, response.getWriter());
		}
		catch (SQLException e) {
			e.printStackTrace(System.out);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno al server");
		}
	}
}
