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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tuclausthal.submissioninterface.authfilter.SessionAdapter;
import de.tuclausthal.submissioninterface.persistence.dao.DAOFactory;
import de.tuclausthal.submissioninterface.persistence.dao.ParticipationDAOIf;
import de.tuclausthal.submissioninterface.persistence.datamodel.Participation;
import de.tuclausthal.submissioninterface.persistence.datamodel.Similarity;
import de.tuclausthal.submissioninterface.persistence.datamodel.SimilarityTest;
import de.tuclausthal.submissioninterface.persistence.datamodel.Submission;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;
import de.tuclausthal.submissioninterface.template.Template;
import de.tuclausthal.submissioninterface.template.TemplateFactory;
import de.tuclausthal.submissioninterface.util.Util;

/**
 * View-Servlet for displaying a submission to a tutor
 * @author Sven Strickroth
 */
public class ShowSubmissionView extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Template template = TemplateFactory.getTemplate(request, response);

		PrintWriter out = response.getWriter();

		Submission submission = (Submission) request.getAttribute("submission");

		List<String> submittedFiles = (List<String>) request.getAttribute("submittedFiles");
		Task task = submission.getTask();

		// check Lecture Participation
		ParticipationDAOIf participationDAO = DAOFactory.ParticipationDAOIf();
		Participation participation = participationDAO.getParticipation(new SessionAdapter(request).getUser(), submission.getTask().getLecture());

		template.printTemplateHeader(submission);

		if (submission.getSubmitter().getGroup() != null) {
			out.println("<h2>Gruppe: " + submission.getSubmitter().getGroup().getName() + "</h2>");
		}

		if (task.getDeadline().before(new Date())) {
			out.println("<h2>Bewertung:</h2>");
			out.println("<table class=border>");
			if (submission.getPoints() != null) {
				out.println("<tr>");
				out.println("<td>");
				out.println(submission.getPoints().getPoints() + "/" + task.getMaxPoints() + " Punkte vergeben ");
				out.println(" von " + submission.getPoints().getIssuedBy().getUser().getFullName());
				out.println("</td>");
				out.println("</tr>");
			}
			out.println("<tr>");
			out.println("<td>");
			out.println("<form action=\"?\" method=post>");
			out.println("<input type=hidden name=sid value=\"" + submission.getSubmissionid() + "\">");
			out.println("<input type=text name=points> (max. " + task.getMaxPoints() + ")");
			out.println(" <input type=submit>");
			out.println("</form>");
			out.println("</td>");
			out.println("</tr>");
			out.println("</table>");

			out.println("<p>");
		}

		if (submission.getSimilarSubmissions().size() > 0) {
			out.println("<h2>Ähnliche Abgaben:</h2>");
			out.println("<table>");
			out.println("<tr>");
			for (SimilarityTest similarityTest : task.getSimularityTests()) {
				out.println("<th><span title=\"" + similarityTest + "\">Ähnlichkeitstest</span></th>");
			}
			out.println("</tr>");
			out.println("<tr>");
			for (SimilarityTest similarityTest : task.getSimularityTests()) {
				out.println("<td>");
				out.println("<table class=border>");
				out.println("<tr>");
				out.println("<th>Student</th>");
				out.println("<th>Ähnlichkeit</th>");
				out.println("</tr>");
				for (Similarity similarity : DAOFactory.SimilarityDAOIf().getUsersWithSimilarity(similarityTest, submission)) {
					out.println("<tr>");
					out.println("<td><a href=\"" + response.encodeURL("ShowSubmission?sid=" + similarity.getSubmissionTwo().getSubmissionid()) + "\">" + Util.mknohtml(similarity.getSubmissionTwo().getSubmitter().getUser().getFullName()) + "</a></td>");
					out.println("<td class=points>" + similarity.getPercentage() + "%</td>");
					out.println("</tr>");
				}
				out.println("</table>");
				out.println("</td>");
			}
			out.println("</tr>");
			out.println("</table><p>");
		}

		if (task.getTest() != null && submission.getTestResult() != null) {
			out.println("<h2>Test:</h2>");
			out.println("<div class=mid>Erfolgreich: " + Util.boolToHTML(submission.getTestResult().getPassedTest()));
			out.println("<p><textarea cols=80 rows=15>" + Util.mknohtml(submission.getTestResult().getTestOutput()) + "</textarea></div>");
		}

		if (submission.getStderr() != null && !submission.getStderr().isEmpty()) {
			out.println("<h2>Standard-Error Ausgabe:</h2>");
			out.println("<p class=mid><textarea cols=80 rows=15>" + Util.mknohtml(submission.getStderr()) + "</textarea></p>");
		}
		out.println("<h2>Dateien:</h2>");
		out.println("<div class=mid>");
		for (String file : submittedFiles) {
			out.println("<iframe width=800 height=250 src=\"" + response.encodeURL("ShowFile/" + file + "?sid=" + submission.getSubmissionid()) + "\"></iframe><p>");
		}
		out.println("</div>");
		template.printTemplateFooter();
	}
}
