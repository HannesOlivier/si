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
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.datamodel.Group;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.ParticipationRole;
import de.tuclausthal.submissioninterface.persistence.datamodel.Similarity;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.persistence.datamodel.TestResult;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.HibernateSessionHelper;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a task in tutor view
 * @author Sven Strickroth
 */
public class ShowTaskTutorView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Session session = HibernateSessionHelper.getSessionFactory().openSession();

		Task task = (Task) request.getAttribute("task");
		Participation participation = (Participation) request.getAttribute("participation");

		template.printTemplateHeader(task);

		out.println("<table class=border>");
		out.println("<tr>");
		out.println("<th>Beschreibung:</th>");
		// HTML must be possible here
		out.println("<td>" + task.getDescription() + "&nbsp;</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Startdatum:</th>");
		out.println("<td>" + Util.mknohtml(task.getStart().toLocaleString()) + "</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Enddatum:</th>");
		out.println("<td>" + Util.mknohtml(task.getDeadline().toLocaleString()));
		if (task.getDeadline().before(Util.correctTimezone(new Date()))) {
			out.println(" Keine Abgabe mehr m�glich");
		}
		out.println("</td>");
		out.println("</tr>");
		out.println("<tr>");
		out.println("<th>Max. Punkte:</th>");
		out.println("<td class=points>" + task.getMaxPoints() + "</td>");
		out.println("</tr>");
		out.println("</table>");

		if (participation.getRoleType() == ParticipationRole.ADVISOR) {
			out.println("<p><div class=mid><a href=\"" + response.encodeURL("TaskManager?lecture=" + task.getLecture().getId() + "&amp;taskid=" + task.getTaskid() + "&amp;action=editTask") + "\">Aufgabe bearbeiten</a></div>");
			out.println("<p><div class=mid><a href=\"" + response.encodeURL("TaskManager?lecture=" + task.getLecture().getId() + "&amp;taskid=" + task.getTaskid() + "&amp;action=deleteTask") + "\">Aufgabe l�schen</a></div>");
		}
		if (task.getSubmissions() != null && task.getSubmissions().size() > 0) {
			out.println("<p><h2>Abgaben</h2><p>");
			Iterator<Submission> submissionIterator = DAOFactory.SubmissionDAOIf(session).getSubmissionsForTaskOrdered(task).iterator();
			Group lastGroup = null;
			boolean first = true;
			int sumOfSubmissions = 0;
			int sumOfPoints = 0;
			int groupSumOfSubmissions = 0;
			int groupSumOfPoints = 0;
			int testCols = 0;
			int lastSID = 0;
			// dynamic splitter for groups
			while (submissionIterator.hasNext()) {
				Submission submission = submissionIterator.next();
				Group group = submission.getSubmitters().iterator().next().getGroup();
				if (first == true || lastGroup != group) {
					lastGroup = group;
					if (first == false) {
						out.println("<tr>");
						out.println("<td colspan=" + (1 + submission.getTestResults().size() + task.getSimularityTests().size()) + ">Durchschnittspunkte:</td>");
						out.println("<td class=points>" + Float.valueOf(groupSumOfPoints / (float) groupSumOfSubmissions).intValue() + "</td>");
						out.println("</tr>");
						out.println("</table><p>");
						groupSumOfSubmissions = 0;
						groupSumOfPoints = 0;
					}
					first = false;
					if (group == null) {
						out.println("<h3>Ohne Gruppe</h3>");
					} else {
						out.println("<h3>Gruppe: " + Util.mknohtml(group.getName()) + "</h3>");
						out.println("<div class=mid><a href=\"ShowTask?taskid=" + task.getTaskid() + "&action=grouplist&groupid=" + group.getGid() + "\" target=\"_blank\">Druckbare Liste</a></div>");
					}
					out.println("<table class=border>");
					out.println("<tr>");
					out.println("<th>Benutzer</th>");
					for (TestResult testResult : submission.getTestResults()) {
						out.println("<th>" + Util.mknohtml(testResult.getTest().getTestTitle()) + "</th>");
					}
					testCols = Math.max(testCols, submission.getTestResults().size());
					for (SimilarityTest similarityTest : task.getSimularityTests()) {
						out.println("<th><span title=\"Max. �hnlichkeit\">" + similarityTest + "</span></th>");
					}
					out.println("<th>Punkte</th>");
					out.println("</tr>");
				}
				if (lastSID != submission.getSubmissionid()) {
					out.println("<tr>");
					out.println("<td><a href=\"" + response.encodeURL("ShowSubmission?sid=" + submission.getSubmissionid()) + "\">" + Util.mknohtml(submission.getSubmitterNames()) + "</a></td>");
					lastSID = submission.getSubmissionid();
					for (TestResult testResult : submission.getTestResults()) {
						out.println("<td>" + Util.boolToHTML(testResult.getPassedTest()) + "</td>");
					}
					for (SimilarityTest similarityTest : task.getSimularityTests()) {
						//TODO: tooltip and who it is
						String users = "";
						for (Similarity similarity : DAOFactory.SimilarityDAOIf(session).getUsersWithMaxSimilarity(similarityTest, submission)) {
							users += Util.mknohtml(similarity.getSubmissionTwo().getSubmitterNames()) + "\n";
						}
						out.println("<td class=points><span title=\"" + users + "\">" + DAOFactory.SimilarityDAOIf(session).getMaxSimilarity(similarityTest, submission) + "</span></td>");
					}
					if (submission.getPoints() != null) {
						out.println("<td align=right>" + submission.getPoints().getPoints() + "</td>");
						sumOfPoints += submission.getPoints().getPoints();
						groupSumOfPoints += submission.getPoints().getPoints();
						sumOfSubmissions++;
						groupSumOfSubmissions++;
					} else {
						out.println("<td>n/a</td>");
					}
					out.println("</tr>");
				}
			}
			if (first == false) {
				out.println("<tr>");
				out.println("<td colspan=" + (1 + testCols + task.getSimularityTests().size()) + ">Durchschnittspunkte:</td>");
				out.println("<td class=points>" + Float.valueOf(groupSumOfPoints / (float) groupSumOfSubmissions).intValue() + "</td>");
				out.println("</tr>");
				out.println("</table><p>");
				out.println("<h3>Gesamtdurchschnitt: " + Float.valueOf(sumOfPoints / (float) sumOfSubmissions).intValue() + "</h3>");
			}
		}
		template.printTemplateFooter();
	}
}
