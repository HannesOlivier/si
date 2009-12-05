/*
 * Copyright 2009 Sven Strickroth <email@cs-ware.de>
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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.dao.SubmissionDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Lecture;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Student;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a lecture in tutor/advisor view
 * @author Sven Strickroth
 */
public class ShowLectureTutorFullView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Participation participation = (Participation) request.getAttribute("participation");
		Lecture lecture = participation.getLecture();
		Session session = HibernateSessionHelper.getSessionFactory().openSession();
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf(session);
		SubmissionDAOIf submissionDAO = DAOFactory.SubmissionDAOIf(session);

		// list all tasks for a lecture
		template.printTemplateHeader(lecture);

		List<Task> taskList = lecture.getTasks();

		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>MatrikelNo</th>");
		out.println("<th>Benutzer</th>");
		for (Task task : taskList) {
			out.println("<th>" + Util.mknohtml(task.getTitle()) + "<br>Pkts: " + task.getMaxPoints() + "</th>");
		}
		out.println("<th>Gesamt</th>");
		out.println("</tr>");

		for (Participation lectureParticipation : participationDAO.getParticipationsOfLectureOrdered(lecture)) {
			out.println("<tr>");
			if (lectureParticipation.getUser() instanceof Student) {
				out.println("<td>" + ((Student) lectureParticipation.getUser()).getMatrikelno() + "</td>");
			} else {
				out.println("<td>n/a</td>");
			}
			out.println("<td>" + lectureParticipation.getUser().getFullName() + "</td>");
			int points = 0;
			for (Task task : taskList) {
				Submission submission = submissionDAO.getSubmission(task, lectureParticipation.getUser());
				if (submission != null && submission.getPoints() != null) {
					if (submission.getPoints().getPointsOk()) {
						out.println("<td class=points>" + submission.getPoints().getPoints() + "</td>");
						points += submission.getPoints().getPoints();
					} else {
						out.println("<td class=points>(" + submission.getPoints().getPoints() + ")</td>");
					}
				} else {
					out.println("<td>n/a</td>");
				}
			}
			if (points > 0) {
				out.println("<td class=points>" + points + "</td>");
			} else {
				out.println("<td>n/a</td>");
			}
			out.println("</tr>");
		}

		out.println("</table>");
		template.printTemplateFooter();
	}
}