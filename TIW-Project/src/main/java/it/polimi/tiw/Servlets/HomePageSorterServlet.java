package it.polimi.tiw.Servlets;

import java.io.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class HomePageSorterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/** Produces the home page
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		if(request.getSession(false) == null)	// if a session already exists (the client logged in)
			response.sendRedirect(request.getContextPath() + "/login");
		else
			response.sendRedirect(request.getContextPath() + "/TIW-Project/home");
	}
}
