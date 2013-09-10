/*
 * Copyright 2009 - 2012 Sven Strickroth <email@cs-ware.de>
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

import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.User;
import de.tuclausthal.submissioninterface.persistence.datamodel.User.SuperUserType;
import de.tuclausthal.submissioninterface.servlets.RequestAdapter;
import de.tuclausthal.submissioninterface.servlets.controller.AdminMenue;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying the admin-overview/startpage
 * @author Sven Strickroth
 */
public class AdminMenueOverView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		User user = (User) request.getAttribute("user");
		Session session = RequestAdapter.getSession(request);

		boolean isSystemSuperUser = (user.getSuperUserType().compareTo(SuperUserType.SYSTEMSUPERUSER) == 0);

		template.printTemplateHeader("Admin-Menü", "<a href=\"" + response.encodeURL("Overview") + "\">Meine Veranstaltungen</a> - Admin-Menü");
		PrintWriter out = response.getWriter();

		Iterator<Lecture> lectureIterator = ((List<Lecture>) request.getAttribute("lectures")).iterator();
		if (lectureIterator.hasNext()) {
			out.println("<table class=border>");
			out.println("<tr>");
			out.println("<th>Veranstaltung</th>");
			out.println("<th>Semester</th>");
			out.println("</tr>");
			while (lectureIterator.hasNext()) {
				Lecture lecture = lectureIterator.next();
				if (isSystemSuperUser || AdminMenue.hasRightsToSee(session, user, lecture)) {
					out.println("<tr>");
					out.println("<td><a href=\"" + response.encodeURL("?action=showLecture&amp;lecture=" + lecture.getId()) + "\">" + Util.escapeHTML(lecture.getName()) + "</a></td>");
					out.println("<td>" + lecture.getReadableSemester() + "</td>");
					out.println("</tr>");
				}
			}
			out.println("</table>");
		}

		if (isSystemSuperUser) {
			out.println("<p class=mid><a href=\"" + response.encodeURL("?action=newLecture") + "\">Neue Veranstaltung</a></p>");
			out.println("<p class=mid><a href=\"" + response.encodeURL("?action=showAdminUsers") + "\">Super User anzeigen</a></p>");
			out.println("<p class=mid><a onclick=\"return confirmLink('Wirklich CleanUp durchführen?')\" href=\"" + response.encodeURL("?action=cleanup") + "\">Verzeichnis Cleanup</a></p>");
		}

		template.printTemplateFooter();
	}
}
