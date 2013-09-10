/*
 * Copyright 2009 - 2010,2012-2013 Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of the SubmissionInterface.
 * 
 * SubmissionInterface is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 * 
 * SubmissionInterface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SubmissionInterface. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.servlets.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.persistence.datamodel.User.SuperUserType;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;

/**
 * View-Servlet for displaying the admin users and an add form for new ones
 * @author Sven Strickroth
 */
public class AdminMenueShowAdminUsersView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		template.printTemplateHeader("Super User", "<a href=\"" + response.encodeURL("Overview") + "\">Meine Veranstaltungen</a> - <a href=\"" + response.encodeURL("AdminMenue") + "\">Admin-Menü</a> &gt; Super User");
		PrintWriter out = response.getWriter();

		printUserTable(response, out, RequestAdapter.getSession(request), ((List<User>) request.getAttribute("systemsuperusers")).iterator(), "SystemSuperUser");

		out.println("<p>SystemSuperUsers have full rights to do anything.</p>");

		printUserTable(response, out, RequestAdapter.getSession(request), ((List<User>) request.getAttribute("superusers")).iterator(), "SuperUser");
		out.println("<p>SuperUsers have the right to enter the Admin Menu and modify the lectures they are subscribed to (e.g. for testing as a student/tutor and so on).</p>");

		out.println("<div class=mid><a href=\"" + response.encodeURL("?") + "\">zur Übersicht</a></div>");
		template.printTemplateFooter();
	}

	private void printUserTable(HttpServletResponse response, PrintWriter out, Session session, Iterator<User> userIterator, String type) {
		out.println("<h2>" + type + "</h2>");
		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Benutzer</th>");
		out.println("<th>Entfernen</th>");
		out.println("</tr>");
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			out.println("<tr>");
			out.println("<td>" + user.getFullName() + "</td>");
			out.println("<td><a href=\"" + response.encodeURL("?action=removeSuperUser&amp;userid=" + user.getUid()) + "\">degradieren</a></td>");
			out.println("</tr>");
		}
		out.println("<tr>");
		out.println("<td colspan=2>");
		userIterator = DAOFactory.UserDAOIf(session).getUsers().iterator();
		out.println("<form action=\"?\">");
		out.println("<input type=hidden name=action value=add" + type + ">");
		out.println("<select name=userid>");
		while (userIterator.hasNext()) {
			User user = userIterator.next();
			if (user.getSuperUserType().compareTo(SuperUserType.NO) == 0) {
				out.println("<option value=" + user.getUid() + ">" + user.getFullName());
			}
		}
		out.println("</select>");
		out.println("<input type=submit value=\"hinzufügen\">");
		out.println("</form>");
		out.println("</td>");
		out.println("</tr>");
		out.println("</table><p>");
	}
}
