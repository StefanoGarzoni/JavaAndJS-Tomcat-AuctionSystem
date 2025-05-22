package it.polimi.tiw.Servlets;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import com.google.gson.JsonObject;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.UtenteDAOImpl;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class HomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private TemplateEngine templateEngine;
	private JakartaServletWebApplication webApplication;
	private WebApplicationTemplateResolver templateResolver;

	private UtenteDAOImpl utenteDAO;
	
	public void init() throws ServletException {
		utenteDAO = new UtenteDAOImpl();

		webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		templateResolver = new WebApplicationTemplateResolver(webApplication);
		
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = "/home.html";
		
		if(request.getSession(false) == null) {	// se non esiste una sessione, indirizza al login
            response.sendRedirect(request.getContextPath() + "/login");
            return;
		}

		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		
		templateEngine.process(path, ctx, response.getWriter());
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		if(request.getSession(false) == null) {	// se non esiste una sessione, indirizza al login
            response.sendRedirect(request.getContextPath() + "/login");
            return;
		}
		
		String username = (String) request.getSession(false).getAttribute("username");
		
		try (Connection conn = ConnectionManager.getConnection()) {
			// deve restituire il valore dell'ultima azione
			boolean isUserPrimoAccesso = utenteDAO.isUserPrimoAccesso(conn, username);
			Boolean userLastActionWasAddedAsta = false;
			boolean lastActionFound = false;

			if(!isUserPrimoAccesso){
				Cookie[] cookies = request.getCookies();

				//scorro l'array di cookie
				//cerco i cookie "lastAction"
				if (cookies != null) {
					for (Cookie c : cookies) {
						if (c.getName().equals("lastActionAstaCreated"+username)) {
							userLastActionWasAddedAsta = Boolean.parseBoolean(c.getValue());
							c.setMaxAge(60*60*24*30);
							lastActionFound = true;
							response.addCookie(c);
							break;
						}
					}
				}
			}else{
				// se è il primo accesso => userLastActionWasAddedAsta rimane al valore di default (false)
				utenteDAO.setUserPrimoAccessoAtFalse(conn, username);
			}

			if(!lastActionFound) {
				Cookie lastAction = new Cookie("lastActionAstaCreated"+username, "false");
				lastAction.setMaxAge(60*60*24*30);
				response.addCookie(lastAction);
			}
			
			setDefaultVendoCookies(response, username);

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("userLastActionWasAddedAsta", userLastActionWasAddedAsta);

			// scrittura JSON nella response
		    PrintWriter out = response.getWriter();
		    out.print(jsonObject);
		}
		catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\""+e+"\"}");
            e.printStackTrace(System.out);
		}
	}
	
	private void setDefaultVendoCookies(HttpServletResponse response, String username) {
		Cookie renderAllTablesAste = new Cookie("renderAllTablesAste"+username, "true");
		renderAllTablesAste.setMaxAge(60*60*24*30);
		response.addCookie(renderAllTablesAste);
		
		Cookie renderTableAsteAperte = new Cookie("renderTableAsteAperte"+username, "true");
		renderTableAsteAperte.setMaxAge(60*60*24*30);
		response.addCookie(renderTableAsteAperte);
		
		Cookie renderArticoli = new Cookie("renderArticoli"+username, "true");
		renderArticoli.setMaxAge(60*60*24*30);
		response.addCookie(renderArticoli);
	}
}
