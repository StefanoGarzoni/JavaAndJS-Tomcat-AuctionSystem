package it.polimi.tiw.Servlets;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.UtenteDAOImpl;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class HomeServlet extends HttpServlet {
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = "/home.html";
		
		if(request.getSession(false) == null) {	// se non esiste una sessione, indirizza al login
            response.sendRedirect(request.getContextPath() + "/login");
            return;
		}
		
		/*
		// alla prima interazione, imposta i cookie per ricaricare le tabelle
		setCookie(response, "renderAllTablesAste", "true", 30);
		setCookie(response, "renderTableAsteAperte", "true", 30);
		setCookie(response, "renderArticoli", "true", 30);
        */

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
			Boolean userLastActionWasAddedAsta = new UtenteDAOImpl().userLastActionWasAddedAsta(conn, username);
			
			
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("userLastActionWasAddedAsta", userLastActionWasAddedAsta);
			
			// uso un GsonBuilder perchè devo serializzare il flag anche nel caso in cui il valore sia null (Gson non serializza i null)
			String finalJson = new GsonBuilder().serializeNulls().create().toJson(jsonObject);
			// scrittura JSON nella response
		    PrintWriter out = response.getWriter();
		    out.print(finalJson);
		    out.flush();
		}
		catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\""+e+"\"}");
            e.printStackTrace(System.out);
		}
	}
	/*
	private void setCookie(HttpServletResponse response, String name, String value, int days) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(days*60*60*24);
        response.addCookie(cookie);
	}
	*/
}
