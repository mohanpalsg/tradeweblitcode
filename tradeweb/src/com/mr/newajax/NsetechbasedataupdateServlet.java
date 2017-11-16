package com.mr.newajax;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.mr.newapp.Finalnsebasedatadnldcarmilla;
import com.mr.newapp.Nsebasedatadnldcarmilla;
import com.mr.newapp.Nsebasedownloader;

/**
 * Servlet implementation class NsetechbasedataupdateServlet
 */
@WebServlet("/NsetechbasedataupdateServlet")
public class NsetechbasedataupdateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NsetechbasedataupdateServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
		String Duration = (String) session.getAttribute("chartintervalselected");
		
		if(Duration == null)
			Duration = "300";
		
		HashMap <String,String> tnd = (HashMap <String,String>)  session.getAttribute("Trendperiodselected");
		String trendsetting = tnd.get(Duration);
		if (trendsetting == null)
			trendsetting = "12-8";
		
		String stochperiod = (String) session.getAttribute("stochperiod");
		Thread thread = new Thread(new Nsebasedatadnldcarmilla(Duration,trendsetting,stochperiod));
		thread.start();
		doGet(request,response);
	}

}
