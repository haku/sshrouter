package com.vaguehope.sshrouter.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaguehope.sshrouter.C;

public class IpServlet extends HttpServlet {

	private static final long serialVersionUID = -2826730502711068672L;

	@Override
	protected void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().print(C.APPNAME + " desu~");
	}

}
