package it.polimi.tiw.Servlets;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.ConnectionManager;
import it.polimi.tiw.dao.LoginDAO;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

public class LoginServlet extends HttpServlet {
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

	/** Provides the client the login web page.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = "/login.html";
		
		if(request.getSession(false) != null)	// if a session already exists (the client logged in)
			response.sendRedirect(request.getContextPath() + "/home");
		
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		
		templateEngine.process(path, ctx, response.getWriter());
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String loginPath = "/login.html";
		String mainPagePathString = "/home";
		
		HttpSession session = request.getSession(false);

		if(session != null) {
			session.invalidate();	
			// faccio questo e non un redirect alla home per evitare la seguente situazione: un utente precedentemente loggato invia nuovamente 
			// delle credenziali (anche sbagliate) e riesce ad accedere alla home perchè viene fatta una redirect 
		}
		
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		
		if(username != null && password != null) {
			try ( Connection dbConnection = ConnectionManager.getConnection() ) {
				
				boolean validCredential = new LoginDAO().areCredentialsCorrect(dbConnection, username, password);
				
				if(validCredential) {
					session = request.getSession(true);
					
					session.setAttribute("lastLoginTimestamp", LocalDateTime.now());
					session.setAttribute("username", username);
					
					response.sendRedirect(request.getContextPath()+mainPagePathString);
					return;
				}
				else {
					ctx.setVariable("loginError", "Invalid credential... Retry!");
				}
			}
			catch (SQLException e) {
				// response.sendError(500);
				ctx.setVariable("loginError", "Sorry... server problem");
				e.printStackTrace(System.out);
			}
			
			templateEngine.process(loginPath, ctx, response.getWriter());
		}
		else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing credentials");
		}
	}
}
