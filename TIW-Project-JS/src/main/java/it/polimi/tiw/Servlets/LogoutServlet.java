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
			}
		}
		response.sendRedirect(request.getContextPath() + "/login");
	}
	
}
