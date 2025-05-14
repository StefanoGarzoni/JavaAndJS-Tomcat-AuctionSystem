package it.polimi.tiw.Servlets;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if(request.getSession(false) != null) {
			if(request.getSession().getAttribute("username") != null) {
				request.getSession().invalidate();
				//response.sendRedirect(request.getContextPath() + "/login");
				
				// elimino tutti i cookie (es. presenti perchè si è usata l'applicazione con un altro account)
				Cookie[] cookies = request.getCookies();
				if (cookies != null) {
				    for (Cookie cookie : cookies) {
				        cookie.setValue("");        // Svuota il valore
				        cookie.setPath("/TIW-Project-JS");
				        cookie.setMaxAge(0);        // scadenza a 0 => cancella il cookie
				        response.addCookie(cookie); // invio il cookie aggiornato al client
				    }
				}
			}
		}
		response.sendRedirect(request.getContextPath() + "/login");
	}
	
}
